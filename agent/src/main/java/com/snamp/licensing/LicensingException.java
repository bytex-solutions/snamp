package com.snamp.licensing;

/**
 * Represents exception that is occurred during license limitation validation.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class LicensingException extends SecurityException {
    /**
     * Initializes a new licensing exception.
     * @param message
     */
    public LicensingException(final String message){
        super(message);
    }
}
