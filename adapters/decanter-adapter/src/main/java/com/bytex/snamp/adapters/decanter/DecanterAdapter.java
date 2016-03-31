package com.bytex.snamp.adapters.decanter;

import com.bytex.snamp.adapters.AbstractResourceAdapter;
import com.bytex.snamp.adapters.modeling.FeatureAccessor;
import com.bytex.snamp.internal.Utils;
import com.google.common.collect.Iterables;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

/**
 * Represents Decanter Collector in the form of SNAMP Resource Adapter.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.2
 */
final class DecanterAdapter extends AbstractResourceAdapter {
    private final EventDrivenCollector eventCollector;
    private final PolledCollector attributeCollector;
    private ServiceRegistration<Runnable> polledCollectorRegistration;

    DecanterAdapter(final String instanceName,
                    final EventAdmin eventAdmin){
        super(instanceName);
        eventCollector = new EventDrivenCollector(eventAdmin);
        attributeCollector = new PolledCollector(eventAdmin, getLogger());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>) eventCollector.addNotification(resourceName, (MBeanNotificationInfo)feature);
        else if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>) attributeCollector.addAttribute(resourceName, (MBeanAttributeInfo)feature);
        else
            return null;
    }

    @Override
    protected Iterable<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) {
        return Iterables.concat(
            eventCollector.clear(resourceName),
            attributeCollector.clear(resourceName)
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) throws Exception {
        if (feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>) eventCollector.removeNotification(resourceName, (MBeanNotificationInfo) feature);
        else if (feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>) attributeCollector.removeAttribute(resourceName, (MBeanAttributeInfo) feature);
        else
            return null;
    }

    @Override
    protected void start(final Map<String, String> parameters) {
        //register collector for attributes
        final BundleContext context = Utils.getBundleContextOfObject(this);
        final Dictionary<String, String> identity = new Hashtable<>();
        identity.put("decanter.collector.name", getInstanceName());
        polledCollectorRegistration = context.registerService(Runnable.class, attributeCollector, identity);
    }

    @Override
    protected void stop() {
        if(polledCollectorRegistration != null)
            polledCollectorRegistration.unregister();
        polledCollectorRegistration = null;
    }
}
