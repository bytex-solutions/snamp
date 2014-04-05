package com.itworks.snamp.core.maintenance;

import java.util.*;
import java.util.concurrent.Future;

/**
 * Represents generic interface for all maintainable objects.
 * <p>
 *     Maintainable means that the human can interacts with this object using
 *     maintenance actions.
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see AbstractMaintainable
 */
public interface Maintainable {
    /**
     * Returns read-only map of maintenance actions.
     * @return Read-only map of maintenance action,
     */
    public Set<String> getActions();

    /**
     * Returns human-readable description of the specified maintenance action that
     * includes description of the arguments string.
     * @param actionName The name of the maintenance action.
     * @param loc Target locale of the action description.
     * @return Localized description of the action.
     */
    public String getActionDescription(final String actionName, final Locale loc);

    /**
     * Invokes maintenance action.
     * @param actionName The name of the action to invoke.
     * @param arguments The action invocation command line. May be {@literal null} or empty for parameterless
     *                  action.
     * @param loc Localization of the action arguments string and invocation result.
     * @return The localized result of the action invocation; or {@literal null}, if the specified
     * action doesn't exist.
     */
    public Future<String> doAction(final String actionName, final String arguments, final Locale loc);
}
