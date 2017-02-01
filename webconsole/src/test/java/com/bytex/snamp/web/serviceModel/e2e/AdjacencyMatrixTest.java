package com.bytex.snamp.web.serviceModel.e2e;

import com.bytex.snamp.instrumentation.Identifier;
import com.bytex.snamp.instrumentation.measurements.Span;
import com.bytex.snamp.moa.topology.GraphOfComponents;
import com.bytex.snamp.moa.topology.TopologyAnalyzer;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
    }

    private static final String COMPONENT1 = "dispatcher";
    private static final String COMPONENT2 = "mobileApp";
    private static final String COMPONENT3 = "database";
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
        span.setModuleName("main");
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

    @Test
    public void childComponentsTest() throws IOException {
        //root component
        final Span firstSpan = new Span();
        firstSpan.setComponentName(COMPONENT1);
        firstSpan.setInstanceName("node1");
        firstSpan.setDuration(15, TimeUnit.MILLISECONDS);
        firstSpan.generateIDs();
        graph.accept(firstSpan);

        final Span secondSpan = new Span();
        secondSpan.setComponentName(COMPONENT2);
        secondSpan.setInstanceName("node1");
        secondSpan.setModuleName("main");
        secondSpan.setDuration(5, TimeUnit.MILLISECONDS);
        secondSpan.setCorrelationID(firstSpan.getCorrelationID());
        secondSpan.setSpanID(Identifier.randomID(4));
        secondSpan.setParentSpanID(firstSpan.getSpanID());
        graph.accept(secondSpan);

        final Span thirdSpan = new Span();
        thirdSpan.setComponentName(COMPONENT3);
        thirdSpan.setInstanceName("db-master");
        thirdSpan.setDuration(5, TimeUnit.MILLISECONDS);
        thirdSpan.setCorrelationID(secondSpan.getCorrelationID());
        thirdSpan.setSpanID(Identifier.randomID(4));
        thirdSpan.setParentSpanID(secondSpan.getSpanID());
        graph.accept(thirdSpan);

        final ChildComponentsView view = new ChildComponentsView();
        view.setTargetComponent(COMPONENT2);
        final AdjacencyMatrix matrix = view.build(graph);
        graph.forEach(matrix);
        final String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(matrix);
        assertNotNull(json);
    }

    @Test
    public void modulesComponentsTest() throws IOException {
        //root component
        final Span firstSpan = new Span();
        firstSpan.setComponentName(COMPONENT1);
        firstSpan.setInstanceName("node1");
        firstSpan.setDuration(15, TimeUnit.MILLISECONDS);
        firstSpan.generateIDs();
        graph.accept(firstSpan);

        final Span secondSpan = new Span();
        secondSpan.setComponentName(COMPONENT2);
        secondSpan.setInstanceName("node1");
        secondSpan.setModuleName("controller");
        secondSpan.setDuration(5, TimeUnit.MILLISECONDS);
        secondSpan.setCorrelationID(firstSpan.getCorrelationID());
        secondSpan.setSpanID(Identifier.randomID(4));
        secondSpan.setParentSpanID(firstSpan.getSpanID());
        graph.accept(secondSpan);

        final Span thirdSpan = new Span();
        thirdSpan.setComponentName(COMPONENT2);
        thirdSpan.setInstanceName("node2");
        thirdSpan.setModuleName("dataAccess");
        thirdSpan.setDuration(5, TimeUnit.MILLISECONDS);
        thirdSpan.setCorrelationID(secondSpan.getCorrelationID());
        thirdSpan.setSpanID(Identifier.randomID(4));
        thirdSpan.setParentSpanID(secondSpan.getSpanID());
        graph.accept(thirdSpan);

        final ComponentModulesView view = new ComponentModulesView();
        view.setTargetComponent(COMPONENT2);
        final AdjacencyMatrix matrix = view.build(graph);
        graph.forEach(matrix);
        final String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(matrix);
        assertNotNull(json);
    }
}
