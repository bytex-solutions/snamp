package com.bytex.snamp.core;

import com.bytex.snamp.SafeCloseable;
import com.bytex.snamp.TimeSpan;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.*;

/**
 * Logical operation represented as logging scope in which all log messages are correlated.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
public class LogicalOperation extends Logger implements SafeCloseable {
    static final CorrelationIdentifierGenerator CORREL_ID_GEN = new DefaultCorrelationIdentifierGenerator();
    private static final Joiner.MapJoiner TO_STRING_JOINER = Joiner.on(',').withKeyValueSeparator("=");

    /**
     * Represents correlation identifier generator.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.2
     */
    public interface CorrelationIdentifierGenerator{
        /**
         * Generates a new unique correlation identifier.
         * @return A new unique correlation identifier.
         */
        long generate();
    }

    /**
     * Represents default CorrelID generator.
     * This class cannot be inherited.
     */
    protected static final class DefaultCorrelationIdentifierGenerator extends AtomicLong implements CorrelationIdentifierGenerator{
        private static final long serialVersionUID = 1744163230081925999L;

        public DefaultCorrelationIdentifierGenerator(final long initialValue){
            super(initialValue);
        }

        public DefaultCorrelationIdentifierGenerator(){
            this(0L);
        }

        /**
         * Generates a new unique correlation identifier.
         * @return A new unique correlation identifier.
         */
        @Override
        public long generate() {
            return getAndIncrement();
        }
    }

    private final String operationName;
    private final long correlationID;
    private final Stopwatch timer;
    private final Logger logger;

    private LogicalOperation(final Logger logger,
                             final String operationName,
                             final long correlationID){
        super(logger.getName(), logger.getResourceBundleName());
        this.operationName = Objects.requireNonNull(operationName);
        this.correlationID = correlationID;
        this.timer = Stopwatch.createStarted();
        this.logger = logger;
        logger.entering(null, operationName);
    }

    /**
     * Initializes a new logical operation.
     * @param logger The underlying logger. Cannot be {@literal null}.
     * @param operationName The name of the logical operation.
     * @param correlationID The correlation identifier generator.
     */
    public LogicalOperation(final Logger logger,
                            final String operationName,
                               final CorrelationIdentifierGenerator correlationID) {
        this(logger, operationName, correlationID != null ? correlationID.generate() : -1L);
    }

    /**
     * Initializes a new logical operation.
     * @param loggerName Name of the underlying logger. Cannot be {@literal null}.
     * @param operationName The name of the logical operation.
     * @param correlationID The correlation identifier generator.
     */
    public LogicalOperation(final String loggerName,
                            final String operationName,
                            final CorrelationIdentifierGenerator correlationID){
        this(Logger.getLogger(loggerName), operationName, correlationID);
    }

    /**
     * Initializes a new logical operation that is connected with the specified parent operation.
     * @param operationName The name of the logical operation.
     * @param parent The parent logical operation. Cannot be {@literal null}.
     */
    public LogicalOperation(final String operationName,
                            final LogicalOperation parent) {
        this(parent.logger, operationName, parent.getCorrelationID());
    }

    /**
     * Get the current filter for this Logger.
     *
     * @return a filter object (may be null)
     */
    @Override
    public final Filter getFilter() {
        return logger.getFilter();
    }

    /**
     * Set a filter to control output on this Logger.
     * <p/>
     * After passing the initial "level" check, the Logger will
     * call this Filter to check if a log record should really
     * be published.
     *
     * @param newFilter a filter object (may be null)
     * @throws SecurityException if a security manager exists and if
     *                           the caller does not have LoggingPermission("control").
     */
    @Override
    public final void setFilter(final Filter newFilter) throws SecurityException {
        logger.setFilter(newFilter);
    }

    /**
     * Get the Handlers associated with this logger.
     * <p/>
     *
     * @return an array of all registered Handlers
     */
    @Override
    public final Handler[] getHandlers() {
        return logger.getHandlers();
    }

    /**
     * Set the parent for this Logger.  This method is used by
     * the LogManager to update a Logger when the namespace changes.
     * <p/>
     * It should not be called from application code.
     * <p/>
     *
     * @param parent the new parent logger
     * @throws SecurityException if a security manager exists and if
     *                           the caller does not have LoggingPermission("control").
     */
    @Override
    public final void setParent(final Logger parent) {
        logger.setParent(parent);
    }

    /**
     * Return the parent for this Logger.
     * <p/>
     * This method returns the nearest extant parent in the namespace.
     * Thus if a Logger is called "a.b.c.d", and a Logger called "a.b"
     * has been created but no logger "a.b.c" exists, then a call of
     * getParent on the Logger "a.b.c.d" will return the Logger "a.b".
     * <p/>
     * The result will be null if it is called on the root Logger
     * in the namespace.
     *
     * @return nearest existing parent Logger
     */
    @Override
    public final Logger getParent() {
        return logger.getParent();
    }

    /**
     * Set the log level specifying which message levels will be
     * logged by this logger.  Message levels lower than this
     * value will be discarded.  The level value Level.OFF
     * can be used to turn off logging.
     * <p/>
     * If the new level is null, it means that this node should
     * inherit its level from its nearest ancestor with a specific
     * (non-null) level value.
     *
     * @param newLevel the new value for the log level (may be null)
     * @throws SecurityException if a security manager exists and if
     *                           the caller does not have LoggingPermission("control").
     */
    @Override
    public final void setLevel(final Level newLevel) throws SecurityException {
        logger.setLevel(newLevel);
    }

    /**
     * Get the log Level that has been specified for this Logger.
     * The result may be null, which means that this logger's
     * effective level will be inherited from its parent.
     *
     * @return this Logger's level
     */
    @Override
    public final Level getLevel() {
        return logger.getLevel();
    }

    /**
     * Retrieve the localization resource bundle name for this
     * logger.  Note that if the result is null, then the Logger
     * will use a resource bundle name inherited from its parent.
     *
     * @return localization bundle name (may be null)
     */
    @Override
    public final String getResourceBundleName() {
        return logger.getResourceBundleName();
    }

    /**
     * Retrieve the localization resource bundle for this
     * logger for the current default locale.  Note that if
     * the result is null, then the Logger will use a resource
     * bundle inherited from its parent.
     *
     * @return localization bundle (may be null)
     */
    @Override
    public final ResourceBundle getResourceBundle() {
        return logger.getResourceBundle();
    }

    /**
     * Check if a message of the given level would actually be logged
     * by this logger.  This check is based on the Loggers effective level,
     * which may be inherited from its parent.
     *
     * @param level a message logging level
     * @return true if the given message level is currently being logged.
     */
    @Override
    public final boolean isLoggable(final Level level) {
        return logger.isLoggable(level);
    }

    /**
     * Get the name for this logger.
     *
     * @return logger name.  Will be null for anonymous Loggers.
     */
    @Override
    public final String getName() {
        return logger.getName();
    }

    /**
     * Add a log Handler to receive logging messages.
     * <p/>
     * By default, Loggers also send their output to their parent logger.
     * Typically the root Logger is configured with a set of Handlers
     * that essentially act as default handlers for all loggers.
     *
     * @param handler a logging Handler
     * @throws SecurityException if a security manager exists and if
     *                           the caller does not have LoggingPermission("control").
     */
    @Override
    public final void addHandler(final Handler handler) throws SecurityException {
        logger.addHandler(handler);
    }

    /**
     * Remove a log Handler.
     * <p/>
     * Returns silently if the given Handler is not found or is null
     *
     * @param handler a logging Handler
     * @throws SecurityException if a security manager exists and if
     *                           the caller does not have LoggingPermission("control").
     */
    @Override
    public final void removeHandler(final Handler handler) throws SecurityException {
        logger.removeHandler(handler);
    }

    /**
     * Specify whether or not this logger should send its output
     * to its parent Logger.  This means that any LogRecords will
     * also be written to the parent's Handlers, and potentially
     * to its parent, recursively up the namespace.
     *
     * @param useParentHandlers true if output is to be sent to the
     *                          logger's parent.
     * @throws SecurityException if a security manager exists and if
     *                           the caller does not have LoggingPermission("control").
     */
    @Override
    public final void setUseParentHandlers(final boolean useParentHandlers) {
        logger.setUseParentHandlers(useParentHandlers);
    }

    /**
     * Discover whether or not this logger is sending its output
     * to its parent logger.
     *
     * @return true if output is to be sent to the logger's parent
     */
    @Override
    public final boolean getUseParentHandlers() {
        return logger.getUseParentHandlers();
    }

    /**
     * Log a LogRecord.
     * <p/>
     * All the other logging methods in this class call through
     * this method to actually perform any logging.  Subclasses can
     * override this single method to capture all log activity.
     *
     * @param record the LogRecord to be published
     */
    @Override
    public final void log(final LogRecord record) {
        record.setSourceMethodName(operationName);
        record.setMessage(record.getMessage() + " Context: " + toString());
        logger.log(record);
    }

    public final void log(final Level level,
                          final String format,
                          final Object param1,
                          final Throwable e){
        super.log(level, String.format(format, param1), e);
    }

    public final void log(final Level level,
                          final String format,
                          final Object param1,
                          final Object param2,
                          final Throwable e){
        super.log(level, String.format(format, param1, param2), e);
    }

    public final void log(final Level level,
                          final String format,
                          final Object param1,
                          final Object param2,
                          final Object param3,
                          final Throwable e){
        super.log(level, String.format(format, param1, param2, param3), e);
    }

    public final void log(final Level level,
                          final String format,
                          final Object param1,
                          final Object param2,
                          final Object param3,
                          final Object param4,
                          final Throwable e){
        super.log(level, String.format(format, param1, param2, param3, param4), e);
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
        return TimeSpan.ofNanos(getDuration(TimeUnit.NANOSECONDS));
    }

    /**
     * Collects string data used to create textual representation of this operation.
     * @param output An output map to populate with string data.
     */
    protected void collectStringData(final Map<String, Object> output) {
        output.put("name", operationName);
        output.put("duration", timer);
        output.put("correlationID", correlationID);
    }

    /**
     * Returns a string representation of this logical operation.
     * @return A string representation of this logical operation.
     */
    @Override
    public final String toString() {
        final Map<String, Object> result = Maps.newHashMap();
        collectStringData(result);
        return TO_STRING_JOINER.join(result);
    }

    /**
     * Closes scope of the logical operation.
     */
    @Override
    public final void close() {
        exiting(null, operationName);
        timer.stop();
    }
}
