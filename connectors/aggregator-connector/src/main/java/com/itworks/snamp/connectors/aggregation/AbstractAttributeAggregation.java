package com.itworks.snamp.connectors.aggregation;

import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.connectors.ManagedResourceConnector;
import com.itworks.snamp.connectors.ManagedResourceConnectorClient;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.*;

/**
 * An abstract class for all aggregations.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractAttributeAggregation<V> implements AttributeAggregation<V> {
    private final String source;

    protected AbstractAttributeAggregation(final AttributeDescriptor descriptor) throws AbsentAggregatorAttributeParameter {
        this.source = AggregatorConnectorConfigurationDescriptor.getSourceManagedResource(descriptor);
    }

    /**
     * The name of the managed resource used as a source for attributes used in this aggregation.
     *
     * @return The name of the managed resource.
     */
    @Override
    public final String getResourceName() {
        return source;
    }

    private ServiceReferenceHolder<ManagedResourceConnector> getResource(final BundleContext context) throws InstanceNotFoundException {
        final ServiceReference<ManagedResourceConnector> resourceRef =
                ManagedResourceConnectorClient.getResourceConnector(context, getResourceName());
        if(resourceRef != null)
            return new ServiceReferenceHolder<>(context, resourceRef);
        else throw new InstanceNotFoundException(String.format("Managed resource %s not found", getResourceName()));
    }

    protected abstract V compute(final DynamicMBean attributeSupport) throws Exception;

    @Override
    public final V compute(final BundleContext context) throws JMException {
        final ServiceReferenceHolder<ManagedResourceConnector> connector = getResource(context);
        try{
            return compute(connector.get());
        } catch (final UnsupportedOperationException e){
            throw new ReflectionException(e);
        } catch (final Exception e) {
            throw new MBeanException(e);
        } finally {
            connector.release(context);
        }
    }
}
