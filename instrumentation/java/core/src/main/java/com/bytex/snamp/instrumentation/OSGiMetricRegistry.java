package com.bytex.snamp.instrumentation;

import com.bytex.snamp.instrumentation.measurements.Measurement;
import com.bytex.snamp.instrumentation.reporters.Reporter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Represents registry of all metrics.
 * <p />
 *     This class represents an entry point to work with SNAMP instrumentation and measurement reporting in OSGi environment.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public class OSGiMetricRegistry extends MetricRegistry {
    private static final class ReporterProxy implements Reporter{
        private BundleContext context;
        private ServiceReference<Reporter> reporterRef;

        private ReporterProxy(final BundleContext context, final ServiceReference<Reporter> ref){
            this.context = context;
            reporterRef = ref;
        }

        @Override
        public boolean isAsynchronous() {
            final Reporter reporter = context.getService(reporterRef);
            try {
                return reporter.isAsynchronous();
            } finally {
                context.ungetService(reporterRef);
            }
        }

        @Override
        public boolean isConnected() {
            final Reporter reporter = context.getService(reporterRef);
            try {
                return reporter.isConnected();
            } finally {
                context.ungetService(reporterRef);
            }
        }

        @Override
        public void flush() throws IOException {
            final Reporter reporter = context.getService(reporterRef);
            try {
                reporter.flush();
            } finally {
                context.ungetService(reporterRef);
            }
        }

        @Override
        public void report(final Measurement... measurements) throws IOException {
            final Reporter reporter = context.getService(reporterRef);
            try {
                reporter.report(measurements);
            } finally {
                context.ungetService(reporterRef);
            }
        }

        @Override
        public void close() {
            context = null;
            reporterRef = null;
        }
    }

    private static final class ReporterIterator implements Iterator<Reporter>{
        private final Iterator<ServiceReference<Reporter>> references;
        private final BundleContext context;

        private ReporterIterator(final BundleContext context){
            this.context = context;
            final Collection<ServiceReference<Reporter>> references;
            try {
                references = context.getServiceReferences(Reporter.class, null);
            } catch (InvalidSyntaxException e) {
                throw new AssertionError(e);    //should never be happened
            }
            this.references = references.iterator();
        }

        @Override
        public boolean hasNext() {
            return references.hasNext();
        }

        @Override
        public ReporterProxy next() {
            return new ReporterProxy(context, references.next());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class ReporterServiceDiscovery implements Iterable<Reporter>{
        private final BundleContext context;

        ReporterServiceDiscovery(final BundleContext context){
            if(context == null)
                throw new IllegalArgumentException("context cannot be null");
            this.context = context;
        }

        @Override
        public Iterator<Reporter> iterator() {
            return new ReporterIterator(context);
        }
    }

    /**
     * Initialize a new registry using the specified bundle context.
     * @param context The context of the caller bundle. Cannot be {@literal null}.
     */
    public OSGiMetricRegistry(final BundleContext context){
        super(new ReporterServiceDiscovery(context));
    }
}
