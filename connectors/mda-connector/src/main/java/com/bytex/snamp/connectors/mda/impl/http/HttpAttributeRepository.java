package com.bytex.snamp.connectors.mda.impl.http;

import com.bytex.snamp.connectors.mda.impl.MDAAttributeRepository;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import javax.management.AttributeNotFoundException;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class HttpAttributeRepository extends MDAAttributeRepository {
    HttpAttributeRepository(final String resourceName, final Logger logger) {
        super(resourceName, logger);
    }

    JsonElement setAttribute(final String storageKey, final Gson formatter, final JsonElement value) throws AttributeNotFoundException, OpenDataException {
        final OpenType<?> attributeType = getAttributeType(storageKey);
        if(attributeType == null)
            throw JMExceptionUtils.attributeNotFound(storageKey);
        final Object result =
                getStorage().put(storageKey, JsonDataConverter.deserialize(formatter, attributeType, value));
        resetAccessTime();
        return JsonDataConverter.serialize(formatter, result);
    }

    JsonElement getAttribute(final String storageKey, final Gson formatter) throws AttributeNotFoundException{
        if(!getStorage().containsKey(storageKey))
            throw JMExceptionUtils.attributeNotFound(storageKey);
        return JsonDataConverter.serialize(formatter, getStorage().get(storageKey));
    }
}
