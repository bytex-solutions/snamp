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
