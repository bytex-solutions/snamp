package com.bytex.snamp.moa.topology;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.moa.DataAnalyzer;

/**
 * Represents MOA service responsible for building network topology.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface TopologyAnalyzer extends DataAnalyzer {

    <E extends Throwable> void forEach(final Acceptor<? super ComponentVertex, E> visitor) throws E;
}
