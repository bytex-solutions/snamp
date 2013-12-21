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


    /**
     * Executes remote action.
     * @param actionName The name of the action,
     * @param args The invocation arguments.
     * @param timeout The Invocation timeout.
     * @return The invocation result.
     */
    public Object doAction(final String actionName, final Arguments args, final TimeSpan timeout) throws UnsupportedOperationException, TimeoutException;
}
