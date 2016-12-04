package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.measurements.Span;
import com.bytex.snamp.instrumentation.reporters.Reporter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class SpanReporter extends MeasurementReporter<Span> {
    private interface ChildTraceScopeProvider{
        TraceScope newScope();
    }

    protected SpanReporter(final Iterable<Reporter> reporters, final String name, final Map<String, String> userData) {
        super(reporters, name, userData);
    }

    /**
     * Creates a new trace scope.
     * @param correlationID Correlation identifier.
     * @param parentSpanID Identifier of the parent span.
     * @return Trace scope.
     */
    public TraceScope trace(final Identifier correlationID, final Identifier parentSpanID){
        return new TraceScope(correlationID, parentSpanID) {
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
    public final TraceScope trace(final Identifier correlationID){
        return trace(correlationID, Identifier.EMPTY);
    }

    /**
     * Creates a new trace scope.
     * @return Trace scope.
     */
    public final TraceScope trace(){
        return trace(Identifier.EMPTY);
    }

    private InvocationHandler createHandler(){
        return new InvocationHandler() {
            @Override
            public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                final TraceScope t = trace();
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

    public <T> Object wrap(final T obj, final Class<? super T>... interfaces) {
        return wrapImpl(obj, interfaces);
    }

    @SuppressWarnings("unchecked")
    public final <T> T wrap(final T obj){
        return (T) wrapImpl(obj, obj.getClass().getInterfaces());
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
                return trace(correlationID, parentSpanID);
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
