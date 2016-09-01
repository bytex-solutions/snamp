package com.bytex.snamp.connector.notifications.measurement;

import java.util.function.BooleanSupplier;

/**
 * Represents boolean state of a value.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class FlagChangedNotification extends InstantValueChangedNotification<Boolean> implements BooleanSupplier {
    private static final long serialVersionUID = 577196431273996014L;

    public FlagChangedNotification(final String componentName, final String instanceName, final String message, final boolean value) {
        super(componentName, instanceName, message, value);
    }

    /**
     * Gets a state.
     *
     * @return a state.
     */
    @Override
    public boolean getAsBoolean() {
        return value;
    }
}
