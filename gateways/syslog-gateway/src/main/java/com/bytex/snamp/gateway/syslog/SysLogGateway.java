package com.bytex.snamp.gateway.syslog;

import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.modeling.FeatureAccessor;
import com.bytex.snamp.gateway.modeling.ModelOfAttributes;
import com.bytex.snamp.gateway.modeling.ModelOfNotifications;
import com.bytex.snamp.internal.Utils;
import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import com.cloudbees.syslog.sender.SyslogMessageSender;
import com.google.common.collect.Multimap;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class SysLogGateway extends AbstractGateway {
    private static final class SysLogModelOfAttributes extends ModelOfAttributes<SysLogAttributeAccessor> {

        @Override
        protected SysLogAttributeAccessor createAccessor(final String resourceName, final MBeanAttributeInfo metadata) {
            return new SysLogAttributeAccessor(metadata);
        }
    }

    private static final class SysLogModelOfNotifications extends ModelOfNotifications<SysLogNotificationAccessor> implements NotificationListener {
        private SyslogMessageSender checkSender;

        @Override
        protected SysLogNotificationAccessor createAccessor(final String resourceName, final MBeanNotificationInfo metadata) {
            return new SysLogNotificationAccessor(resourceName, metadata, this);
        }

        private void setCheckSender(final SyslogMessageSender value){
            this.checkSender = Objects.requireNonNull(value);
        }

        private Logger getLogger(){
            return LoggerProvider.getLoggerForObject(this);
        }

        @Override
        public void handleNotification(final NotificationEvent event) {
            final Severity severity = SysLogNotificationAccessor.getSeverity(event.getMetadata());
            final Facility facility = SysLogNotificationAccessor.getFacility(event.getMetadata());
            final String applicationName = SysLogNotificationAccessor.getApplicationName(event.getMetadata(), event.getResourceName());
            final SyslogMessageSender checkSender = this.checkSender;
            if (checkSender != null) {
                final SyslogMessage message = new SyslogMessage()
                        .withSeverity(severity)
                        .withFacility(facility)
                        .withMsgId(event.getNotification().getType())
                        .withAppName(applicationName)
                        .withTimestamp(event.getNotification().getTimeStamp())
                        .withMsg(new CharArrayWriter().append(event.getNotification().getMessage()))
                        .withProcId(SysLogUtils.getProcessId(applicationName));
                try {
                    checkSender.sendMessage(message);
                } catch (final IOException e) {
                    getLogger().log(Level.SEVERE, String.format("Failed to wrap notification %s of resource %s into syslog message", event.getType(), event.getResourceName()), e);
                }
            }
        }

        @Override
        protected void cleared() {
            checkSender = null;
        }
    }

    private final SysLogModelOfAttributes attributes;
    private SysLogAttributeSender attributeSender;
    private final SysLogModelOfNotifications notifications;

    SysLogGateway(final String gatewayInstance){
        super(gatewayInstance);
        attributes = new SysLogModelOfAttributes();
        notifications = new SysLogModelOfNotifications();
        attributeSender = null;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)attributes.addAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>)notifications.addNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) throws Exception {
        return Stream.concat(
                notifications.clear(resourceName).stream(),
                attributes.clear(resourceName).stream()
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>)attributes.removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>)notifications.removeNotification(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected void start(final Map<String, String> parameters) throws Exception {
        final SysLogConfigurationDescriptor parser = SysLogConfigurationDescriptor.getInstance();
        final SyslogMessageSender sender = parser.createSender(parameters);
        attributeSender = new SysLogAttributeSender(parser.getPassiveCheckSendPeriod(parameters),
                sender,
                attributes);
        notifications.setCheckSender(sender);
        attributeSender.run();
    }

    @Override
    protected void stop() throws Exception {
        Utils.closeAll(attributeSender, attributes::clear, notifications::clear);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends MBeanFeatureInfo> Multimap<String, ? extends FeatureBindingInfo<M>> getBindings(final Class<M> featureType) {
        if(featureType.isAssignableFrom(MBeanAttributeInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getBindings(attributes);
        else if(featureType.isAssignableFrom(MBeanNotificationInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getBindings(notifications);
        else return super.getBindings(featureType);
    }
}
