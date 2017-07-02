package com.bytex.snamp.gateway.modeling;

import com.bytex.snamp.jmx.JMExceptionUtils;

import javax.management.*;

/**
 * Represents a collection of managed resource attributes.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class ResourceAttributeList<TAccessor extends AttributeAccessor> extends ResourceFeatureList<MBeanAttributeInfo, TAccessor> {
    private static final long serialVersionUID = 8660062708813935948L;

    public ResourceAttributeList(){
        super(10);
    }

    public final Object getAttribute(final String attributeName) throws MBeanException, AttributeNotFoundException, ReflectionException {
        if(containsKey(attributeName)){
            final TAccessor accessor = get(attributeName);
            return accessor.getValue();
        }
        else throw JMExceptionUtils.attributeNotFound(attributeName);
    }

    public final void setAttribute(final String attributeName, final Object value) throws AttributeNotFoundException, MBeanException, ReflectionException, InvalidAttributeValueException {
        if(containsKey(attributeName)){
            final TAccessor accessor = get(attributeName);
            accessor.setValue(value);
        }
        else throw JMExceptionUtils.attributeNotFound(attributeName);
    }

    @Override
    protected String getKey(final MBeanAttributeInfo feature) {
        return feature.getName();
    }
}
