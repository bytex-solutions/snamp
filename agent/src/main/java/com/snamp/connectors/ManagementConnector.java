package com.snamp.connectors;

import com.snamp.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Represents management connector that exposes management attributes of the remote provider.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@Lifecycle(InstanceLifecycle.NORMAL)
public interface ManagementConnector extends AutoCloseable, Aggregator, AttributeSupport {
}
