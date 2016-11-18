package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.Convert;
import com.bytex.snamp.connector.attributes.AbstractAttributeInfo;
import com.bytex.snamp.connector.attributes.AbstractOpenAttributeInfo;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.gateway.modeling.AttributeAccessor;
import com.bytex.snamp.jmx.CompositeDataUtils;
import com.bytex.snamp.jmx.WellKnownType;
import com.google.common.collect.ImmutableMap;
import org.influxdb.dto.Point;

import javax.management.*;
import javax.management.openmbean.CompositeData;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Represents attribute as point of measurement that can be stored in InfluxDB.
 * @since 2.0
 * @version 2.0
 */
final class AttributePoint extends AttributeAccessor {
    AttributePoint(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    @SuppressWarnings("unchecked")
    Point toPoint(final Map<String, String> tags) throws MBeanException, AttributeNotFoundException, ReflectionException, InvalidAttributeValueException {
        final String VALUE_FIELD = "value";
        final WellKnownType type;
        final Map fields;
        switch (type = getType()) {
            case DATE:
                fields = ImmutableMap.<String, Object>of(VALUE_FIELD, Convert.toDate(getValue()).getTime());
                break;
            case CHAR:
                fields = ImmutableMap.<String, Object>of(VALUE_FIELD, String.valueOf(getValue()));
                break;
            case DICTIONARY:
                fields = CompositeDataUtils.toMap(getValue(CompositeData.class));
                break;
            default:
                if (type.isPrimitive())
                    fields = ImmutableMap.of(VALUE_FIELD, getValue());
                else
                    throw new UnsupportedAttributeTypeException(getRawType());
        }
        //create point
        return Point
                .measurement(getName())
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag(tags)
                .fields(fields)
                .build();
    }
}
