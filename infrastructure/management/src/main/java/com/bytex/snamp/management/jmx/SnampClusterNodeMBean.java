package com.bytex.snamp.management.jmx;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Aggregator;
import com.bytex.snamp.jmx.OpenMBean;
import com.bytex.snamp.management.FrameworkMBean;

import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class SnampClusterNodeMBean extends OpenMBean implements FrameworkMBean {
    public static final String OBJECT_NAME = "com.bytex.snamp.management:type=SnampClusterNode";

    private final Aggregator aggregator;

    public SnampClusterNodeMBean(){
        super(
                new IsInClusterAttribute(),
                new IsActiveNodeAttribute(),
                new MemberNameAttribute(),
                new ResignOperation()
        );
        aggregator = AbstractAggregator.builder()
                .add(Logger.class, this::getLogger)
                .build();
    }

    @Override
    public Logger getLogger() {
        return SnampCoreMBean.getLoggerImpl();
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(final Class<T> objectType) {
        return aggregator.queryObject(objectType);
    }
}
