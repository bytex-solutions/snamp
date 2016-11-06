package com.bytex.snamp.connector.attributes.reflection;

import com.bytex.snamp.configuration.AttributeConfiguration;
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
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.bytex.snamp.configuration.ConfigurationManager.createEntityConfiguration;

/**
 * Represents repository of attributes reflected from JavaBean properties.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class JavaBeanAttributeRepository extends AbstractAttributeRepository<JavaBeanAttributeInfo> {
    private final Object owner;

    /**
     * Initializes a new repository of attributes reflected from JavaBean properties.
     * @param resourceName Resource name.
     * @param owner JavaBean instance. Cannot be {@literal null}.
     */
    protected JavaBeanAttributeRepository(final String resourceName, final Object owner){
        super(resourceName, JavaBeanAttributeInfo.class, true);
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
            if (Objects.equals(property.getName(), descriptor.getName(attributeName)))
                return createAttribute(attributeName, property, descriptor);

        throw JMExceptionUtils.attributeNotFound(descriptor.getName(attributeName));
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
    public Collection<JavaBeanAttributeInfo> expandAttributes() {
        return getProperties().stream()
                .map(property -> {
                    final AttributeConfiguration config = createEntityConfiguration(getClassLoader(), AttributeConfiguration.class);
                    assert config != null;
                    config.setAlternativeName(property.getName());
                    config.setAutomaticallyAdded(true);
                    config.setReadWriteTimeout(AttributeConfiguration.TIMEOUT_FOR_SMART_MODE);
                    return addAttribute(property.getName(), new AttributeDescriptor(config));
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static JavaBeanAttributeRepository create(final String resourceName,
                                                     final ManagedResourceConnector connector,
                                                     final BeanInfo connectorInfo){
        final ImmutableList<PropertyDescriptor> properties = ImmutableList.copyOf(connectorInfo.getPropertyDescriptors());

        return new JavaBeanAttributeRepository(resourceName, connector) {
            @Override
            protected ImmutableList<PropertyDescriptor> getProperties() {
                return properties;
            }

            private Logger getLogger() {
                return ((ManagedResourceConnector) super.owner).getLogger();
            }

            @Override
            protected void failedToConnectAttribute(final String attributeName, final Exception e) {
                failedToConnectAttribute(getLogger(), Level.SEVERE, attributeName, e);
            }

            @Override
            protected void failedToGetAttribute(final String attributeID, final Exception e) {
                failedToGetAttribute(getLogger(), Level.SEVERE, attributeID, e);
            }

            @Override
            protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
                failedToSetAttribute(getLogger(), Level.SEVERE, attributeID, value, e);
            }
        };
    }
}