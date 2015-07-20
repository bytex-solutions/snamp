package com.itworks.snamp.adapters.jmx;

import com.google.common.collect.ImmutableList;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.adapters.AttributesModelReader;
import com.itworks.snamp.adapters.FeatureAccessor;
import com.itworks.snamp.adapters.NotificationsModelReader;
import com.itworks.snamp.adapters.binding.FeatureBindingInfo;
import com.itworks.snamp.adapters.jmx.binding.JmxAdapterRuntimeInfo;
import com.itworks.snamp.internal.AbstractKeyedObjects;
import com.itworks.snamp.internal.KeyedObjects;
import com.itworks.snamp.internal.RecordReader;
import com.itworks.snamp.internal.Utils;
import org.osgi.framework.BundleContext;

import javax.management.*;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents JMX adapter. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxResourceAdapter extends AbstractResourceAdapter {
    private static final class MBeanRegistry extends AbstractKeyedObjects<String, ProxyMBean> implements AttributesModelReader<JmxAttributeAccessor>, NotificationsModelReader<JmxNotificationAccessor>{
        private static final long serialVersionUID = 7388558732363175763L;

        private MBeanRegistry(){
            super(8);
        }

        @Override
        public String getKey(final ProxyMBean bean) {
            return bean.getResourceName();
        }

        @Override
        public <E extends Exception> void forEachAttribute(final RecordReader<String, ? super JmxAttributeAccessor, E> attributeReader) throws E {
            for(final ProxyMBean bean: values())
                bean.forEachAttribute(attributeReader);
        }

        @Override
        public <E extends Exception> void forEachNotification(final RecordReader<String, ? super JmxNotificationAccessor, E> notificationReader) throws E {
            for(final ProxyMBean bean: values())
                bean.forEachNotification(notificationReader);
        }
    }

    private final MBeanRegistry exposedBeans;
    private boolean usePlatformMBean;
    private ObjectName rootObjectName;

    JmxResourceAdapter(final String adapterInstanceName) {
        super(adapterInstanceName);
        this.exposedBeans = new MBeanRegistry();
        this.usePlatformMBean = false;
        rootObjectName = null;
    }

    private static ObjectName createObjectName(final ObjectName rootObjectName, final String resourceName) throws MalformedObjectNameException {
        if(rootObjectName == null)
            throw new MalformedObjectNameException("Root object name is not specified");
        final Hashtable<String, String> attrs = new Hashtable<>(rootObjectName.getKeyPropertyList());
        attrs.put("resource", resourceName);
        return new ObjectName(rootObjectName.getDomain(), attrs);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected synchronized <M extends MBeanFeatureInfo, S> FeatureAccessor<M, S> addFeature(final String resourceName, final M feature) throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        final ProxyMBean bean;
        if(exposedBeans.containsKey(resourceName))
            bean = exposedBeans.get(resourceName);
        else {
            exposedBeans.put(bean = new ProxyMBean(resourceName));
            if(rootObjectName != null) {
                //register bean
                if (usePlatformMBean)
                    bean.register(createObjectName(rootObjectName, resourceName));
                else
                    bean.register(getBundleContext(), createObjectName(rootObjectName, resourceName));
            }
        }
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, S>)bean.addAttribute((MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M, S>)bean.addNotification((MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected synchronized Iterable<? extends FeatureAccessor<?, ?>> removeAllFeatures(final String resourceName) throws MalformedObjectNameException, MBeanRegistrationException, InstanceNotFoundException {
        if(exposedBeans.containsKey(resourceName)){
            final ProxyMBean bean = exposedBeans.remove(resourceName);
            //unregister bean
            if(rootObjectName != null) {
                if (usePlatformMBean)
                    bean.unregister(createObjectName(rootObjectName, resourceName));
            }
            return bean.getAccessorsAndClose();
        }
        else return ImmutableList.of();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected synchronized  <M extends MBeanFeatureInfo> FeatureAccessor<M, ?> removeFeature(final String resourceName, final M feature) throws Exception {
        if(exposedBeans.containsKey(resourceName)){
            final ProxyMBean bean = exposedBeans.get(resourceName);
            if(feature instanceof MBeanAttributeInfo)
                return (FeatureAccessor<M, ?>)bean.removeAttribute((MBeanAttributeInfo)feature);
            else if(feature instanceof MBeanNotificationInfo)
                return (FeatureAccessor<M, ?>)bean.removeNotification((MBeanNotificationInfo)feature);
            else return null;
        }
        else return null;
    }

    @Override
    protected synchronized void start(final Map<String, String> parameters) throws MalformedObjectNameException, NotCompliantMBeanException, InstanceAlreadyExistsException, MBeanRegistrationException {
        rootObjectName = JmxAdapterConfigurationProvider.parseRootObjectName(parameters);
        usePlatformMBean = JmxAdapterConfigurationProvider.usePlatformMBean(parameters);
        for (final Map.Entry<String, ProxyMBean> entry : exposedBeans.entrySet())
            if (usePlatformMBean)
                entry.getValue().register(createObjectName(rootObjectName, entry.getKey()));
            else entry.getValue().register(getBundleContext(), createObjectName(rootObjectName, entry.getKey()));
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextByObject(this);
    }

    @Override
    protected synchronized void stop() throws MalformedObjectNameException, InstanceNotFoundException, MBeanRegistrationException {
        try {
            for (final Map.Entry<String, ProxyMBean> entry : exposedBeans.entrySet()) {
                if (usePlatformMBean)
                    entry.getValue().unregister(createObjectName(rootObjectName, entry.getKey()));
                entry.getValue().close();
            }
        }
        finally {
            rootObjectName = null;
            exposedBeans.clear();
        }
    }

    /**
     * Gets information about binding of the features.
     *
     * @param bindingType Type of the feature binding.
     * @return A collection of features
     */
    @Override
    protected synchronized <B extends FeatureBindingInfo> Collection<? extends B> getBindings(final Class<B> bindingType) {
        return JmxAdapterRuntimeInfo.getBindingInfo(bindingType, exposedBeans, exposedBeans);
    }
}
