package com.bytex.snamp.scripting.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import com.bytex.snamp.core.Communicator;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;

import javax.management.*;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Represents basic API for scripting.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public interface ScriptingAPI extends GroovyObject {
    /**
     * Creates a new timer which executes the specified action.
     *
     * @param job    The action to execute periodically. Cannot be {@literal null}.
     * @param period Execution period, in millis.
     * @return A new timer.
     */
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    static Repeater createTimer(final Closure<?> job, final long period) {
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
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    static Repeater schedule(final Closure<?> job, final long period) {
        final Repeater timer = createTimer(job, period);
        timer.run();
        return timer;
    }

    /**
     * Gets logger.
     * @return Logger.
     */
    Logger getLogger();

    /**
     * Reads value of the managed resource attribute.
     *
     * @param resourceName  The name of the managed resource.
     * @param attributeName The name of the attribute.
     * @return The value of the attribute.
     * @throws JMException Unable to
     */
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    Object getResourceAttribute(final String resourceName, final String attributeName) throws JMException;

    /**
     * Reads attribute metadata.
     *
     * @param resourceName  The name of the managed resource.
     * @param attributeName The name of the attribute.
     * @return A dictionary of attribute parameters.
     * @throws AttributeNotFoundException The attribute doesn't exist in the specified managed resource.
     */
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    Dictionary<String, ?> getResourceAttributeInfo(final String resourceName, final String attributeName) throws AttributeNotFoundException, InstanceNotFoundException;

    /**
     * Sets value of the managed resource attribute.
     *
     * @param resourceName  The name of the managed resource.
     * @param attributeName The name of the attribute.
     * @param value         The value of the attribute.
     * @throws JMException Unable to set attribute value.
     */
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    void setResourceAttribute(final String resourceName,
                                               final String attributeName,
                                               final Object value) throws JMException;

    /**
     * Reads notification metadata.
     *
     * @param resourceName The name of the managed resource.
     * @param notifType    The notification type as it configured in the managed resource.
     * @return A dictionary of attribute parameters.
     * @throws MBeanException Notification is not declared by managed resource.
     */
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    Dictionary<String, ?> getResourceNotificationInfo(final String resourceName,
                                                                       final String notifType) throws MBeanException, InstanceNotFoundException;

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    void addNotificationListener(final String resourceName, final NotificationListener listener) throws MBeanException, InstanceNotFoundException;

    void addNotificationListener(final String resourceName,
                                 final NotificationListener listener,
                                 final NotificationFilter filter,
                                 final Objects handback) throws MBeanException, InstanceNotFoundException;

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    void removeNotificationListener(final String resourceName, final NotificationListener listener) throws ListenerNotFoundException, InstanceNotFoundException;

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    ManagedResourceConfiguration getResourceConfiguration(final String resourceName) throws IOException;

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    boolean isActiveClusterNode();

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    Set<String> getResources();

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    Communicator getCommunicator(final String sessionName);
}
