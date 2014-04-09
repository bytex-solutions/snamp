package com.itworks.snamp.connectors;

import com.itworks.snamp.Aggregator;
import com.itworks.snamp.internal.InstanceLifecycle;
import com.itworks.snamp.internal.Lifecycle;

/**
 * Represents management connector that exposes management attributes of the remote provider.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Lifecycle(InstanceLifecycle.NORMAL)
public interface ManagementConnector extends AutoCloseable, Aggregator, AttributeSupport {
}
