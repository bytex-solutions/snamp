package com.bytex.snamp.web.serviceModel;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.connector.StatefulManagedResourceTracker;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nonnull;
import java.util.Map;
import static com.bytex.snamp.internal.Utils.callUnchecked;

/**
 * Represents slim version of resource tracker without custom configuration.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public abstract class ManagedResourceTrackerSlim extends StatefulManagedResourceTracker<Map<String, String>> {
    protected ManagedResourceTrackerSlim() {
        super(new InternalState<>(ImmutableMap.of()));
    }

    public final void startTracking() throws Exception {
        update(ImmutableMap.of());
    }
}
