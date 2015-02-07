package com.itworks.snamp.jmx;

import javax.management.Attribute;
import javax.management.AttributeList;
import java.util.Map;

/**
 * Provides various method for working with {@link javax.management.AttributeList}.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class AttributeListUtils {
    private AttributeListUtils(){

    }

    public static AttributeList create(final Map<String, ?> attributes){
        final AttributeList result = new AttributeList(attributes.size() + 5);
        for(final Map.Entry<String, ?> entry: attributes.entrySet())
            result.add(new Attribute(entry.getKey(), entry.getValue()));
        return result;
    }
}
