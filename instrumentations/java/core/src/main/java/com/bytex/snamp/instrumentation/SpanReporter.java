package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.measurements.CorrelationPolicy;
import com.bytex.snamp.instrumentation.measurements.Span;
import com.bytex.snamp.instrumentation.reporters.Reporter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Represents reporter of {@link Span}s.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class SpanReporter extends MeasurementReporter<Span> {
    private interface ChildTraceScopeProvider{
        TraceScope newScope();
    }

    private CorrelationPolicy correlationPolicy;

    protected SpanReporter(final Iterable<Reporter> reporters,
                           final String name,
                           final Map<String, String> userData) {
        super(reporters, name, userData);
        correlationPolicy = Span.DEFAULT_CORRELATION_POLICY;
    }

    /**
     * Defines policy for correlation of all spans reported by this object.
     * @param value A new correlation policy. Cannot be {@literal null}.
     * @return This reporter.
     */
    public SpanReporter correlationPolicy(final CorrelationPolicy value){
        if(value == null)
            throw new IllegalArgumentException("Correlation policy cannot be null");
        correlationPolicy = value;
        return this;
    }

    /**
     * Creates a new trace scope.
     * @param correlationID Correlation identifier.
     * @param parentSpanID Identifier of the parent span.
     * @return Trace scope.
     */
    public TraceScope beginTrace(final Identifier correlationID, final Identifier parentSpanID){
        return new TraceScope(correlationID, correlationPolicy, parentSpanID) {
            @Override
            protected void report(final Span s) {
                SpanReporter.super.report(s);
            }
        };
    }

    /**
     * Creates a new trace scope.
     * @param correlationID Correlation identifier.
     * @return Trace scope.
     */
    public final TraceScope beginTrace(final Identifier correlationID){
        return beginTrace(correlationID, Identifier.EMPTY);
    }

    /**
     * Creates a new trace scope.
     * @return Trace scope.
     */
    public final TraceScope beginTrace(){
        return beginTrace(Identifier.EMPTY);
    }

    private InvocationHandler createHandler(){
        return new InvocationHandler() {
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                final TraceScope t = beginTrace();
                try {
                    return method.invoke(proxy, args);
                } finally {
                    t.close();
                }
            }
        };
    }

    private Object wrapImpl(final Object obj, final Class<?>... interfaces){
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(), interfaces, createHandler());
    }

    /**
     * Wraps object into traceable object in which invocation of each method will be traced.
     * @param obj An object to wrap. Cannot be {@literal null}.
     * @param interfaces A set of interfaces implemented by object to be instrumented with tracers.
     * @param <T> Type of object to be instrumented.
     * @return Instrumented object.
     */
    public <T> Object wrap(final T obj, final Class<? super T>... interfaces) {
        return wrapImpl(obj, interfaces.length == 0 ? obj.getClass().getInterfaces() : interfaces);
    }

    private ChildTraceScopeProvider getTraceScopeProvider(){
        final TraceScope parent = TraceScope.current();
        final Identifier correlationID, parentSpanID;
        if(parent == null)
            correlationID = parentSpanID = Identifier.EMPTY;
        else {
            correlationID = parent.getCorrelationID();
            parentSpanID = parent.getSpanID();
        }

        return new ChildTraceScopeProvider() {
            @Override
            public TraceScope newScope() {
                return beginTrace(correlationID, parentSpanID);
            }
        };
    }

    private static <V> Callable<V> wrap(final Callable<V> obj, final ChildTraceScopeProvider scopeProvider){
        return new Callable<V>() {
            @Override
            public V call() throws Exception {
                final TraceScope trace = scopeProvider.newScope();
                try {
                    return obj.call();
                } finally {
                    trace.close();
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
    public final <V> Callable<V> wrap(final Callable<V> obj){
        return wrap(obj, getTraceScopeProvider());
    }

    private static Runnable wrap(final Runnable obj, final ChildTraceScopeProvider scopeProvider){
        return new Runnable() {
            @Override
            public void run() {
                final TraceScope trace = scopeProvider.newScope();
                try {
                    obj.run();
                } finally {
                    trace.close();
                }
            }
        };
    }

    public final Runnable wrap(final Runnable obj){
        return wrap(obj, getTraceScopeProvider());
    }
}
