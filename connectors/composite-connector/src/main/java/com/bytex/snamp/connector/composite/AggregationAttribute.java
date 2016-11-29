package com.bytex.snamp.connector.composite;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSupport;
import com.bytex.snamp.connector.composite.functions.AggregationFunction;
import com.bytex.snamp.connector.composite.functions.NameResolver;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import java.util.Objects;

import static com.bytex.snamp.internal.Utils.callAndWrapException;

/**
 * Represents attribute with aggregation formula.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class AggregationAttribute extends ProcessingAttribute implements OpenMBeanAttributeInfo {
    private static final long serialVersionUID = 2597653763554514237L;
    private final AggregationFunction<?> function;
    private final NameResolver resolver;

    AggregationAttribute(final String name,
                         final AggregationFunction<?> function,
                         final NameResolver resolver,
                         final AttributeDescriptor descriptor){
        super(name, function.getReturnType(), function.toString(), true, false, false, descriptor);
        this.function = function;
        this.resolver = Objects.requireNonNull(resolver);
    }

    @Override
    Object getValue(final AttributeSupport support) throws ReflectionException, AttributeNotFoundException, MBeanException {
        final Object attributeValue = support.getAttribute(AttributeDescriptor.getName(this));
        return callAndWrapException(() -> function.invoke(resolver, attributeValue), ReflectionException::new);
    }
}
