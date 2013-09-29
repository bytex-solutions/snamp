package com.snamp.connectors;

import java.util.Map;

/**
 * Represents attribute metadata,
 */
public interface AttributeMetadata extends Map<String, String> {

    /**
     * Returns the attribute name.
     * @return The attribute name.
     */
    public String getAttributeName();

    /**
     * Determines whether the value of this attribute can be obtained.
     * @return {@literal true}, if attribute value can be obtained; otherwise, {@literal false}.
     */
    public boolean canRead();

    /**
     * Determines whether the value of this attribute can be changed.
     * @return {@literal true}, if the attribute value can be changed; otherwise, {@literal false}.
     */
    public boolean canWrite();

    /**
     * Determines whether the value of the attribute can be cached after first reading
     * and supplied as real attribute value before first write.
     * @return {@literal true}, if the value of this attribute can be cached; otherwise, {@literal false}.
     */
    public boolean cacheable();

    /**
     * Returns the canonical name of the attribute type.
     * @return The canonical name of the attribute type.
     */
    public String getAttributeClassName();
}
