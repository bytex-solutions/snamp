package com.itworks.snamp.management.jmx;

import javax.management.JMException;
import javax.management.ObjectName;
import java.util.Map;

import static com.itworks.snamp.core.AbstractServiceLibrary.ProvidedService;
import static com.itworks.snamp.core.AbstractServiceLibrary.RequiredService;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class OpenMBeanProvider<T extends OpenMBean & FrameworkMBean> extends ProvidedService<FrameworkMBean, T> {
    private final String objectName;

    /**
     * Initializes a new holder for the MBean.
     * @param objectName The registration name of the MBean.
     * @param dependencies A collection of service dependencies.
     * @throws IllegalArgumentException contract is {@literal null}.
     */
    protected OpenMBeanProvider(final String objectName,
                                final RequiredService<?>... dependencies) {
        super(FrameworkMBean.class, dependencies);
        this.objectName = objectName;
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
     * @param dependencies A collection of dependencies.
     * @return A new instance of the service.
     * @throws javax.management.JMException Unable to instantiate MBean.
     */
    @Override
    protected T activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) throws JMException {
        identity.put(FrameworkMBean.OBJECT_NAME_IDENTITY_PROPERTY, new ObjectName(objectName));
        return createMBean();
    }
}
