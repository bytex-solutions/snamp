package com.bytex.snamp.moa.watching;

import com.bytex.snamp.Stateful;
import com.bytex.snamp.supervision.GroupStatusDetails;

import java.util.Map;

/**
 * Represents component watcher.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ComponentWatcher extends Stateful {
    /**
     * Gets status of the component.
     * @return Status of the component.
     */
    GroupStatusDetails getStatus();

    /**
     * Gets map of attribute checkers where key represents attribute name.
     * @return Mutable map of attribute checkers.
     */
    Map<String, AttributeChecker> getAttributeCheckers();

    void addStatusEventListener(final ComponentStatusEventListener listener);

    void removeStatusEventListener(final ComponentStatusEventListener listener);
}
