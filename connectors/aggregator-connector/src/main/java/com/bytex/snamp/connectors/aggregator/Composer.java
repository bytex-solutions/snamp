package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.configuration.SerializableAgentConfiguration.SerializableManagedResourceConfiguration.SerializableAttributeConfiguration;
import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.connectors.attributes.AbstractAttributeSupport;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.osgi.framework.BundleContext;

import javax.management.Attribute;
import javax.management.DynamicMBean;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.openmbean.*;
import java.util.Collection;
import java.util.Map;

/**
 * Composes scalar attributes from the managed resource into a single vector.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
final class Composer extends AbstractAttributeAggregation<CompositeData> {
    static final String NAME = "composer";
    private static final String DESCRIPTION = "Composes all scalar attributes";
    private static final Predicate<OpenType<?>> TYPE_FILTER = new Predicate<OpenType<?>>() {
        @Override
        public boolean apply(final OpenType<?> attributeType) {
            return attributeType instanceof SimpleType<?>;
        }
    };


    private static CompositeType detectAttributeType(final AttributeDescriptor descriptor,
                                                     final BundleContext context) throws InstanceNotFoundException, AbsentAggregatorAttributeParameterException, OpenDataException {
        final ManagedResourceConnectorClient client = getResource(descriptor, context);
        try {
            return AbstractAttributeSupport.compose("ComposedAttributes",
                    "A set of composed attributes",
                    TYPE_FILTER,
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
        final CompositeType attributeType = (CompositeType)getOpenType();
        final Collection<? extends Attribute> attributes = attributeSupport.getAttributes(ArrayUtils.toArray(attributeType.keySet(), String.class)).asList();
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(attributes.size());
        for(final Attribute attr: attributes)
            result.put(attr.getName(), attr.getValue());
        return new CompositeDataSupport(attributeType, result);
    }

    static SerializableAttributeConfiguration getConfiguration() {
        final SerializableAttributeConfiguration result = new SerializableAttributeConfiguration(NAME);
        result.getParameters().put(AggregatorConnectorConfiguration.SOURCE_PARAM, "");
        return result;
    }
}