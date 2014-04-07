package com.itworks.snamp.internal;

import java.util.*;

/**
 * Represents parser of key-value pairs contained in the single string and separated by
 * the custom delimiter. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class KeyValueParser {
    private final String pairDelimiter;
    private final String keyValueDelimiter;

    /**
     * Initializes a new parser.
     * @param pairDelimiter Regexp that is used to separate key-value pairs. Cannot be {@literal null} or empty.
     * @param keyValueDelimiter Regexp that is used to separated key and value inside of the pair. Cannot be {@literal null} or empty,
     * @throws IllegalArgumentException pairDelimiter or keyValueDelimiter is {@literal null} or empty.
     */
    public KeyValueParser(final String pairDelimiter, final String keyValueDelimiter){
        if(pairDelimiter == null || pairDelimiter.isEmpty())
            throw new IllegalArgumentException("pairDelimiter is not specified.");
        else if(keyValueDelimiter == null || keyValueDelimiter.isEmpty())
            throw new IllegalArgumentException("keyValueDelimiter is not specified.");
        else {
            this.pairDelimiter = pairDelimiter;
            this.keyValueDelimiter = keyValueDelimiter;
        }
    }

    /**
     * Returns regexp that is used to separate pairs inside of a string.
     * @return Regexp that is used to separate pairs inside of a string.
     */
    public final String getPairDelimiter(){
        return pairDelimiter;
    }

    /**
     * Returns regexp that is used to separate key and value inside of the pair.
     * @return Regexp that is used to separate key and value inside of the pair.
     */
    public final String getKeyValueDelimiter(){
        return keyValueDelimiter;
    }

    /**
     * Splits the input string into key/value pairs.
     * @param input The input string to parse.
     * @return Key/value representation of the input string.
     */
    public final Properties parse(final String input){
        final Properties result = new Properties();
        if(input == null || input.isEmpty()) return result;
        //split string by pair delimiters
        final String[] pairs = input.split(pairDelimiter);
        //now split each pair to the keys/values
        for(String pair: pairs){
            pair = pair.trim();
            final String[] keyValue = pair.split(keyValueDelimiter);
            if(keyValue == null || keyValue.length < 2) continue;
            result.put(keyValue[0].trim(), keyValue[1].trim());
        }
        return result;
    }

    public final Map<String, String> parseAsMap(final String input){
        final Properties props = parse(input);
        final Map<String, String> result = new HashMap<>(props.size());
        for(final Object key: props.keySet())
            result.put(Objects.toString(key), Objects.toString(props.get(key)));
        return result;
    }
}
