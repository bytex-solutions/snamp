package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.adapters.AbstractResourceAdapter.AttributeAccessor;
import com.itworks.snamp.connectors.attributes.AttributeSupportException;

import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenType;
import java.util.concurrent.TimeoutException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxAttributeMapping implements JmxFeature<MBeanAttributeInfo> {
    private final AttributeAccessor accessor;
    private volatile OpenType<?> attributeType;
    private final boolean pureSerialization;

    JmxAttributeMapping(final AttributeAccessor accessor, final boolean pureSerialization){
        this.accessor = accessor;
        attributeType = null;
        this.pureSerialization = pureSerialization;
    }

    String getOriginalName(){
        return accessor.getName();
    }

    Object getValue() throws TimeoutException, OpenDataException, AttributeSupportException {
        return JmxTypeSystem.getValue(accessor.getValue(), accessor);
    }


    void setValue(final Object value) throws TimeoutException, OpenDataException, InvalidAttributeValueException, AttributeSupportException{
        if(!pureSerialization && JmxAdapterHelpers.isJmxCompliantAttribute(accessor))
            accessor.setValue(value);
        else accessor.setValue(JmxTypeSystem.parseValue(value, accessor.getType(), getAttributeType()));
    }

    OpenType<?> getAttributeType() throws OpenDataException{
        OpenType<?> type = attributeType;
        if(type == null)
            synchronized (this){
                type = attributeType;
                if(type == null)
                    type = attributeType = JmxTypeSystem.getType(accessor.getType(), accessor);
            }
        return type;
    }

    @Override
    public OpenMBeanAttributeInfoSupport createFeature(final String featureName) throws OpenDataException{
        String description = accessor.getDescription(null);
        if(description == null || description.isEmpty()) description = String.format("Description stub for %s attribute.", featureName);
        return new OpenMBeanAttributeInfoSupport(featureName,
                description,
                getAttributeType(),
                accessor.canRead(),
                accessor.canWrite(),
                featureName.indexOf("is") == 0);
    }
}
