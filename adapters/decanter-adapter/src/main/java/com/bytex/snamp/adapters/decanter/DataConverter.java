package com.bytex.snamp.adapters.decanter;

import com.bytex.snamp.Switch;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.google.common.base.Function;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class DataConverter {
    private static final Function<CompositeData, Map<String, ?>> FROM_COMPOSITE_DATA = new Function<CompositeData, Map<String, ?>>() {
        @Override
        public Map<String, ?> apply(final CompositeData input) {
            return CompositeDataUtils.toMap(input);
        }
    };
    private static final Function<ObjectName, String> FROM_OBJECT_NAME = new Function<ObjectName, String>() {
        @Override
        public String apply(final ObjectName input) {
            return input.getCanonicalName();
        }
    };
    private DataConverter(){

    }

    static Object convertUserData(final Object value) {
        return new Switch<>()
                .instanceOf(CompositeData.class, FROM_COMPOSITE_DATA)
                .instanceOf(ObjectName.class, FROM_OBJECT_NAME)
                .otherwise(value)
                .apply(value);
    }
}
