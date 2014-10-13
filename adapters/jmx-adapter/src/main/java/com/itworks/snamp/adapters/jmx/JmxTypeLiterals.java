package com.itworks.snamp.adapters.jmx;

import com.itworks.snamp.TypeLiterals;
import org.apache.commons.lang3.reflect.Typed;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class JmxTypeLiterals {
    private JmxTypeLiterals(){}

    static final Typed<CompositeData> COMPOSITE_DATA = TypeLiterals.of(CompositeData.class);

    static final Typed<TabularData> TABULAR_DATA = TypeLiterals.of(TabularData.class);
}
