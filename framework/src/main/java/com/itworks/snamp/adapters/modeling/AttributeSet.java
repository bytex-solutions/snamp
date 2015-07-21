package com.itworks.snamp.adapters.modeling;

import com.itworks.snamp.internal.RecordReader;

/**
 * Represents reader for a set of attributes stored inside of the resource adapter.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface AttributeSet<TAccessor extends AttributeAccessor> {
    /**
     * Reads all attributes sequentially.
     * @param attributeReader An object that accepts attribute and its resource.
     * @param <E> Type of the exception that may be produced by reader.
     * @throws E Unable to process attribute.
     */
    <E extends Exception> void forEachAttribute(final RecordReader<String, ? super TAccessor, E> attributeReader) throws E;
}
