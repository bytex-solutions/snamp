package com.bytex.snamp.connectors.mda.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import javax.management.openmbean.OpenDataException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface HttpValueParser {

    Object getDefaultValue();

    Object deserialize(final JsonElement value, final Gson formatter) throws OpenDataException;

    JsonElement serialize(final Object value, final Gson formatter);
}
