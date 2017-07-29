package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.connector.health.HealthStatus;
import com.bytex.snamp.connector.health.MalfunctionStatus;
import com.bytex.snamp.core.AbstractStatefulFrameworkServiceTracker;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.supervision.*;
import com.bytex.snamp.supervision.elasticity.MaxClusterSizeReachedEvent;
import com.bytex.snamp.supervision.elasticity.ScaleInEvent;
import com.bytex.snamp.supervision.elasticity.ScaleOutEvent;
import com.bytex.snamp.supervision.elasticity.ScalingEvent;
import com.bytex.snamp.supervision.health.HealthStatusChangedEvent;
import com.bytex.snamp.supervision.health.ResourceGroupHealthStatus;
import org.osgi.framework.ServiceReference;
import org.stringtemplate.v4.ST;

import javax.activation.DataHandler;
import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

/**
 * Collects supervision events and send them using SMTP.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
final class SmtpModelOfSupervisors extends AbstractStatefulFrameworkServiceTracker<Supervisor, SupervisorClient, Map<String, MailMessageFactory>> implements SupervisionEventListener {
    SmtpModelOfSupervisors() {
        super(Supervisor.class, new InternalState<>(Collections.emptyMap()));
    }

    private static ST addCommonParameters(final SupervisionEvent event,
                                            final ST template) {
        return template
                .add("groupName", event.getGroupName())
                .add("timeStamp", event.getTimeStamp());
    }

    private static ST addCompositionChangedParameters(final GroupCompositionChangedEvent event,
                                                        final ST template) {
        return addCommonParameters(event, template).add("resourceName", event.getResourceName());
    }

    private static ST addScalingParameters(final ScalingEvent event,
                                             final ST template) {
        return addCommonParameters(event, template)
                .add("evaluationResult", event.getPolicyEvaluationResult().entrySet())
                .add("castingVoteWeight", event.getCastingVoteWeight());
    }

    private void sendHealthStatusToMail(final HealthStatusChangedEvent event,
                                        final MailMessageFactory factory) {
        final HealthStatus previousStatus = event.getPreviousStatus().getSummaryStatus(),
                newStatus = event.getNewStatus().getSummaryStatus();
        if (newStatus instanceof MalfunctionStatus && !previousStatus.like(newStatus))
            try {
                final Message message = factory.createMessage();
                message.setSubject("Health status of " + event.getGroupName() + ':' + newStatus);
                //prepare template
                final ST template = addCommonParameters(event, factory.prepareHealthStatusTemplate())
                        .add("malfunctionLevel", ((MalfunctionStatus) newStatus).getLevel())
                        .add("status", newStatus)
                        .add("statuses", event.getNewStatus().entrySet())
                        .add("resourceName", ResourceGroupHealthStatus.getMostProblematicResource(event.getNewStatus()).orElse(""));
                final MimeMultipart attachments = new MimeMultipart();
                //set message text
                MimeBodyPart part = new MimeBodyPart();
                part.setText(template.render(), IOUtils.DEFAULT_CHARSET.name());
                attachments.addBodyPart(part);
                //set details about health status
                part = new MimeBodyPart();
                part.setFileName("health_status.json");
                part.setDescription("Detailed information about health status");
                part.setDataHandler(new DataHandler(new JsonDataSource(part.getFileName(), ((MalfunctionStatus) newStatus).getData())));
                attachments.addBodyPart(part);
                message.setContent(attachments);
                Transport.send(message);
            } catch (final MessagingException e) {
                getLogger().log(Level.SEVERE, "Unable to send e-mail with health status", e);
            }
    }

    private void sendNewResourceToMail(final ResourceAddedEvent event,
                                       final MailMessageFactory factory) {
        try {
            final Message message = factory.createMessage();
            message.setSubject("New resource " + event.getResourceName() + " in group " + event.getGroupName());
            final ST template = addCompositionChangedParameters(event, factory.prepareNewResourceTemplate());
            message.setText(template.render());
            Transport.send(message);
        } catch (final MessagingException e) {
            getLogger().log(Level.SEVERE, "Unable to send e-mail with information about new resource", e);
        }
    }

    private void sendRemovedResourceToMail(final ResourceRemovedEvent event,
                                                  final MailMessageFactory factory) {
        try {
            final Message message = factory.createMessage();
            message.setSubject("Resource " + event.getResourceName() + " was removed from group " + event.getGroupName());
            final ST template = addCompositionChangedParameters(event, factory.prepareRemovedResourceTemplate());
            message.setText(template.render());
            Transport.send(message);
        } catch (final MessagingException e) {
            getLogger().log(Level.SEVERE, "Unable to send e-mail with information about removed resource", e);
        }
    }

    private void sendUpscaleToMail(final ScaleOutEvent event,
                                   final MailMessageFactory factory) {
        try {
            final Message message = factory.createMessage();
            message.setSubject("Scale-out: " + event.getGroupName());
            final ST template = addScalingParameters(event, factory.prepareScaleOutTemplate());
            message.setText(template.render());
            Transport.send(message);
        } catch (final MessagingException e) {
            getLogger().log(Level.SEVERE, "Unable to send e-mail with information about scale-out of group", e);
        }
    }

    private void sendDownscaleToMail(final ScaleInEvent event,
                                   final MailMessageFactory factory) {
        try {
            final Message message = factory.createMessage();
            message.setSubject("Scale-in: " + event.getGroupName());
            final ST template = addScalingParameters(event, factory.prepareScaleInTemplate());
            message.setText(template.render());
            Transport.send(message);
        } catch (final MessagingException e) {
            getLogger().log(Level.SEVERE, "Unable to send e-mail with information about scale-in of group", e);
        }
    }

    private void sendMaxClusterSizeReachedToMail(final MaxClusterSizeReachedEvent event,
                                     final MailMessageFactory factory) {
        try {
            final Message message = factory.createMessage();
            message.setSubject("Max size of group is reached: " + event.getGroupName());
            final ST template = addScalingParameters(event, factory.prepareScaleInTemplate());
            message.setText(template.render());
            Transport.send(message);
        } catch (final MessagingException e) {
            getLogger().log(Level.SEVERE, "Unable to send e-mail with information about max size of group", e);
        }
    }

    @Override
    public void handle(@Nonnull final SupervisionEvent event, final Object handback) {
        for (final MailMessageFactory messageFactory : getConfiguration().values())
            if (event instanceof HealthStatusChangedEvent)
                sendHealthStatusToMail((HealthStatusChangedEvent) event, messageFactory);
            else if (event instanceof ResourceAddedEvent)
                sendNewResourceToMail((ResourceAddedEvent) event, messageFactory);
            else if (event instanceof ResourceRemovedEvent)
                sendRemovedResourceToMail((ResourceRemovedEvent) event, messageFactory);
            else if (event instanceof ScaleOutEvent)
                sendUpscaleToMail((ScaleOutEvent) event, messageFactory);
            else if (event instanceof ScaleInEvent)
                sendDownscaleToMail((ScaleInEvent) event, messageFactory);
            else if(event instanceof MaxClusterSizeReachedEvent)
                sendMaxClusterSizeReachedToMail((MaxClusterSizeReachedEvent) event, messageFactory);
    }

    @Override
    protected void addService(@WillNotClose final SupervisorClient supervisor) {
        supervisor.addSupervisionEventListener(this);
    }

    @Override
    protected void removeService(@WillNotClose final SupervisorClient supervisor) {
        supervisor.removeSupervisionEventListener(this);
    }

    /**
     * Returns filter used to query services from OSGi Service Registry.
     *
     * @return A filter used to query services from OSGi Service Registry.
     */
    @Nonnull
    @Override
    protected SupervisorSelector createServiceFilter() {
        return SupervisorClient.selector();
    }

    @Override
    protected void stop() {

    }

    @Override
    protected void start(final Map<String, MailMessageFactory> configuration) {

    }

    @Override
    @Nonnull
    protected SupervisorClient createClient(final ServiceReference<Supervisor> serviceRef) throws InstanceNotFoundException {
        return new SupervisorClient(getBundleContext(), serviceRef);
    }

    @Override
    protected String getServiceId(final SupervisorClient client) {
        return client.getGroupName();
    }
}
