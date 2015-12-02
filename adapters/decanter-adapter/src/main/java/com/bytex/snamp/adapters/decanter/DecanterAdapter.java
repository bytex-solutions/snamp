package com.bytex.snamp.adapters.decanter;

import com.bytex.snamp.adapters.AbstractResourceAdapter;
import com.bytex.snamp.adapters.modeling.FeatureAccessor;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.event.EventAdmin;

import javax.management.MBeanFeatureInfo;
import java.util.Map;
import java.util.Objects;

/**
 * Represents Decanter Collector in the form of SNAMP Resource Adapter.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
final class DecanterAdapter extends AbstractResourceAdapter {
    private final EventAdmin eventAdmin;

    DecanterAdapter(final String instanceName,
                    final EventAdmin eventAdmin){
        super(instanceName);
        this.eventAdmin = Objects.requireNonNull(eventAdmin);
    }

    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName, final M feature) throws Exception {
        return null;
    }

    @Override
    protected Iterable<? extends FeatureAccessor<?>> removeAllFeatures(String resourceName) throws Exception {
        return null;
    }

    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(String resourceName, M feature) throws Exception {
        return null;
    }

    @Override
    protected void start(final Map<String, String> parameters) throws Exception {

    }

    @Override
    protected void stop() throws Exception {

    }
}
