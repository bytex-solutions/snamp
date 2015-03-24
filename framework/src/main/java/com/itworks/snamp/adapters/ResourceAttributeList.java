package com.itworks.snamp.adapters;

import javax.management.MBeanAttributeInfo;

/**
 * Represents a collection of managed resource attributes.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class ResourceAttributeList<TAccessor extends AttributeAccessor> extends ResourceFeatureList<MBeanAttributeInfo, TAccessor> {
    private static final long serialVersionUID = 8660062708813935948L;

    public ResourceAttributeList(){
        super(10);
    }

    @Override
    protected String getKey(final MBeanAttributeInfo feature) {
        return feature.getName();
    }
}
