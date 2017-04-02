package com.bytex.snamp.management.jmx;

import com.bytex.snamp.AbstractAggregator;
import com.bytex.snamp.Aggregator;
import com.bytex.snamp.core.SnampManager;
import com.bytex.snamp.jmx.FrameworkMBean;
import com.bytex.snamp.jmx.OpenMBean;
import com.bytex.snamp.management.DefaultSnampManager;

import javax.annotation.Nonnull;

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
                .addValue(SnampManager.class, new DefaultSnampManager())
                .build();
    }

    /**
     * Retrieves the aggregated object.
     *
     * @param objectType Type of the requested object.
     * @return An instance of the aggregated object; or {@literal null} if object is not available.
     */
    @Override
    public <T> T queryObject(@Nonnull final Class<T> objectType) {
        return objectType.isInstance(this) ? objectType.cast(this) : aggregator.queryObject(objectType);
    }
}
