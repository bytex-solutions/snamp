package com.bytex.snamp.management.jmx;

import com.bytex.snamp.jmx.OpenMBean;
import com.bytex.snamp.management.FrameworkMBean;

import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class SnampClusterNodeMBean extends OpenMBean implements FrameworkMBean {
    public static final String OBJECT_NAME = "com.bytex.snamp.management:type=SnampClusterNode";

    public SnampClusterNodeMBean(){
        super(
                new IsInClusterAttribute(),
                new IsActiveNodeAttribute(),
                new NodeNameAttribute(),
                new ResignOperation()
        );
    }

    @Override
    public Logger getLogger() {
        return MonitoringUtils.getLogger();
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return null;
    }
}
