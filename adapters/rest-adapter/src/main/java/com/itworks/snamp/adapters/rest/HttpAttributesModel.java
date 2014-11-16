package com.itworks.snamp.adapters.rest;

import com.google.gson.Gson;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface HttpAttributesModel extends Map<String, HttpAttributeMapping> {
    Gson getJsonFormatter();

    HttpAttributeMapping get(final String resourceName, final String userDefinedAttributeName);
}
