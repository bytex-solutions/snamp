package com.bytex.snamp.web.serviceModel.e2e;

import org.codehaus.jackson.node.ObjectNode;

/**
 * Represents adjacency matrix where each vertex has metrics associated with arrivals of input requests.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AdjacencyMatrixWithArrivals extends AdjacencyMatrix {
    @Override
    final void serialize(final ObjectNode output) {

    }
}
