package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.measurements.Span;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class TraceScope implements MeasurementScope {
    private static final ThreadLocal<TraceScope> CURRENT_SCOPE = new ThreadLocal<TraceScope>();

    private final long startTime;
    private final Identifier spanID;
    private final TraceScope parent;
    private final Identifier correlationID;
    private volatile Map<String, String> annotations;

    protected TraceScope(final Identifier correlationID, final Identifier parentSpanID){
        if(correlationID == null)
            throw new IllegalArgumentException("correlationID cannot be null");
        else if(parentSpanID == null)
            throw new IllegalArgumentException("parentSpanID cannot be null");
        else {
            this.spanID = Identifier.randomID();
            this.correlationID = correlationID;
            startTime = System.nanoTime();
            //push scope
            if (Identifier.EMPTY.equals(parentSpanID)) {
                parent = CURRENT_SCOPE.get();
            } else {
                parent = new TraceScope(correlationID, Identifier.EMPTY) {
                    @Override
                    protected void report(final Span s) {
                        TraceScope.this.report(s);
                    }
                };
            }
            CURRENT_SCOPE.set(this);
        }
    }

    private synchronized Map<String, String> getAnnotations(){
        if(annotations == null)
            annotations = new LinkedHashMap<String, String>();
        return annotations;
    }

    /**
     * Adds annotation to this scope.
     * @param name Annotation name.
     * @param value Annotation value.
     */
    public final void addAnnotation(final String name, final String value){
        Map<String, String> userData = annotations;
        if(userData == null)
            userData = getAnnotations();
        userData.put(name, value);
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
    public void close() {
        final Span s = new Span();
        //set parent span
        {
            if (parent != null)
                s.setParentSpanID(parent.getSpanID());
        }
        //add all annotations
        {
            final Map<String, String> userData = annotations;
            if (userData != null)
                s.addAnnotations(userData);
        }
        s.setSpanID(spanID);
        s.setCorrelationID(getCorrelationID());
        s.setDuration(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        //pop scope
        CURRENT_SCOPE.set(parent);
        report(s);
    }
}
