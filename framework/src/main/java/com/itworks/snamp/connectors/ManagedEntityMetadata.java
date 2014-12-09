package com.itworks.snamp.connectors;

import com.itworks.snamp.Descriptive;

import java.util.*;

/**
 * Represents a root interface for all management entities, such as management attributes, notifications and etc.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ManagedEntityMetadata extends Map<String, String>, Descriptive {
    /**
     * The name of the optional metadata property that provides a name of the attribute type.
     */
    String TYPE_NAME = "typeName";

    /**
     * The name of the optional metadata property that provides a description of the attribute type.
     */
    String TYPE_DESCRIPTION = "typeDescription";

    /**
     * The name of the optional metadata property that contains pattern for column description.
     */
    String COLUMN_DESCRIPTION = "column.%s.description";
}
