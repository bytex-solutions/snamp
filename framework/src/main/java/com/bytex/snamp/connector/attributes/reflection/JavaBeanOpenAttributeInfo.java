package com.bytex.snamp.connector.attributes.reflection;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.jmx.WellKnownType;

import javax.management.ReflectionException;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenType;
import java.beans.PropertyDescriptor;
import java.util.Set;

/**
 * Represents OpenType-enabled attribute declared as a Java property.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public class JavaBeanOpenAttributeInfo extends JavaBeanAttributeInfo implements OpenMBeanAttributeInfo {
    private static final long serialVersionUID = -4173983412042130772L;
    private final OpenType<?> openType;

    public JavaBeanOpenAttributeInfo(final String attributeName,
                                     final PropertyDescriptor property,
                                     final AttributeDescriptor descriptor) throws ReflectionException, OpenDataException {
        super(attributeName, property, descriptor);
        OpenType<?> type = formatter.getOpenType();
        //tries to detect open type via WellKnownType
        if (type == null) {
            final WellKnownType knownType = WellKnownType.getType(property.getPropertyType());
            if (knownType != null && knownType.isOpenType())
                type = knownType.getOpenType();
            else throw new OpenDataException();
        }
        this.openType = type;
    }

    @Override
    public final OpenType<?> getOpenType() {
        return openType;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public Set<?> getLegalValues() {
        return null;
    }

    @Override
    public Comparable<?> getMinValue() {
        return null;
    }

    @Override
    public Comparable<?> getMaxValue() {
        return null;
    }

    @Override
    public boolean hasDefaultValue() {
        return false;
    }

    @Override
    public boolean hasLegalValues() {
        return false;
    }

    @Override
    public boolean hasMinValue() {
        return false;
    }

    @Override
    public boolean hasMaxValue() {
        return false;
    }

    @Override
    public final boolean isValue(final Object obj) {
        return openType.isValue(obj);
    }
}
