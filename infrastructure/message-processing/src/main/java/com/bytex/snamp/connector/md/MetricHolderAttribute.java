package com.bytex.snamp.connector.md;

import com.bytex.snamp.concurrent.Timeout;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.attributes.AttributeSpecifier;
import com.bytex.snamp.connector.metrics.Metric;
import org.osgi.framework.BundleContext;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import java.time.Duration;
import java.util.concurrent.ConcurrentMap;
import static com.bytex.snamp.core.DistributedServices.isActiveNode;
import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents a holder for metric.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
abstract class MetricHolderAttribute<M extends Metric> extends MessageDrivenAttribute {
    private static final long serialVersionUID = 2645456225474793148L;
    private final Timeout timeout;

    MetricHolderAttribute(final String name,
                          final CompositeType type,
                          final String description,
                          final Duration synchronizationPeriod,
                          final AttributeDescriptor descriptor) {
        super(name, type, description, AttributeSpecifier.READ_ONLY, descriptor);
        timeout = new Timeout(synchronizationPeriod);
    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    private void updateMetric(final ConcurrentMap<String, Object> storage){
        if(isActiveNode(getBundleContext())){
            //overwrite internal state in the map

        }
    }

    @Override
    CompositeData getValue(final ConcurrentMap<String, Object> storage) throws Exception {
        timeout.acceptIfExpired(this, storage, MetricHolderAttribute::updateMetric);
        return null;
    }

    abstract M getMetric();

    abstract boolean setMetric(final Metric value);
}
