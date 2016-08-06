package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.connectors.attributes.AbstractAttributeRepository;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import org.osgi.framework.BundleContext;

import javax.management.Attribute;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.openmbean.*;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static com.bytex.snamp.configuration.ManagedResourceConfiguration.AttributeConfiguration;

/**
 * Composes scalar attributes from the managed resource into a single vector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 2.0
 */
final class Composer extends AbstractAttributeAggregation<CompositeData> {
    static final String NAME = "composer";
    private static final String DESCRIPTION = "Composes all scalar attributes";
    private static final long serialVersionUID = 2877483846537647364L;


    private static CompositeType detectAttributeType(final AttributeDescriptor descriptor,
                                                     final BundleContext context) throws InstanceNotFoundException, AbsentAggregatorAttributeParameterException, OpenDataException {
        final ManagedResourceConnectorClient client = getResource(descriptor, context);
        try {
            return AbstractAttributeRepository.compose("ComposedAttributes",
                    "A set of composed attributes",
                    attributeType -> attributeType instanceof SimpleType<?>,
                    client.getMBeanInfo().getAttributes());
        }
        finally {
            client.release(context);
        }
    }

    Composer(final String attributeID,
                       final AttributeDescriptor descriptor,
                       final BundleContext context) throws AbsentAggregatorAttributeParameterException, JMException {
        super(attributeID, DESCRIPTION, detectAttributeType(descriptor, context), descriptor);
    }

    @Override
    protected CompositeDataSupport compute(final DynamicMBean attributeSupport) throws OpenDataException {
        final CompositeType attributeType = (CompositeType) getOpenType();
        final Collection<? extends Attribute> attributes = attributeSupport.getAttributes(attributeType.keySet().stream().toArray(String[]::new)).asList();
        final Map<String, Object> result = attributes.stream().collect(Collectors.toMap(Attribute::getName, Attribute::getValue));
        return new CompositeDataSupport(attributeType, result);
    }

    static AttributeConfiguration getConfiguration() {
        final AttributeConfiguration result = createAttributeConfiguration(Composer.class.getClassLoader());
        result.setAlternativeName(NAME);
        result.getParameters().put(AggregatorConnectorConfiguration.SOURCE_PARAM, "");
        return result;
    }
}
