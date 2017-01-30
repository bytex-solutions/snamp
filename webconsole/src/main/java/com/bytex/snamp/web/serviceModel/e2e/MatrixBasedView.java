package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.moa.topology.TopologyAnalyzer;

/**
 * Represents view which data can be constructed as adjacency matrix.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
interface MatrixBasedView extends E2EView {
    @Override
    AdjacencyMatrix build(final TopologyAnalyzer analyzer);
}
