package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.core.AbstractStatefulFrameworkServiceTracker;
import com.bytex.snamp.supervision.*;
import org.osgi.framework.ServiceReference;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import javax.management.InstanceNotFoundException;
import java.util.Collections;
import java.util.Map;

/**
 * Collects supervision events and send them using SMTP.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class SmtpModelOfSupervisors extends AbstractStatefulFrameworkServiceTracker<Supervisor, SupervisorClient, Map<String, MailMessageFactory>> implements SupervisionEventListener {
    SmtpModelOfSupervisors() {
        super(Supervisor.class, new InternalState<>(Collections.emptyMap()));
    }

    @Override
    public void handle(@Nonnull final SupervisionEvent event, final Object handback) {
        for(final MailMessageFactory factory: getConfiguration().values()){

        }
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
    protected SupervisorFilterBuilder createServiceFilter() {
        return SupervisorClient.filterBuilder();
    }

    @Override
    protected void stop() {

    }

    @Override
    protected void start(final Map<String, MailMessageFactory> configuration) {

    }

    @Override
    protected SupervisorClient createClient(final ServiceReference<Supervisor> serviceRef) throws InstanceNotFoundException {
        return new SupervisorClient(getBundleContext(), serviceRef);
    }

    @Override
    protected String getServiceId(final SupervisorClient client) {
        return client.getGroupName();
    }
}
