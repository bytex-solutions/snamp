package com.itworks.snamp.adapters.groovy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.eventbus.Subscribe;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.adapters.NotificationEvent;
import com.itworks.snamp.adapters.NotificationListener;
import com.itworks.snamp.concurrent.Repeater;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.core.OSGiLoggingContext;
import com.itworks.snamp.internal.annotations.SpecialUse;
import com.itworks.snamp.io.Communicator;
import com.itworks.snamp.jmx.JMExceptionUtils;
import groovy.lang.Closure;
import groovy.lang.Script;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import javax.management.*;
import java.util.Collection;
import java.util.EventListener;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * Represents an abstract class for adapter automation script.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ResourceAdapterScript extends Script implements AutoCloseable, ManagementInformationRepository, NotificationListener {
    public static final String REPOSITORY_GLOBAL_VAR = "repository";

    private static final String LOGGER_NAME = ResourceAdapterInfo.getLoggerName();

    private ManagementInformationRepository getRepository(){
        return (ManagementInformationRepository)super.getProperty(REPOSITORY_GLOBAL_VAR);
    }

    @SpecialUse
    protected static Communicator getCommunicator(final String sessionName) throws ExecutionException {
        return Communicator.getSession(sessionName);
    }

    @SpecialUse
    protected static EventListener asListener(final Closure<?> closure){
        return new EventListener(){

            @Subscribe
            @SpecialUse
            public void accept(final Object message){
                closure.call(message);
            }
        };
    }

    /**
     * Creates a new timer which executes the specified action.
     * @param job The action to execute periodically. Cannot be {@literal null}.
     * @param period Execution period, in millis.
     * @return A new timer.
     */
    @SpecialUse
    protected static Repeater createTimer(final Closure<?> job, final long period) {
        return new Repeater(period) {
            @Override
            protected void doAction() {
                job.call();
            }
        };
    }

    /**
     * Schedules a new periodic task
     * @param job The action to execute periodically. Cannot be {@literal null}.
     * @param period Execution period, in millis.
     * @return Executed timer.
     */
    @SpecialUse
    protected static Repeater schedule(final Closure<?> job, final long period){
        final Repeater timer = createTimer(job, period);
        timer.run();
        return timer;
    }

    @SpecialUse
    protected static void error(final String message) {
        OSGiLoggingContext.within(LOGGER_NAME, new SafeConsumer<Logger>() {
            @Override
            public void accept(final Logger logger) {
                logger.severe(message);
            }
        });
    }

    @SpecialUse
    protected static void warning(final String message) {
        OSGiLoggingContext.within(LOGGER_NAME, new SafeConsumer<Logger>() {
            @Override
            public void accept(final Logger logger) {
                logger.warning(message);
            }
        });
    }

    @SpecialUse
    protected static void info(final String message) {
        OSGiLoggingContext.within(LOGGER_NAME, new SafeConsumer<Logger>() {
            @Override
            public void accept(final Logger logger) {
                logger.info(message);
            }
        });
    }

    @SpecialUse
    protected static void debug(final String message) {
        OSGiLoggingContext.within(LOGGER_NAME, new SafeConsumer<Logger>() {
            @Override
            public void accept(final Logger logger) {
                logger.config(message);
            }
        });
    }

    @SpecialUse
    protected static void fine(final String message) {
        OSGiLoggingContext.within(LOGGER_NAME, new SafeConsumer<Logger>() {
            @Override
            public void accept(final Logger logger) {
                logger.fine(message);
            }
        });
    }

    private static BundleContext getBundleContext() {
        return FrameworkUtil.getBundle(ResourceAdapterScript.class).getBundleContext();
    }

    @Override
    @SpecialUse
    public final Set<String> getHostedResources() {
        final ManagementInformationRepository provider = getRepository();
        return provider != null ? provider.getHostedResources() : ImmutableSet.<String>of();
    }

    @Override
    @SpecialUse
    public final Set<String> getResourceAttributes(final String resourceName) {
        final ManagementInformationRepository provider = getRepository();
        return provider != null ? provider.getResourceAttributes(resourceName) : ImmutableSet.<String>of();
    }

    @Override
    @SpecialUse
    public final Object getAttributeValue(final String resourceName, final String attributeName) throws MBeanException, AttributeNotFoundException, ReflectionException {
        final ManagementInformationRepository provider = getRepository();
        if(provider != null)
            return provider.getAttributeValue(resourceName, attributeName);
        else throw JMExceptionUtils.attributeNotFound(attributeName);
    }

    @Override
    public final void setAttributeValue(final String resourceName, final String attributeName, final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        final ManagementInformationRepository provider = getRepository();
        if(provider != null)
            provider.setAttributeValue(resourceName, attributeName, value);
        else throw JMExceptionUtils.attributeNotFound(attributeName);
    }

    /**
     * Obtains direct reference to the specified managed resource.
     * @param resourceName The name of the connected resource. Cannot be {@literal null} or empty.
     * @return Direct reference to the managed resource.
     * @throws InstanceNotFoundException Managed resource with specified name doesn't exist.
     * @see #releaseManagedResource(ManagedResourceConnectorClient)
     */
    @SpecialUse
    protected static ManagedResourceConnectorClient getManagedResource(final String resourceName) throws InstanceNotFoundException {
        return new ManagedResourceConnectorClient(getBundleContext(), resourceName);
    }

    /**
     * Releases direct reference to the specified managed resource.
     * @param client Managed resource client. Cannot be {@literal null}.
     * @see #getManagedResource(String)
     */
    @SpecialUse
    protected static void releaseManagedResource(final ManagedResourceConnectorClient client){
        client.release(getBundleContext());
    }

    /**
     * Handles notifications.
     *
     * @param event Notification event.
     */
    @Override
    public final void handleNotification(final NotificationEvent event) {
        handleNotification(event.getSource(), event.getNotification());
    }

    @Override
    public final Collection<MBeanAttributeInfo> getAttributes(final String resourceName) {
        final ManagementInformationRepository repository = getRepository();
        return repository == null ? ImmutableList.<MBeanAttributeInfo>of() : repository.getAttributes(resourceName);
    }

    @Override
    public final Collection<MBeanNotificationInfo> getNotifications(final String resourceName) {
        final ManagementInformationRepository repository = getRepository();
        return repository == null ? ImmutableList.<MBeanNotificationInfo>of() : repository.getNotifications(resourceName);
    }

    @SpecialUse
    protected Object handleNotification(final Object metadata,
                                      final Object notif){
        return null;
    }

    /**
     * Releases all resources associated with this script.
     * @throws Exception Unable to release resources.
     */
    @Override
    public void close() throws Exception {
        setProperty(REPOSITORY_GLOBAL_VAR, null);
    }
}
