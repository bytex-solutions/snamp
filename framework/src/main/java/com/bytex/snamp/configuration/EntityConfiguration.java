package com.bytex.snamp.configuration;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Represents a root interface for all agent configuration entities.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public interface EntityConfiguration extends Map<String, String> {
    /**
     * The name of the parameter which contains description of the configuration entity.
     */
    String DESCRIPTION_KEY = "description";

    default void setDescription(final String value){
        put(DESCRIPTION_KEY, value);
    }

    default String getDescription(){
        return get(DESCRIPTION_KEY);
    }

    default void load(final Map<String, String> parameters){
        clear();
        putAll(parameters);
    }

    /**
     * Iterates through each configuration parameter and replace pattern {paramName} with actual parameter value.
     * <p>
     *     For example, <pre>param1={param2}; param2=hello</pre> will be replaced as <pre>param1=hello; param2=hello</pre>
     */
    default void expandParameters(){
        for(final Entry<String, String> entry: entrySet()) {
            final Pattern pattern = Pattern.compile('{' + entry.getKey() + '}', Pattern.LITERAL);
            replaceAll((k, v) -> pattern.matcher(v).replaceAll(entry.getValue()));
        }
    }
}
