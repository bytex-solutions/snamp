package com.itworks.snamp.adapters.rest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;

import java.util.concurrent.TimeoutException;

import static com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;

/**
 * Represents attribute of the managed resource accessible through REST service.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class HttpAttributeMapping {
    private final AttributeAccessor accessor;
    private final Gson jsonFormatter;


    HttpAttributeMapping(final AttributeAccessor accessor, final Gson jsonFormatter){
        this.accessor = accessor;
        this.jsonFormatter = jsonFormatter;
    }

    public JsonElement getValueAsJson() throws TimeoutException, AttributeSupportException{
        return JsonTypeSystem.toJson(accessor.getValue(), jsonFormatter);
    }

    public String getValue() throws TimeoutException, AttributeSupportException {
        return jsonFormatter.toJson(getValueAsJson());
    }



    public void setValue(final String value) throws TimeoutException, IllegalArgumentException, JsonSyntaxException, AttributeSupportException {
        accessor.setValue(JsonTypeSystem.fromJson(value, accessor.getType(), jsonFormatter));
    }
}
