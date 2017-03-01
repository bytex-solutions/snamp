package com.bytex.snamp.connector.supervision.triggers;

import com.bytex.snamp.connector.supervision.GroupStatusEventListener;

/**
 * Represents trigger used to handle health status.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface GroupStatusTrigger extends GroupStatusEventListener {
    GroupStatusTrigger NO_OP = event -> {};
}
