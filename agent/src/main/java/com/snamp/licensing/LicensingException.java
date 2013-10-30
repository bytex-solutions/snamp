package com.snamp.licensing;

/**
 * Represents exception that is occurred during license limitation validation.
 * @author roman
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
