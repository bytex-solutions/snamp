package com.bytex.snamp.gateway.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.gateway.NotificationEvent;
import com.bytex.snamp.gateway.NotificationListener;
import com.bytex.snamp.gateway.groovy.dsl.GroovyManagementModel;
import com.bytex.snamp.scripting.groovy.Scriptlet;
import groovy.lang.Closure;

import java.time.Duration;
import java.util.Optional;

/**
 * Represents an abstract class for gateway automation script.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public abstract class GatewayScript extends Scriptlet implements AutoCloseable, NotificationListener {
    public static final String MODEL_GLOBAL_VAR = "resources";

    private GroovyManagementModel getModel() {
        return (GroovyManagementModel) super.getProperty(MODEL_GLOBAL_VAR);
    }

    private <T> Optional<T> queryModelObject(final Class<T> objectType){
        return Optional.ofNullable(getModel())
                .flatMap(model -> model.queryObject(objectType));
    }

    private static void processAttributes(final AttributesRootAPI model, final Closure<?> closure) {
        model.processAttributes((resourceName, accessor) -> {
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
        });
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final void processAttributes(final Closure<?> closure) {
        queryModelObject(AttributesRootAPI.class).ifPresent(attributesRootAPI -> processAttributes(attributesRootAPI, closure));
    }

    private static void processEvents(final EventsRootAPI model, final Closure<?> closure) {
        model.processEvents((resourceName, accessor) -> {
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
        });
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final void processEvents(final Closure<?> closure) {
        queryModelObject(EventsRootAPI.class).ifPresent(eventsRootAPI -> processEvents(eventsRootAPI, closure));
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

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected Object handleNotification(final Object metadata,
                                        final Object notif) {
        return null;
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final ResourceAttributesAnalyzer<?> attributesAnalyzer(final Duration checkPeriod) {
        return queryModelObject(AttributesRootAPI.class)
                .map(model -> model.attributesAnalyzer(checkPeriod))
                .orElseThrow(AssertionError::new);
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final ResourceAttributesAnalyzer<?> attributesAnalyzer(final long checkPeriod){
        return attributesAnalyzer(Duration.ofMillis(checkPeriod));
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final ResourceNotificationsAnalyzer eventsAnalyzer() {
        return queryModelObject(EventsRootAPI.class).map(EventsRootAPI::eventsAnalyzer).orElseThrow(AssertionError::new);
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
