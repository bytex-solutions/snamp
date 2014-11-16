package com.itworks.snamp;

/**
 * Represents at interface for processing objects (serializable, for example)
 * which support transitive custom data.
 * @param <D> Type of the transitive data associated with the processing object.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface UserDataSupport<D> {
    /**
     * Gets user data associated with this object.
     * @return The user data associated with this object.
     */
    D getUserData();

    /**
     * Sets the user data associated with this object.
     * @param value The user data to be associated with this object.
     */
    void setUserData(final D value);
}
