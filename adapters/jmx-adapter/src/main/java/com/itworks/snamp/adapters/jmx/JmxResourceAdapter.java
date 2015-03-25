package com.itworks.snamp.adapters.jmx;

import com.google.common.collect.ImmutableList;
import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.adapters.FeatureAccessor;
import com.itworks.snamp.internal.Utils;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;

import static com.itworks.snamp.adapters.jmx.JmxAdapterConfigurationProvider.OBJECT_NAME_PARAM;
import static com.itworks.snamp.adapters.jmx.JmxAdapterConfigurationProvider.USE_PLATFORM_MBEAN_PARAM;

/**
 * Represents JMX adapter. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxResourceAdapter extends AbstractResourceAdapter {
    static final String NAME = JmxAdapterHelpers.ADAPTER_NAME;

    private final Map<ObjectName, ProxyMBean> exposedBeans;
    private boolean usePlatformMBean;
    private ObjectName rootObjectName;

    JmxResourceAdapter(final String adapterInstanceName) {
        super(adapterInstanceName);
        this.exposedBeans = createMBeanMap();
        this.usePlatformMBean = false;
        rootObjectName = null;
    }

    private static Map<ObjectName, ProxyMBean> createMBeanMap(){
        return new HashMap<ObjectName, ProxyMBean>(10){
            private static final long serialVersionUID = 7388558732363175763L;

            @Override
            public void clear() {
                for(final ProxyMBean bean: values())
                    bean.close();
                super.clear();
            }
        };
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
        final ObjectName beanName = createObjectName(rootObjectName, resourceName);
        final ProxyMBean bean;
        if(exposedBeans.containsKey(beanName))
            bean = exposedBeans.get(beanName);
        else {
            exposedBeans.put(beanName, bean = new ProxyMBean(resourceName));
            //register bean
            if(usePlatformMBean)
                ManagementFactory.getPlatformMBeanServer().registerMBean(bean, beanName);
            else
                bean.registerAsService(Utils.getBundleContextByObject(this), beanName);
        }
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, S>)bean.addAttribute((MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M, S>)bean.addNotification((MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected synchronized Iterable<? extends FeatureAccessor<?, ?>> removeAllFeatures(final String resourceName) throws MalformedObjectNameException, MBeanRegistrationException, InstanceNotFoundException {
        final ObjectName beanName = createObjectName(rootObjectName, resourceName);
        if(exposedBeans.containsKey(beanName)){
            final ProxyMBean bean = exposedBeans.remove(beanName);
            //unregister bean
            if (usePlatformMBean)
                ManagementFactory.getPlatformMBeanServer().unregisterMBean(beanName);
            return bean.getAccessorsAndClose();
        }
        else return ImmutableList.of();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected synchronized  <M extends MBeanFeatureInfo> FeatureAccessor<M, ?> removeFeature(final String resourceName, final M feature) throws Exception {
        final ObjectName beanName = createObjectName(rootObjectName, resourceName);
        if(exposedBeans.containsKey(beanName)){
            final ProxyMBean bean = exposedBeans.get(rootObjectName);
            if(feature instanceof MBeanAttributeInfo)
                return (FeatureAccessor<M, ?>)bean.removeAttribute((MBeanAttributeInfo)feature);
            else if(feature instanceof MBeanNotificationInfo)
                return (FeatureAccessor<M, ?>)bean.removeNotification((MBeanNotificationInfo)feature);
            else return null;
        }
        else return null;
    }

    @Override
    protected synchronized void start(final Map<String, String> parameters) throws MalformedObjectNameException{
        if (parameters.containsKey(OBJECT_NAME_PARAM)) {
            this.rootObjectName = new ObjectName(parameters.get(OBJECT_NAME_PARAM));
            usePlatformMBean = parameters.containsKey(USE_PLATFORM_MBEAN_PARAM) &&
                    Boolean.valueOf(parameters.get(USE_PLATFORM_MBEAN_PARAM));
        }
        else throw new MalformedObjectNameException(String.format("Adapter configuration has no %s entry", OBJECT_NAME_PARAM));
    }

    /**
     * Stops the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     */
    @Override
    protected synchronized void stop(){
        exposedBeans.clear();
        System.gc();
    }

    /**
     * Gets withLogger associated with this service.
     *
     * @return The withLogger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return getLogger(NAME);
    }
}
