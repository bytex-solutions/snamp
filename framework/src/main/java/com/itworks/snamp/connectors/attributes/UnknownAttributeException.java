package com.itworks.snamp.connectors.attributes;

/**
 * Represents an exception indicating that the requested attribute doesn't exist.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class UnknownAttributeException extends Exception {
    /**
     * Identifier of the unknown requested attribute.
     */
    public final String attributeID;

    public UnknownAttributeException(final String attributeID){
        super(String.format("Attribute %s is not registered in the resource connector.", attributeID));
        this.attributeID = attributeID;
    }
}
