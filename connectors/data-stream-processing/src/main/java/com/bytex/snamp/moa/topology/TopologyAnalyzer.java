package com.bytex.snamp.moa.topology;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.moa.DataAnalyzer;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents MOA service responsible for building network topology.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface TopologyAnalyzer extends DataAnalyzer {
    @FunctionalInterface
    interface Visitor{
        boolean visit(final ComponentVertex vertex);
    }

    void visit(final Visitor visitor);
}
