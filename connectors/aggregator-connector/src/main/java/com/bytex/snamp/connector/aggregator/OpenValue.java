package com.bytex.snamp.connector.aggregator;

import javax.management.openmbean.OpenType;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class OpenValue<T> {
    private final T value;
    private final OpenType<T> type;

    OpenValue(final T value, final OpenType<T> type){
        this.value = value;
        this.type = type;
    }

    T getValue(){
        return value;
    }

    OpenType<T> getType(){
        return type;
    }
}
