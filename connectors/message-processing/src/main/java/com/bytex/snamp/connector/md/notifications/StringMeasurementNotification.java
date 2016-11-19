package com.bytex.snamp.connector.md.notifications;

import com.bytex.snamp.instrumentation.StringMeasurement;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Represents measurement of {@link String} data type.
 * @since 2.0
 * @version 2.0
 */
public final class StringMeasurementNotification extends ValueMeasurementNotification<StringMeasurement> implements Supplier<String>, UnaryOperator<String> {
    private static final long serialVersionUID = -3133093532382654999L;

    public StringMeasurementNotification(Object source, StringMeasurement measurement) {
        super(source, measurement, "String supplied");
    }

    @Override
    public String apply(final String existingValue) {
        return getMeasurement().getValue(existingValue);
    }

    @Override
    public String get() {
        return getMeasurement().getValue();
    }
}
