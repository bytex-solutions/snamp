package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.connector.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ReflectionException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents repository of Groovy-based attributes.
 */
final class GroovyAttributeRepository extends AbstractAttributeRepository<GroovyAttribute> {
    private final ManagedResourceScriptlet scriptlet;

    GroovyAttributeRepository(final String resourceName,
                              final ManagedResourceScriptlet scriptlet,
                              final boolean expandable) {
        super(resourceName, GroovyAttribute.class, expandable);
        this.scriptlet = Objects.requireNonNull(scriptlet);

    }

    @Override
    public Map<String, AttributeDescriptor> discoverAttributes() {
        final Map<String, AttributeDescriptor> result = new HashMap<>();
        for(final String attributeName: scriptlet.getAttributes())
            result.put(attributeName, createDescriptor());
        return result;
    }

    @Override
    protected GroovyAttribute connectAttribute(final String attributeName,
                                               final AttributeDescriptor descriptor) throws AttributeNotFoundException {
        return scriptlet.createAttribute(attributeName, descriptor);
    }


    @Override
    protected Object getAttribute(final GroovyAttribute metadata) throws ReflectionException, InvalidAttributeValueException {
        return metadata.getValue();
    }

    @Override
    protected void setAttribute(final GroovyAttribute attribute, final Object value) throws ReflectionException, InvalidAttributeValueException {
        attribute.setValue(value);
    }
}
