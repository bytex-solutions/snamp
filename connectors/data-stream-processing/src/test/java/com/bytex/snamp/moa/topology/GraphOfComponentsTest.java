package com.bytex.snamp.moa.topology;

import com.bytex.snamp.instrumentation.Identifier;
import com.bytex.snamp.instrumentation.measurements.Span;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Represents tests for {@link GraphOfComponents}.
 */
public final class GraphOfComponentsTest extends Assert {
    private static final String COMPONENT1 = "dispatcher";
    private static final String COMPONENT2 = "mobileApp";
    private static final String COMPONENT3 = "scheduler";
    private final GraphOfComponents graph;

    public GraphOfComponentsTest() {
        graph = new GraphOfComponents(1000);
        graph.getAllowedComponents().add(COMPONENT1);
        graph.getAllowedComponents().add(COMPONENT2);
        graph.getAllowedComponents().add(COMPONENT3);
    }

    @Test
    public void simpleSpanCollectionTest(){
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

        assertEquals(2, graph.size());

        final ComponentVertex rootComponent = graph.get(COMPONENT1);
        assertNotNull(rootComponent);
        assertEquals(1, rootComponent.size());
        final ComponentVertex childComponent = graph.get(COMPONENT2);
        assertNotNull(childComponent);
        assertTrue(childComponent.isEmpty());

        rootComponent.contains(childComponent);
        

        graph.clear();
    }
}
