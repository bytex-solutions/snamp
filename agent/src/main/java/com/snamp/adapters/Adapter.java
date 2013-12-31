package com.snamp.adapters;

import com.snamp.*;
import com.snamp.connectors.AttributeSupport;
import com.snamp.connectors.ManagementConnector;
import com.snamp.connectors.NotificationSupport;

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
 * @see com.snamp.adapters.AttributePublisher
 * @see com.snamp.adapters.NotificationPublisher
 */
@Lifecycle(InstanceLifecycle.SINGLE_PER_PROCESS)
public interface Adapter extends AutoCloseable, PlatformPlugin, AttributePublisher {
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
    @ThreadSafety(value = MethodThreadSafety.THREAD_UNSAFE, advice = SynchronizationType.EXCLUSIVE_LOCK)
    public boolean start(final Map<String, String> parameters) throws IOException;

    /**
     * Stops the connector hosting.
     * @param saveState {@literal true} to save previously exposed management entities for future reuse; otherwise,
     *                                      clear internal list of exposed management entities.
     * @return {@literal true}, if adapter is previously started; otherwise, {@literal false}.
     */
    @ThreadSafety(MethodThreadSafety.THREAD_UNSAFE)
    public boolean stop(final boolean saveState);


}
