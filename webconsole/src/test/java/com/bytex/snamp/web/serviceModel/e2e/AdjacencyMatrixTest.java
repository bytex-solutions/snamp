package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.instrumentation.Identifier;
import com.bytex.snamp.instrumentation.measurements.Span;
import com.bytex.snamp.moa.topology.ComponentVertex;
import com.bytex.snamp.moa.topology.GraphOfComponents;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class AdjacencyMatrixTest extends Assert {
    private static final class TopologyAnalyzerMock extends GraphOfComponents implements TopologyAnalyzer{
        private static final long serialVersionUID = -384175063368082423L;

        private TopologyAnalyzerMock() {
            super(100);
        }

        @Override
        public void parallelForEach(final Consumer<? super ComponentVertex> visitor, final Executor executor) {
            forEach(visitor::accept);
        }
    }

    private static final String COMPONENT1 = "dispatcher";
    private static final String COMPONENT2 = "mobileApp";
    private final TopologyAnalyzerMock graph;
    private final ObjectMapper mapper;

    public AdjacencyMatrixTest() {
        graph = new TopologyAnalyzerMock();
        mapper = new ObjectMapper();
    }

    @Test
    public void landscapeTest() throws IOException {
        //root component
        final Span rootSpan = new Span();
        rootSpan.setComponentName(COMPONENT1);
        rootSpan.setInstanceName("node1");
        rootSpan.setDuration(15, TimeUnit.MILLISECONDS);
        rootSpan.generateIDs();
        graph.accept(rootSpan);

        final Span span = new Span();
        span.setComponentName(COMPONENT2);
        span.setInstanceName("node1");
        span.setDuration(5, TimeUnit.MILLISECONDS);
        span.setCorrelationID(rootSpan.getCorrelationID());
        span.setSpanID(Identifier.randomID(4));
        span.setParentSpanID(rootSpan.getSpanID());
        graph.accept(span);

        final Span recursiveSpan = new Span();
        recursiveSpan.setComponentName(COMPONENT1);
        recursiveSpan.setInstanceName("node1");
        recursiveSpan.setDuration(30, TimeUnit.SECONDS);
        recursiveSpan.setCorrelationID(rootSpan.getCorrelationID());
        recursiveSpan.setSpanID(Identifier.randomID(4));
        recursiveSpan.setParentSpanID(span.getSpanID());
        graph.accept(recursiveSpan);

        final AdjacencyMatrix matrix = new LandscapeView().build(graph);
        graph.forEach(matrix);
        final String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(matrix);
        assertNotNull(json);
    }
}
