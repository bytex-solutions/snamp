package com.itworks.snamp.licensing;

import com.itworks.snamp.Descriptive;

/**
 * Represents license limitations.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public interface LicenseLimitations extends Iterable<String> {
    /**
     * Represents a limitation for the specified licensed object.
     * @param <T>
     * @since 1.0
     * @version 1.0
     */
    public static interface Limitation<T> extends Descriptive{
        /**
         * Creates a new licensing exception.
         * @return A new instance of the licensing exception.
         */
        LicensingException createException();

        /**
         * Determines whether the specified actual value doesn't break this limitation.
         * @param actualValue The actual value to check.
         * @return {@literal true}, if the specified actual value doesn't break this limitation; otherwise, {@literal false}.
         */
        boolean validate(final T actualValue);
    }

    /**
     * Throws an {@link LicensingException} if the specified license limitation fails.
     * @param limitationName
     * @param actualValue The actual value of the restricted parameter.
     * @param <T> Type of the restricted parameter.
     */
    <T> void verify(final String limitationName, final T actualValue) throws LicensingException;

    /**
     * Returns the restricted parameter by its name.
     * @param limitationName The name of the restricted parameter.
     * @return An instance of the limitation descriptor.
     */
    Limitation<?> getLimitation(final String limitationName);
}
