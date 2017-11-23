package com.bytex.snamp.connector.attributes.reflection;

import com.bytex.snamp.connector.ManagedResourceConnector;
import com.bytex.snamp.connector.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.google.common.collect.ImmutableList;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ReflectionException;
import javax.management.openmbean.OpenDataException;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents repository of attributes reflected from JavaBean properties.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@Deprecated
public abstract class JavaBeanAttributeRepository extends AbstractAttributeRepository<JavaBeanAttributeInfo> {
    private final Object owner;

    /**
     * Initializes a new repository of attributes reflected from JavaBean properties.
     * @param resourceName Resource name.
     * @param owner JavaBean instance. Cannot be {@literal null}.
     */
    protected JavaBeanAttributeRepository(final String resourceName, final Object owner){
        super(resourceName, JavaBeanAttributeInfo.class);
        this.owner = Objects.requireNonNull(owner);
    }

    /**
     * Gets collection of reflected properties.
     * @return A collection of reflected properties.
     */
    protected abstract Collection<PropertyDescriptor> getProperties();

    private static JavaBeanAttributeInfo createAttribute(final String attributeName,
                                                         final PropertyDescriptor property,
                                                         final AttributeDescriptor descriptor) throws ReflectionException {
        try {
            //try to connect as Open Type attribute
            return new JavaBeanOpenAttributeInfo(attributeName, property, descriptor);
        } catch (final OpenDataException e) {
            //bean property type is not Open Type
            return new JavaBeanAttributeInfo(attributeName, property, descriptor);
        }
    }

    @Override
    protected JavaBeanAttributeInfo connectAttribute(final String attributeName,
                                                     final AttributeDescriptor descriptor) throws AttributeNotFoundException, ReflectionException {
        for (final PropertyDescriptor property : getProperties())
            if (Objects.equals(property.getName(), descriptor.getAlternativeName().orElse(attributeName)))
                return createAttribute(attributeName, property, descriptor);

        throw JMExceptionUtils.attributeNotFound(descriptor.getAlternativeName().orElse(attributeName));
    }

    /**
     * Obtains the value of a specific attribute of the managed resource.
     *
     * @param metadata The metadata of the attribute.
     * @return The value of the attribute retrieved.
     * @throws ReflectionException Internal connector error.
     */
    @Override
    protected final Object getAttribute(final JavaBeanAttributeInfo metadata) throws ReflectionException {
        return metadata.getValue(owner);
    }

    /**
     * Sets the value of a specific attribute of the managed resource.
     *
     * @param metadata The attribute of to set.
     * @param value     The value of the attribute.
     * @throws ReflectionException                      Internal connector error.
     * @throws InvalidAttributeValueException Incompatible attribute type.
     */
    @Override
    protected final void setAttribute(final JavaBeanAttributeInfo metadata, final Object value) throws ReflectionException, InvalidAttributeValueException {
        metadata.setValue(owner, value);
    }

    private ClassLoader getClassLoader() {
        return owner.getClass().getClassLoader();
    }

    /**
     * Obtains collection of attributes discovered through reflection.
     * @return A collection of attributes discovered through reflection.
     */
    @Override
    public Map<String, AttributeDescriptor> discoverAttributes() {
        final Map<String, AttributeDescriptor> result = new HashMap<>();
        for (final PropertyDescriptor property : getProperties())
            if (JavaBeanAttributeInfo.isValidDescriptor(property))
                result.put(property.getName(), createDescriptor());
        return result;
    }

    public static JavaBeanAttributeRepository create(final String resourceName,
                                                     final ManagedResourceConnector connector,
                                                     final BeanInfo connectorInfo) {
        final ImmutableList<PropertyDescriptor> properties = ImmutableList.copyOf(connectorInfo.getPropertyDescriptors());

        return new JavaBeanAttributeRepository(resourceName, connector) {
            @Override
            protected ImmutableList<PropertyDescriptor> getProperties() {
                return properties;
            }
        };
    }
}
