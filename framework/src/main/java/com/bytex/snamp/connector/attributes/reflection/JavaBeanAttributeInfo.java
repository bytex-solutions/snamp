package com.bytex.snamp.connector.attributes.reflection;

import com.bytex.snamp.connector.attributes.AbstractAttributeInfo;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeDescriptorRead;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;

import javax.management.InvalidAttributeValueException;
import javax.management.ReflectionException;
import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Objects;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * Represents an attribute declared as a Java property.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
public class JavaBeanAttributeInfo extends AbstractAttributeInfo implements AttributeDescriptorRead {
    private static final long serialVersionUID = -5047097712279607039L;
    private final MethodHandle getter;
    private final MethodHandle setter;

    /**
     * Represents attribute formatter.
     */
    protected final ManagementAttributeMarshaller formatter;

    public JavaBeanAttributeInfo(final String attributeName,
                                 final PropertyDescriptor property,
                                 final AttributeDescriptor descriptor) throws ReflectionException {
        super(attributeName,
                property.getPropertyType(),
                getDescription(property, descriptor),
                getSpecifier(property),
                descriptor);
        final Method getter = property.getReadMethod();
        final Method setter = property.getWriteMethod();
        final ManagementAttribute info = getAdditionalInfo(getter, setter);
        if(info != null)
            this.formatter = callAndWrapException(() -> {
                final Class<? extends ManagementAttributeMarshaller> formatterClass =
                        info.marshaller();
                return Objects.equals(formatterClass, DefaultManagementAttributeMarshaller.class) ?
                        new DefaultManagementAttributeMarshaller():
                        formatterClass.newInstance();
            }, ReflectionException::new);
        else
            throw new ReflectionException(new IllegalArgumentException(String.format("Property '%s' is not marked with annotation ManagementAttribute", property)));
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        try{

            this.setter = setter != null ? lookup.unreflect(setter): null;
            this.getter = getter != null ? lookup.unreflect(getter): null;
        } catch (final ReflectiveOperationException e) {
            throw new ReflectionException(e);
        }
    }

    /**
     * Gets value of this attribute.
     * @param owner JavaBean instance which provides access to property reflected by this attribute.
     * @return Value of this attribute.
     * @throws ReflectionException Unable to reflect getter.
     */
    public final Object getValue(final Object owner) throws ReflectionException {
        if (getter != null){
            final Object value;
            try {
                value = getter.invoke(owner);
            } catch (final Exception e) {
                throw new ReflectionException(e);
            } catch (final Error e){
                throw e;
            } catch (final Throwable e){
                throw new InternalError(e);
            }
            return formatter.toJmxValue(value, this);
        }
        else
            throw new ReflectionException(new UnsupportedOperationException("Attribute is write-only"));
    }

    /**
     * Changes value of this attribute.
     * @param owner JavaBean instance which provides access to property reflected by this attribute.
     * @param value New value.
     * @throws ReflectionException Unable to reflect setter.
     * @throws InvalidAttributeValueException Invalid type of value.
     */
    @SuppressWarnings("unchecked")
    public final void setValue(final Object owner, Object value) throws ReflectionException, InvalidAttributeValueException {
        if (setter != null) {
            value = formatter.fromJmxValue(value, this);
            try {
                setter.invoke(owner, value);
            } catch (final ClassCastException e){
                throw new InvalidAttributeValueException(e.getMessage());
            } catch (final Exception e) {
                throw new ReflectionException(e);
            } catch (final Error e) {
                throw e;
            } catch (final Throwable e) {
                throw new InternalError(e);
            }
        } else throw new ReflectionException(new UnsupportedOperationException("Attribute is read-only"));
    }

    private static AttributeSpecifier getSpecifier(final PropertyDescriptor descriptor){
        AttributeSpecifier result = AttributeSpecifier.NOT_ACCESSIBLE;
        if(descriptor.getReadMethod() != null){
            result = result.readable(true);
            result = descriptor.getReadMethod().getName().startsWith("is") ?
                    result.flag(true):
                    result;
        }
        result = result.writable(descriptor.getWriteMethod() != null);
        return result;
    }

    private static ManagementAttribute getAdditionalInfo(final Method... methods){
        for(final Method m: methods)
            if(m != null && m.isAnnotationPresent(ManagementAttribute.class))
                return m.getAnnotation(ManagementAttribute.class);
        return null;
    }

    public static boolean isValidDescriptor(final PropertyDescriptor descriptor){
        return getAdditionalInfo(descriptor.getWriteMethod(), descriptor.getReadMethod()) != null;
    }

    private static String getDescription(final PropertyDescriptor property,
                                         final AttributeDescriptor descriptor) {
        String description = descriptor.getDescription();
        if (description == null || description.isEmpty()) {
            final ManagementAttribute attr = getAdditionalInfo(property.getReadMethod(), property.getWriteMethod());
            description = attr != null ? attr.description() : null;
            if (description == null || description.isEmpty())
                description = property.getName();
        }
        return description;
    }
}
