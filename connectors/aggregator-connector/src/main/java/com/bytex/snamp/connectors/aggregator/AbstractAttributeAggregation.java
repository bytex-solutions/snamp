package com.bytex.snamp.connectors.aggregator;

import com.bytex.snamp.connectors.ManagedResourceConnectorClient;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.connectors.attributes.AttributeSpecifier;
import com.bytex.snamp.connectors.attributes.OpenAttributeAccessor;
import com.bytex.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

import javax.management.*;
import javax.management.openmbean.OpenType;

/**
 * An abstract class for all aggregations.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractAttributeAggregation<T> extends OpenAttributeAccessor<T> {
    private static final long serialVersionUID = -3564884715121017964L;
    private final String source;

    protected AbstractAttributeAggregation(final String attributeID,
                                           final String description,
                                           final OpenType<T> attributeType,
                                           final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameterException {
        super(attributeID,
                description,
                attributeType,
                AttributeSpecifier.READ_ONLY,
                descriptor);
        source = AggregatorConnectorConfiguration.getSourceManagedResource(descriptor);
    }

    protected static ManagedResourceConnectorClient getResource(final AttributeDescriptor descriptor,
                                                                      final BundleContext context) throws AbsentAggregatorAttributeParameterException, InstanceNotFoundException {
        return new ManagedResourceConnectorClient(context, AggregatorConnectorConfiguration.getSourceManagedResource(descriptor));
    }

    /**
     * The name of the managed resource used as a source for attributes used in this aggregation.
     *
     * @return The name of the managed resource.
     */
    public final String getResourceName() {
        return source;
    }

    /**
     * Sets value of this attribute.
     *
     * @param value The value of this attribute.
     * @throws Exception Unable to write attribute value.
     */
    @Override
    protected final void setValue(final T value) throws Exception {
        throw new UnsupportedOperationException(String.format("Attribute %s is read-only", getName()));
    }

    protected abstract T compute(final DynamicMBean attributeSupport) throws Exception;

    private T compute(final BundleContext context) throws JMException {
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(context, getResourceName());
        try{
            return compute(client.get());
        } catch (final UnsupportedOperationException e){
            throw new ReflectionException(e);
        } catch (final Exception e) {
            throw new MBeanException(e);
        } finally {
            client.release(context);
        }
    }

    protected final BundleContext getBundleContext(){
        return Utils.getBundleContextOfObject(this);
    }

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    @Override
    protected final T getValue() throws Exception {
        return compute(getBundleContext());
    }
}
