package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.management.jmx.OpenMBeanProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import javax.management.DynamicMBean;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents JMX adapter. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxResourceAdapter extends AbstractResourceAdapter {

    private static final class JmxAttributes extends AbstractAttributesModel<JmxAttribute>{
        private static final String ID_SEPARATOR = "::";
        private boolean pureSerialization = false;

        /**
         * Creates a new domain-specific representation of the management attribute.
         *
         * @param resourceName             User-defined name of the managed resource.
         * @param userDefinedAttributeName User-defined name of the attribute.
         * @param accessor                 An accessor for the individual management attribute.
         * @return A new domain-specific representation of the management attribute.
         */
        @Override
        protected JmxAttribute createAttributeView(final String resourceName, final String userDefinedAttributeName, final AttributeAccessor accessor) {
            return new JmxAttribute(accessor, pureSerialization);
        }

        /**
         * Creates a new unique identifier of the management attribute.
         * <p>
         * The identifier must be unique through all instances of the resource adapter.
         * </p>
         *
         * @param resourceName             User-defined name of the managed resource which supply the attribute.
         * @param userDefinedAttributeName User-defined name of the attribute.
         * @return A new unique identifier of the management attribute.
         */
        @Override
        protected String makeAttributeID(final String resourceName, final String userDefinedAttributeName) {
            return String.format("%s" + ID_SEPARATOR + "%s", resourceName, userDefinedAttributeName);
        }

        private static String getResourceName(final String attributeId){
            final String[] parts = attributeId.split(ID_SEPARATOR);
            return parts.length == 2 ? parts[0] : null;
        }

        public Set<String> getResources(){
            final Set<String> resources = new HashSet<>(5);
            for(final String id: keySet()){
                final String resourceName = getResourceName(id);
                if(resourceName == null || resourceName.isEmpty()) continue;
                resources.add(resourceName);
            }
            return resources;
        }

        public Map<String, JmxAttribute> get(final String resourceName){
            final Map<String, JmxAttribute> attributes = new HashMap<>(10);
            for(final String id: keySet()){
                final String[] parts = id.split(ID_SEPARATOR);
                if(parts.length != 2 || !Objects.equals(parts[0], resourceName)) continue;
                attributes.put(parts[1], super.get(id));
            }
            return attributes;
        }

        public void usePureSerialization() {
            pureSerialization = true;
        }
    }

    private final ObjectName rootObjectName;
    private final boolean usePlatformMBean;
    private final JmxAttributes attributes;
    private final Collection<ServiceRegistration<DynamicMBean>> exposedBeans;

    public JmxResourceAdapter(final ObjectName rootObjectName,
                              final boolean usePlatformMBean,
                              final Map<String, ManagedResourceConfiguration> resources) throws MalformedObjectNameException {
        super(resources);
        this.rootObjectName = rootObjectName;
        this.usePlatformMBean = usePlatformMBean;
        this.attributes = new JmxAttributes();
        this.exposedBeans = new ArrayList<>(10);
    }

    public void usePureSerialization(){
        attributes.usePureSerialization();
    }

    private static ObjectName createObjectName(final ObjectName rootObjectName, final String resourceName) throws MalformedObjectNameException {
        final Hashtable<String, String> attrs = new Hashtable<>(rootObjectName.getKeyPropertyList());
        attrs.put("resource", resourceName);
        return new ObjectName(rootObjectName.getDomain(), attrs);
    }

    private void registerMBean(final ObjectName name, final ProxyMBean mbean) throws JMException{
        if(usePlatformMBean)
            ManagementFactory.getPlatformMBeanServer().registerMBean(mbean, name);
        else {
            final BundleContext context = Utils.getBundleContextByObject(this);
            exposedBeans.add(context.registerService(DynamicMBean.class, mbean, OpenMBeanProvider.createIdentity(name)));
        }
    }

    private void unregisterMBean(final ObjectName name) throws JMException {
        if (usePlatformMBean)
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(name);
        else
            for (final ServiceRegistration<DynamicMBean> mbean : exposedBeans)
                mbean.unregister();
    }

    /**
     * Starts the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     *
     * @return {@literal true}, if adapter is started successfully; otherwise, {@literal false}.
     */
    @Override
    protected boolean start() {
        populateModel(attributes);
        for (final String resourceName : attributes.getResources())
            try {
                registerMBean(createObjectName(rootObjectName, resourceName), new ProxyMBean(resourceName, attributes.get(resourceName)));
            }
            catch (final JMException e) {
                getLogger().log(Level.SEVERE, String.format("Unable to register MBean for resource %s", resourceName), e);
            }
        return true;
    }

    /**
     * Stops the adapter.
     * <p>
     * This method will be called by SNAMP infrastructure automatically.
     * </p>
     */
    @Override
    protected void stop() {
        for(final String resourceName: attributes.getResources())
            try {
                unregisterMBean(createObjectName(rootObjectName, resourceName));
            }
            catch (final JMException e) {
                getLogger().log(Level.SEVERE, String.format("Unable to unregister MBean for resource %s", resourceName), e);
            }
    }

    /**
     * Gets logger associated with this service.
     *
     * @return The logger associated with this service.
     */
    @Override
    public Logger getLogger() {
        return JmxAdapterHelpers.getLogger();
    }
}
