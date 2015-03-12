package com.itworks.jcommands.impl;

import com.google.common.reflect.TypeToken;

import java.util.List;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class TypeTokens {

    private TypeTokens(){

    }

    /**
     * Represents type of the {@link com.itworks.jcommands.impl.XmlParsingResultType#DICTIONARY} object.
     */
    public static final TypeToken<Map<String, ?>> DICTIONARY_TYPE_TOKEN = new TypeToken<Map<String, ?>>() {};

    /**
     * Represents type of the {@link com.itworks.jcommands.impl.XmlParsingResultType#TABLE} object.
     */
    public static final TypeToken<List<? extends Map<String, ?>>> TABLE_TYPE_TOKEN = new TypeToken<List<? extends Map<String, ?>>>(){};
}
