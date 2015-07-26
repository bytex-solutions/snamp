package com.bytex.snamp.jmx;

import javax.management.openmbean.OpenType;
import java.util.Objects;

/**
* @author Roman Sakno
* @version 1.0
* @since 1.0
*/
final class CompositeTypeItem {
    private final String description;
    private final OpenType<?> type;

    CompositeTypeItem(final OpenType<?> type,
                      final String description){
        this.description = Objects.requireNonNull(description, "description is null.");
        this.type = Objects.requireNonNull(type, "type is null.");
    }

    String getDescription(){
        return description;
    }

    OpenType<?> getType(){
        return type;
    }
}
