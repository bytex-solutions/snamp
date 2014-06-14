package com.itworks.snamp.management.jmx;

import com.itworks.snamp.core.FrameworkService;

import javax.management.DynamicMBean;

/**
 * Represents an interface for all SNAMP-related managed beans.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 * @see com.itworks.snamp.management.jmx.OpenMBean
 */
public interface FrameworkMBean extends DynamicMBean, FrameworkService {
    /**
     * The name of the service identity property that is used by Apache Aries JMX Whiteboard
     * to capture and expose MBeans.
     */
    String OBJECT_NAME_IDENTITY_PROPERTY = "jmx.objectname";
}
