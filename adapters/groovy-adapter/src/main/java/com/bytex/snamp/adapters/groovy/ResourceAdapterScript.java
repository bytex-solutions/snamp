package com.bytex.snamp.adapters.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.TimeSpan;
import com.bytex.snamp.adapters.NotificationEvent;
import com.bytex.snamp.adapters.NotificationListener;
import com.bytex.snamp.adapters.groovy.dsl.GroovyManagementModel;
import com.bytex.snamp.adapters.modeling.AttributeAccessor;
import com.bytex.snamp.adapters.modeling.NotificationAccessor;
import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.internal.EntryReader;
import com.bytex.snamp.io.Communicator;
import com.google.common.eventbus.Subscribe;
import groovy.lang.Closure;
import groovy.lang.Script;

import javax.management.JMException;
import java.util.EventListener;
import java.util.concurrent.ExecutionException;

/**
 * Represents an abstract class for adapter automation script.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ResourceAdapterScript extends Script implements AutoCloseable, NotificationListener {
    public static final String MODEL_GLOBAL_VAR = "resources";

    private GroovyManagementModel getModel() {
        return (GroovyManagementModel) super.getProperty(MODEL_GLOBAL_VAR);
    }

    private <T> T queryModelObject(final Class<T> objectType){
        final GroovyManagementModel model = getModel();
        return model != null ? model.queryObject(objectType) : null;
    }

    @SpecialUse
    protected static Communicator getCommunicator(final String sessionName) throws ExecutionException {
        return Communicator.getSession(sessionName);
    }

    @SpecialUse
    protected static EventListener asListener(final Closure<?> closure) {
        return new EventListener() {
            @Subscribe
            @SpecialUse
            public void accept(final Object message) {
                closure.call(message);
            }
        };
    }

    /**
     * Creates a new timer which executes the specified action.
     *
     * @param job    The action to execute periodically. Cannot be {@literal null}.
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
     *
     * @param job    The action to execute periodically. Cannot be {@literal null}.
     * @param period Execution period, in millis.
     * @return Executed timer.
     */
    @SpecialUse
    protected static Repeater schedule(final Closure<?> job, final long period) {
        final Repeater timer = createTimer(job, period);
        timer.run();
        return timer;
    }

    @SpecialUse
    protected static void error(final String message) {
        ResourceAdapterInfo.getLogger().severe(message);
    }

    @SpecialUse
    protected static void warning(final String message) {
        ResourceAdapterInfo.getLogger().warning(message);
    }

    @SpecialUse
    protected static void info(final String message) {
        ResourceAdapterInfo.getLogger().info(message);
    }

    @SpecialUse
    protected static void debug(final String message) {
        ResourceAdapterInfo.getLogger().config(message);
    }

    @SpecialUse
    protected static void fine(final String message) {
        ResourceAdapterInfo.getLogger().fine(message);
    }

    private static void processAttributes(final AttributesRootAPI model, final Closure<?> closure) throws JMException {
        model.processAttributes(new EntryReader<String, AttributeAccessor, JMException>() {
            @Override
            public boolean read(final String resourceName, final AttributeAccessor accessor) throws JMException {
                switch (closure.getMaximumNumberOfParameters()) {
                    case 0:
                        closure.call();
                        return true;
                    case 1:
                        closure.call(accessor.getMetadata());
                        return true;
                    case 2:
                        closure.call(resourceName, accessor.getMetadata());
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @SpecialUse
    protected final void processAttributes(final Closure<?> closure) throws JMException {
        final AttributesRootAPI model = queryModelObject(AttributesRootAPI.class);
        if (model != null)
            processAttributes(model, closure);
    }

    private static void processEvents(final EventsRootAPI model, final Closure<?> closure) throws JMException {
        model.processEvents(new EntryReader<String, NotificationAccessor, JMException>() {
            @Override
            public boolean read(final String resourceName, final NotificationAccessor accessor) throws JMException {
                switch (closure.getMaximumNumberOfParameters()) {
                    case 0:
                        closure.call();
                        return true;
                    case 1:
                        closure.call(accessor.getMetadata());
                        return true;
                    case 2:
                        closure.call(resourceName, accessor.getMetadata());
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    @SpecialUse
    protected final void processEvents(final Closure<?> closure) throws JMException {
        final EventsRootAPI model = queryModelObject(EventsRootAPI.class);
        if(model != null)
            processEvents(model, closure);
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

    @SpecialUse
    protected Object handleNotification(final Object metadata,
                                        final Object notif) {
        return null;
    }

    @SpecialUse
    protected final ResourceAttributesAnalyzer<?> attributesAnalyzer(final TimeSpan checkPeriod) {
        final AttributesRootAPI model = queryModelObject(AttributesRootAPI.class);
        return model != null ? model.attributesAnalyzer(checkPeriod) : null;
    }

    @SpecialUse
    protected final ResourceAttributesAnalyzer<?> attributesAnalyzer(final long checkPeriod){
        return attributesAnalyzer(TimeSpan.ofMillis(checkPeriod));
    }

    @SpecialUse
    protected final ResourceNotificationsAnalyzer eventsAnalyzer() {
        final EventsRootAPI model = queryModelObject(EventsRootAPI.class);
        return model != null ? model.eventsAnalyzer() : null;
    }

    /**
     * Releases all resources associated with this script.
     *
     * @throws Exception Unable to release resources.
     */
    @Override
    public void close() throws Exception {
        setProperty(MODEL_GLOBAL_VAR, null);
    }
}
