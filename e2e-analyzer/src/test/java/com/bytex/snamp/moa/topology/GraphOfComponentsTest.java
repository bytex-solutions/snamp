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
    }

    @Test
    public void recursiveSpanCollectionTest(){
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

        assertEquals(2, graph.size());

        final ComponentVertex rootComponent = graph.get(COMPONENT1);
        assertNotNull(rootComponent);
        assertEquals(1, rootComponent.size());
        final ComponentVertex childComponent = graph.get(COMPONENT2);
        assertNotNull(childComponent);
        assertEquals(1, childComponent.size());

        assertTrue(rootComponent.contains(childComponent));
        assertTrue(childComponent.contains(rootComponent));

        assertTrue(graph.remove(COMPONENT1));
        assertTrue(graph.values().contains(childComponent));
        assertEquals(0, childComponent.size());

        graph.clear();
    }

    @Test
    public void spanTreeCollectionTest(){
        //root component
        final Span rootSpan = new Span();
        rootSpan.setComponentName(COMPONENT1);
        rootSpan.setInstanceName("node1");
        rootSpan.setDuration(15, TimeUnit.MILLISECONDS);
        rootSpan.generateIDs();
        graph.accept(rootSpan);

        Span span = new Span();
        span.setComponentName(COMPONENT2);
        span.setInstanceName("node1");
        span.setDuration(5, TimeUnit.MILLISECONDS);
        span.setCorrelationID(rootSpan.getCorrelationID());
        span.setSpanID(Identifier.randomID(4));
        span.setParentSpanID(rootSpan.getSpanID());
        graph.accept(span);

        span = new Span();
        span.setComponentName(COMPONENT3);
        span.setInstanceName("node1");
        span.setDuration(3, TimeUnit.MILLISECONDS);
        span.setCorrelationID(rootSpan.getCorrelationID());
        span.setSpanID(Identifier.randomID(4));
        span.setParentSpanID(rootSpan.getSpanID());
        graph.accept(span);

        assertEquals(3, graph.size());

        final ComponentVertex rootComponent = graph.get(COMPONENT1);
        assertNotNull(rootComponent);
        assertEquals(2, rootComponent.size());
        final ComponentVertex childComponent1 = graph.get(COMPONENT2);
        assertNotNull(childComponent1);
        assertEquals(0, childComponent1.size());
        final ComponentVertex childComponent2 = graph.get(COMPONENT3);
        assertNotNull(childComponent2);
        assertEquals(0, childComponent2.size());


        assertTrue(rootComponent.contains(childComponent1));
        assertTrue(rootComponent.contains(childComponent2));

        graph.clear();
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

        assertTrue(rootComponent.contains(childComponent));

        assertTrue(graph.remove(COMPONENT2));

        graph.clear();
    }
}
