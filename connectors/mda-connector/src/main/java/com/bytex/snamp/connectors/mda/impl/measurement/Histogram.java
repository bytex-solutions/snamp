package com.bytex.snamp.connectors.mda.impl.measurement;

import java.util.Iterator;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface Histogram extends StatDataCollector {
    Iterator<Comparable<?>[]> getSeries();
}
