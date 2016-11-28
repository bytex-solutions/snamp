package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.connector.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ReflectionException;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents repository of Groovy-based attributes.
 */
final class GroovyAttributeRepository extends AbstractAttributeRepository<GroovyAttribute> {
    private final ManagedResourceScriptlet scriptlet;

    GroovyAttributeRepository(final String resourceName,
                              final ManagedResourceScriptlet scriptlet) {
        super(resourceName, GroovyAttribute.class, true);
        this.scriptlet = Objects.requireNonNull(scriptlet);

    }

    @Override
    public Collection<? extends GroovyAttribute> expandAttributes() {
        return scriptlet.expandAttributes();
    }

    @Override
    protected GroovyAttribute connectAttribute(final String attributeName,
                                               final AttributeDescriptor descriptor) throws AttributeNotFoundException {
        return scriptlet.createAttribute(attributeName, descriptor);
    }

    @Override
    protected void failedToConnectAttribute(final String attributeName, final Exception e) {
        failedToConnectAttribute(scriptlet.getLogger(), Level.SEVERE, attributeName, e);
    }

    @Override
    protected Object getAttribute(final GroovyAttribute metadata) throws ReflectionException, InvalidAttributeValueException {
        return metadata.getValue();
    }

    @Override
    protected void failedToGetAttribute(final String attributeName, final Exception e) {
        failedToGetAttribute(scriptlet.getLogger(), Level.SEVERE, attributeName, e);
    }

    @Override
    protected void setAttribute(final GroovyAttribute attribute, final Object value) throws ReflectionException, InvalidAttributeValueException {
        attribute.setValue(value);
    }

    @Override
    protected void failedToSetAttribute(final String attributeID, final Object value, final Exception e) {
        failedToSetAttribute(scriptlet.getLogger(), Level.SEVERE, attributeID, value, e);
    }
}
