package com.snamp.adapters;

import com.snamp.*;
import com.snamp.connectors.ManagementConnector;

import java.io.IOException;
import java.util.*;

import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;
import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration;

/**
 * Represents hosting adapter, that exposes management information to the outside world.
 * <p>
 *     The implementer should have parameterless constructor and annotated with {@link net.xeoh.plugins.base.annotations.PluginImplementation}.
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Lifecycle(InstanceLifecycle.SINGLE_PER_PROCESS)
public interface Adapter extends AutoCloseable, PlatformPlugin {
    /**
     * Represents name of the port definition parameter.
     */
    static final String portParamName = "port";

    /**
     * Represents name of the hosting address definition parameter.
     */
    static final String addressParamName = "address";

    /**
     * Exposes the connector to the world.
     * @param parameters The adapter startup parameters.
     * @return {@literal true}, if adapter is started successfully; otherwise, {@literal false}.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public boolean start(final Map<String, String> parameters) throws IOException;

    /**
     * Stops the connector hosting.
     * @param saveAttributes {@literal true} to save previously exposed attributes for reuse; otherwise,
     *                                      clear internal list of exposed attributes.
     * @return {@literal true}, if adapter is previously started; otherwise, {@literal false}.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public boolean stop(final boolean saveAttributes);

    /**
     * Exposes management attributes.
     * @param connector The management connector that provides access to the specified attributes.
     * @param namespace The attributes namespace.
     * @param attributes The dictionary of attributes.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public void exposeAttributes(final ManagementConnector connector, final String namespace, final Map<String, AttributeConfiguration> attributes);

    /**
     * Exposes monitoring events.
     * @param connector The management connector that provides notification listening and subscribing.
     * @param events The collection of configured notifications.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public void exposeEvents(final ManagementConnector connector, final Set<EventConfiguration> events);
}
