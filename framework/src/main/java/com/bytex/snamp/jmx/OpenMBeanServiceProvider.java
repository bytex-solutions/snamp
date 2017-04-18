package com.bytex.snamp.jmx;

import javax.annotation.Nonnull;
import javax.management.JMException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import static com.bytex.snamp.core.AbstractBundleActivator.RequiredService;
import static com.bytex.snamp.core.AbstractServiceLibrary.ProvidedService;

/**
 * Exposes JMX Bean as OSGi service.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class OpenMBeanServiceProvider<T extends OpenMBean & FrameworkMBean> extends ProvidedService<FrameworkMBean, T> {
    private final String objectName;

    /**
     * Initializes a new holder for the MBean.
     * @param objectName The registration name of the MBean.
     * @param dependencies A collection of service dependencies.
     * @throws IllegalArgumentException contract is {@literal null}.
     */
    protected OpenMBeanServiceProvider(final String objectName,
                                       final RequiredService<?>... dependencies) {
        super(FrameworkMBean.class, dependencies);
        this.objectName = objectName;
    }

    /**
     * Returns {@literal true} to register MBean in platform-specific MBean Server.
     * @return {@literal true} to register MBean in platform-specific MBean Server.
     */
    protected boolean usePlatformMBeanServer(){
        return false;
    }

    /**
     * Creates a new instance of MBean.
     * @return A new instance of MBean.
     * @throws JMException Unable to instantiate MBean.
     */
    protected abstract T createMBean() throws JMException;

    /**
     * Creates a new instance of the service.
     *
     * @param identity     A dictionary of properties that uniquely identifies service instance.
     * @return A new instance of the service.
     * @throws javax.management.JMException Unable to instantiate MBean.
     */
    @Override
    @Nonnull
    protected T activateService(final Map<String, Object> identity) throws JMException {
        final ObjectName objectName = new ObjectName(this.objectName);
        createIdentity(identity, objectName);
        final T mbean = createMBean();
        if (usePlatformMBeanServer())
            ManagementFactory.getPlatformMBeanServer().registerMBean(mbean, objectName);
        return mbean;
    }

    private static void createIdentity(final Map<String, Object> identity, final ObjectName name){
        identity.put(FrameworkMBean.OBJECT_NAME_IDENTITY_PROPERTY, name);
    }

    public static Dictionary<String, Object> createIdentity(final ObjectName name){
        final Hashtable<String, Object> identity = new Hashtable<>(1);
        createIdentity(identity, name);
        return identity;
    }

    /**
     * Provides service cleanup operations.
     * <p>
     * In the default implementation this method does nothing.
     * </p>
     *
     * @param serviceInstance An instance of the hosted service to cleanup.
     * @param stopBundle      {@literal true}, if this method calls when the owner bundle is stopping;
     *                        {@literal false}, if this method calls when loosing dependency.
     */
    @Override
    protected void cleanupService(final T serviceInstance, final boolean stopBundle) throws Exception {
        //if bundle started in Apache Karaf environment then MBean server automatically registers SNAMP MBeans
        //if not, then register MBean manually
        if(usePlatformMBeanServer())
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(objectName));
    }
}
