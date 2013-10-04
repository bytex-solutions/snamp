package com.snamp.connectors;

import java.util.*;

/**
 * Represents dictionary type of the attribute value.
 * @author roman
 */
public interface AttributeDictionaryType extends AttributeTypeInfo {
    /**
     * Gets a set of dictionary keys (items).
     * @return A set of dictionary keys (items).
     */
    public Set<String> getItems();

    /**
     * Returns the item type.
     * @param itemName The item name.
     * @return The type descriptor; or {@literal null} if the specified item doesn't exist.
     */
    public AttributeTypeInfo getItemType(final String itemName);
}
