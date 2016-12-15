package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.measurements.CorrelationPolicy;
import com.bytex.snamp.instrumentation.measurements.Span;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class TraceScope implements MeasurementScope {
    private static final ThreadLocal<TraceScope> CURRENT_SCOPE = new ThreadLocal<>();

    private final long startTime;
    private final Identifier spanID;
    private final TraceScope parent;
    private final Identifier correlationID;
    private final CorrelationPolicy correlationPolicy;
    private Map<String, String> annotations;

    protected TraceScope(final Identifier correlationID, final CorrelationPolicy correlationPolicy, final Identifier parentSpanID){
        if(correlationID == null)
            throw new IllegalArgumentException("correlationID cannot be null");
        else if(parentSpanID == null)
            throw new IllegalArgumentException("parentSpanID cannot be null");
        else {
            this.spanID = Identifier.randomID();
            this.correlationID = correlationID;
            this.correlationPolicy = correlationPolicy;
            startTime = System.nanoTime();
            //push scope
            if (Identifier.EMPTY.equals(parentSpanID)) {
                parent = CURRENT_SCOPE.get();
            } else {
                parent = new TraceScope(correlationID, correlationPolicy, Identifier.EMPTY) {
                    @Override
                    protected void report(final Span s) {
                        TraceScope.this.report(s);
                    }
                };
            }
            CURRENT_SCOPE.set(this);
        }
    }

    /**
     * Adds annotation to this scope.
     * @param name Annotation name.
     * @param value Annotation value.
     */
    public final void addAnnotation(final String name, final String value){
        if(annotations == null)
            annotations = new HashMap<>();
        annotations.put(name, value);
    }

    public final CorrelationPolicy getCorrelationPolicy() {
        return correlationPolicy == null && parent != null ? parent.getCorrelationPolicy() : correlationPolicy;
    }

    /**
     * Gets correlation ID associated with this scope.
     * @return Correlation ID associated with this scope.
     */
    public final Identifier getCorrelationID() {
        return Identifier.EMPTY.equals(correlationID) && parent != null ? parent.getCorrelationID() : correlationID;
    }

    /**
     * Gets identifier of the span associated with this scope.
     * @return Identifier of the span associated with this scope.
     */
    public final Identifier getSpanID(){
        return spanID;
    }

    /**
     * Gets parent scope.
     * @return The parent scope.
     */
    public final TraceScope getParent(){
        return parent;
    }

    /**
     * Gets current scope.
     * @return Current scope.
     */
    public static TraceScope current(){
        return CURRENT_SCOPE.get();
    }

    protected abstract void report(final Span s);

    /**
     * Closes measurement scope.
     */
    @Override
    public final void close() {
        final Span s = new Span();
        //set parent span
        {
            if (parent != null)
                s.setParentSpanID(parent.getSpanID());
        }
        //add all annotations
        {
            if(annotations != null)
                s.addAnnotations(annotations);
        }
        s.setSpanID(spanID);
        s.setCorrelationPolicy(getCorrelationPolicy());
        s.setCorrelationID(getCorrelationID());
        s.setDuration(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        //pop scope
        CURRENT_SCOPE.set(parent);
        report(s);
    }
}
