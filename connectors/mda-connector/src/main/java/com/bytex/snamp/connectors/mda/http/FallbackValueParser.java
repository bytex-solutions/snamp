package com.bytex.snamp.connectors.mda.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class FallbackValueParser implements HttpValueParser {
    static final HttpValueParser INSTANCE = new FallbackValueParser();

    private FallbackValueParser(){

    }

    @Override
    public String getDefaultValue() {
        return "";
    }

    @Override
    public String deserialize(final JsonElement value, final Gson formatter) {
        return formatter.toJson(value);
    }

    @Override
    public JsonElement serialize(final Object value, final Gson formatter) {
        return formatter.fromJson(String.valueOf(value), JsonElement.class);
    }
}
