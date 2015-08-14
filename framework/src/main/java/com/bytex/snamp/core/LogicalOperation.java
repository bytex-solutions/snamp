package com.bytex.snamp.core;

import com.bytex.snamp.TimeSpan;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import org.osgi.framework.BundleContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class LogicalOperation extends LoggingScope {
    static final CorrelationIdentifierGenerator CORREL_ID_GEN = new DefaultCorrelationIdentifierGenerator();
    private static final Joiner.MapJoiner TO_STRING_JOINER = Joiner.on(',').withKeyValueSeparator("=");

    /**
     * Represents correlation identifier generator.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public interface CorrelationIdentifierGenerator{
        /**
         * Generates a new unique correlation identifier.
         * @return A new unique correlation identifier.
         */
        long generate();
    }

    protected static final class DefaultCorrelationIdentifierGenerator extends AtomicLong implements CorrelationIdentifierGenerator{
        private static final long serialVersionUID = 1744163230081925999L;

        public DefaultCorrelationIdentifierGenerator(final long initialValue){
            super(initialValue);
        }

        public DefaultCorrelationIdentifierGenerator(){
            this(0L);
        }

        @Override
        public long generate() {
            return getAndIncrement();
        }
    }

    private final String name;
    private final long correlationID;
    private final Stopwatch timer;
    private boolean initialized = false;

    private LogicalOperation(final Logger logger,
                             final String name,
                             final long correlationID,
                             final BundleContext context){
        super(logger, context);
        this.name = Objects.requireNonNull(name);
        this.correlationID = correlationID;
        this.timer = Stopwatch.createStarted();
        entering(null, name);
        initialized = true;
    }

    /**
     * Initializes a new logical operation.
     * @param logger The underlying logger. Cannot be {@literal null}.
     * @param name The name of the logical operation.
     * @param correlationID The correlation identifier generator.
     * @param context The bundle context. May be {@literal null}.
     */
    public LogicalOperation(final Logger logger,
                            final String name,
                               final CorrelationIdentifierGenerator correlationID,
                            final BundleContext context) {
        this(logger,
                name,
                correlationID != null ? correlationID.generate() : -1L,
                context);
    }

    /**
     * Initializes a new logical operation.
     * @param logger The underlying logger. Cannot be {@literal null}.
     * @param name The name of the logical operation.
     * @param correlationID The correlation identifier generator.
     */
    protected LogicalOperation(final Logger logger,
                            final String name,
                            final CorrelationIdentifierGenerator correlationID) {
        this(logger,
                name,
                correlationID != null ? correlationID.generate() : -1L,
                null);
    }

    /**
     * Initializes a new logical operation that is connected with the specified parent operation.
     * @param name The name of the logical operation.
     * @param parent The parent logical operation. Cannot be {@literal null}.
     */
    public LogicalOperation(final String name,
                            final LogicalOperation parent) {
        this(parent.getLogger(),
                name,
                parent.getCorrelationID(),
                parent.getBundleContext());
    }

    /**
     * Gets correlation identifier.
     * @return The correlation identifier.
     */
    public final long getCorrelationID(){
        return correlationID;
    }

    /**
     * Gets duration of logical operation execution.
     * @param desiredUnit The desired time measurement unit of returned value.
     * @return The duration of logical operation execution.
     */
    public final long getDuration(final TimeUnit desiredUnit){
        return timer.elapsed(desiredUnit);
    }

    /**
     * Gets duration of logical operation execution.
     * @return The duration of logical operation execution.
     */
    public final TimeSpan getDuration(){
        return new TimeSpan(getDuration(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
    }

    /**
     * Returns a textual log message constructed from {@link LogRecord} instance.
     *
     * @param record The record to transform.
     * @return A string representation of the {@link LogRecord} instance.
     */
    @Override
    protected final String transformRecord(final LogRecord record) {
        record.setSourceMethodName(name);
        String result = super.transformRecord(record);
        if(initialized)
            result += ". Context: " + toString();
        return result;
    }

    /**
     * Collects string data used to create textual representation of this operation.
     * @param output An output map to populate with string data.
     */
    protected void collectStringData(final Map<String, Object> output) {
        output.put("name", name);
        output.put("duration", timer);
        output.put("correlationID", correlationID);
    }

    /**
     * Returns a string representation of this logical operation.
     * @return A string representation of this logical operation.
     */
    @Override
    public final String toString() {
        final Map<String, Object> result = new HashMap<>(10);
        collectStringData(result);
        return TO_STRING_JOINER.join(result);
    }

    @Override
    protected final void beforeClose() {
        exiting(null, name);
        timer.stop();
        initialized = false;
    }
}
