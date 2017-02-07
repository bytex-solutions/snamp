package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.measurements.Span;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    private final String moduleName;
    private Map<String, String> annotations;

    private TraceScope(final Identifier correlationID, final Identifier spanID, final Identifier parentSpanID, final String moduleName) {
        this.moduleName = Objects.requireNonNull(moduleName);
        this.spanID = Objects.requireNonNull(spanID);
        this.correlationID = Objects.requireNonNull(correlationID);
        startTime = System.nanoTime();
        //push scope
        parent = parentSpanID.isEmpty() ? CURRENT_SCOPE.get() : ephemeralScope(correlationID, parentSpanID);
        CURRENT_SCOPE.set(this);
    }

    protected TraceScope(final Identifier correlationID, final Identifier parentSpanID, final String moduleName) {
        this(correlationID, Identifier.randomID(), parentSpanID, moduleName);
    }

    private static TraceScope ephemeralScope(final Identifier correlationID, final Identifier spanID){
        return new TraceScope(correlationID, spanID, Identifier.EMPTY, "") {
            @Override
            protected void report(final Span s) {
                //ephemeral scope cannot report anything
            }
        };
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

    /**
     * Gets correlation ID associated with this scope.
     * @return Correlation ID associated with this scope.
     */
    public final Identifier getCorrelationID() {
        return correlationID.isEmpty() && parent != null ? parent.getCorrelationID() : correlationID;
    }

    /**
     * Gets identifier of the span associated with this scope.
     * @return Identifier of the span associated with this scope.
     */
    public final Identifier getSpanID(){
        return spanID;
    }

    /**
     * Gets reporting module.
     * @return Reporting module name.
     */
    public final String getModule() {
        return moduleName.isEmpty() && parent != null ? parent.getModule() : moduleName;
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
        if (parent != null)
            s.setParentSpanID(parent.getSpanID());
        //add all annotations
        if (annotations != null)
            s.addAnnotations(annotations);
        s.setSpanID(spanID);
        s.setModuleName(moduleName);
        s.setCorrelationID(getCorrelationID());
        s.setDuration(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        //pop scope
        CURRENT_SCOPE.set(parent);
        report(s);
    }
}
