package com.itworks.snamp.adapters.jmx;

import com.google.common.reflect.TypeToken;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxTypeLiterals {
    private JmxTypeLiterals(){}

    static final TypeToken<CompositeData> COMPOSITE_DATA = TypeToken.of(CompositeData.class);

    static final TypeToken<TabularData> TABULAR_DATA = TypeToken.of(TabularData.class);
}
