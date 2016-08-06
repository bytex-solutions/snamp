package com.bytex.snamp.adapters.decanter;

import com.bytex.snamp.Switch;
import com.bytex.snamp.jmx.CompositeDataUtils;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class DataConverter {
    private DataConverter(){

    }

    static Object convertUserData(final Object value) {
        return new Switch<>()
                .instanceOf(CompositeData.class, CompositeDataUtils::toMap)
                .instanceOf(ObjectName.class, ObjectName::getCanonicalName)
                .otherwise(value)
                .apply(value);
    }
}
