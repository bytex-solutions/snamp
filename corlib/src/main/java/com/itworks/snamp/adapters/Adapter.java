package com.itworks.snamp.adapters;

import com.itworks.snamp.core.FrameworkService;
import com.itworks.snamp.internal.*;

import java.io.IOException;
import java.util.*;

/**
 * Represents hosting adapter, that exposes management information to the outside world.
 * <p>
 *     The implementer should have parameterless constructor and annotated with {@link net.xeoh.plugins.base.annotations.PluginImplementation}.
 * </p>
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 * @see AttributePublisher
 * @see NotificationPublisher
 */
@Lifecycle(InstanceLifecycle.SINGLE_PER_PROCESS)
public interface Adapter extends AutoCloseable, FrameworkService, AttributePublisher {
    /**
     * Represents name of the port definition parameter.
     */
    static final String PORT_PARAM_NAME = "port";

    /**
     * Represents name of the hosting address definition parameter.
     */
    static final String ADDRESS_PARAM_NAME = "address";

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
