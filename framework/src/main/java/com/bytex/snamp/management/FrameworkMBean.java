package com.bytex.snamp.management;

import com.bytex.snamp.core.SupportService;

import javax.management.DynamicMBean;

/**
 * Represents an interface for all SNAMP-related managed beans.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 * @see com.bytex.snamp.jmx.OpenMBean
 */
public interface FrameworkMBean extends DynamicMBean, SupportService {
    /**
     * The name of the service identity property that is used by Apache Aries JMX Whiteboard
     * to capture and expose MBeans.
     */
    String OBJECT_NAME_IDENTITY_PROPERTY = "jmx.objectname";
}
