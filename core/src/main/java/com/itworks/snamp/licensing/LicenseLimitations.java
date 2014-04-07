package com.itworks.snamp.licensing;

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
    public static interface Limitation<T>{
        /**
         * Creates a new licensing exception.
         * @return A new instance of the licensing exception.
         */
        public LicensingException createException();

        /**
         * Determines whether the specified actual value doesn't break this limitation.
         * @param actualValue The actual value to check.
         * @return {@literal true}, if the specified actual value doesn't break this limitation; otherwise, {@literal false}.
         */
        public boolean validate(final T actualValue);
    }

    /**
     * Throws an {@link LicensingException} if the specified license limitation fails.
     * @param limitationName
     * @param actualValue The actual value of the restricted parameter.
     * @param <T> Type of the restricted parameter.
     */
    public <T> void verify(final String limitationName, final T actualValue) throws LicensingException;

    /**
     * Returns the restricted parameter by its name.
     * @param limitationName The name of the restricted parameter.
     * @return An instance of the limitation descriptor.
     */
    public Limitation<?> getLimitation(final String limitationName);
}
