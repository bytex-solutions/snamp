package com.itworks.snamp.connectors.aggregator;

import com.google.common.base.Splitter;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.InvalidKeyException;
import javax.management.openmbean.OpenType;
import java.util.Iterator;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class CompositeDataPath {
    private final Iterable<String> path;
    private static final char delimiter = '/';

    CompositeDataPath(final String pathSpec){
        path = Splitter.on(delimiter).omitEmptyStrings().trimResults().split(pathSpec);
    }

    @SuppressWarnings("unchecked")
    private static OpenValue<?> getField(final Iterator<String> navigator,
                                         final Object value,
                                         final OpenType<?> type) throws InvalidKeyException{
        if(!navigator.hasNext()) return new OpenValue(value, type);
        else if(value instanceof CompositeData){
            final String fieldName = navigator.next();
            final CompositeData dictionary = (CompositeData)value;
            if(dictionary.containsKey(fieldName))
                return getField(navigator, dictionary.get(fieldName), dictionary.getCompositeType().getType(fieldName));
        }
        throw new InvalidKeyException(String.format("Unable to decompose %s value", value));
    }

    Object getFieldValue(final CompositeData value) throws InvalidKeyException{
        return getField(value).getValue();
    }

    OpenValue<?> getField(final CompositeData value) throws InvalidKeyException{
        return getField(path.iterator(), value, value.getCompositeType());
    }
}
