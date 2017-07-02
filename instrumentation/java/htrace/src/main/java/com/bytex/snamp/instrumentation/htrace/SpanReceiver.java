package com.bytex.snamp.instrumentation.htrace;

import com.bytex.snamp.instrumentation.Identifier;
import com.bytex.snamp.instrumentation.MeasurementReporter;
import com.bytex.snamp.instrumentation.MetricRegistry;
import com.bytex.snamp.instrumentation.measurements.Span;
import com.bytex.snamp.instrumentation.reporters.Reporter;
import org.apache.htrace.core.HTraceConfiguration;
import org.apache.htrace.core.SpanId;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Represents receiver of HTrace spans which are redirected into {@link Reporter}s.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class SpanReceiver extends org.apache.htrace.core.SpanReceiver {
    /**
     * HTrace configuration property indicating that the receiver should load default SNAMP reporters using {@link java.util.ServiceLoader}.
     */
    public static final String USE_DEFAULT_REPORTERS = "snamp.instrumentation.useDefaultReporters";
    /**
     * HTrace configuration property used to specify set of semicolon-separated {@link Reporter} classes.
     */
    public static final String REPORTER_CLASSES = "snamp.instrumentation.reporters";

    private final Iterable<Reporter> reporters;
    private static final Pattern SPLITTER = Pattern.compile(";", Pattern.LITERAL);

    public SpanReceiver(final HTraceConfiguration configuration) throws ReflectiveOperationException {
        final ClassLoader loader = getClass().getClassLoader();
        if (configuration.getBoolean(USE_DEFAULT_REPORTERS, true)) {
            reporters = new MetricRegistry(loader);
        } else {
            final Collection<Reporter> reporters = new LinkedList<>();
            for (final String reporterClassName : SPLITTER.split(configuration.get(REPORTER_CLASSES, ""))) {
                final Class<?> reporterClass = Class.forName(reporterClassName, true, loader);
                if (reporterClass.isAssignableFrom(Reporter.class))
                    reporters.add((Reporter) reporterClass.newInstance());
            }
            this.reporters = reporters;
        }
    }

    public SpanReceiver(final Reporter... reporters){
        this.reporters = Arrays.asList(reporters.clone());
    }

    /**
     * Converts span identifier into SNAMP-compliant identifier.
     * @param spanId Span identifier to convert.
     * @return SNAMP-compliant identifier.
     */
    protected Identifier toIdentifier(final SpanId spanId){
        return Identifier.ofString(spanId.toString());
    }

    /**
     * Called when a Span is stopped and can now be stored.
     *
     * @param htraceSpan The span to store with this SpanReceiver.
     */
    @Override
    public void receiveSpan(final org.apache.htrace.core.Span htraceSpan) {
        final Span span = new Span();
        span.setAnnotations(htraceSpan.getKVAnnotations());
        span.setName(htraceSpan.getDescription());
        span.setDuration(htraceSpan.getAccumulatedMillis(), TimeUnit.MILLISECONDS);
        span.setSpanID(toIdentifier(htraceSpan.getSpanId()));
        span.setCorrelationID(Identifier.ofString(htraceSpan.getTracerId()));
        span.setTimeStamp(htraceSpan.getStartTimeMillis());
        final SpanId[] parents = htraceSpan.getParents();
        if(parents != null && parents.length > 0)
            span.setSpanID(toIdentifier(parents[0]));   //SNAMP doesn't support multiple parents
        MeasurementReporter.report(reporters, span);
    }

    @Override
    public void close() throws IOException {
        if (reporters instanceof Closeable)
            ((Closeable) reporters).close();
        else for (final Reporter reporter : reporters)
            reporter.close();
    }
}
