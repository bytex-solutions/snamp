package com.bytex.snamp.core;

import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.internal.annotations.MethodStub;
import com.bytex.snamp.internal.annotations.ThreadSafe;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

import java.util.ResourceBundle;
import java.util.logging.*;

/**
 * Represents proxy logger that connects the underlying {@link java.util.logging.Logger}
 * to the {@link org.osgi.service.log.LogService} service.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@ThreadSafe(false)
public class LoggingScope extends Logger implements AutoCloseable {
    private static final Object[] EMPTY_ARRAY = new String[0];
    private static final Joiner LOG_JOINER = Joiner.on(',').skipNulls();
    private final Logger logger;
    private ServiceHolder<LogService> service;
    private BundleContext context;

    /**
     * Initializes a new logging context that attaches the specified logger
     * to the {@link org.osgi.service.log.LogService} service.
     * @param underlyingLogger The underlying logger. Cannot be {@literal null}.
     * @param context The bundle context.
     */
    public LoggingScope(final Logger underlyingLogger,
                           final BundleContext context) {
        super(underlyingLogger.getName(), underlyingLogger.getResourceBundleName());
        this.context = context == null ? Utils.getBundleContextByObject(this) : context;
        this.service = new ServiceHolder<>(this.context, LogService.class);
        this.logger = underlyingLogger;
    }

    /**
     * Initializes a new logging context that attaches the specified logger
     * to the {@link org.osgi.service.log.LogService} service.
     * @param loggerName The name of the logger to be attached.
     * @param context The bundle context.
     */
    public LoggingScope(final String loggerName,
                        final BundleContext context){
        this(Logger.getLogger(loggerName), context);
    }

    /**
     * Initializes a new logging context that attaches anonymous logger
     * to the {@link org.osgi.service.log.LogService} service.
     * @param context The bundle context. Cannot be {@literal null}.
     */
    public LoggingScope(final BundleContext context){
        this(Logger.getAnonymousLogger(), context);
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
        logger.log(record);
        final Filter theFilter = getFilter();
        if (theFilter == null || theFilter.isLoggable(record) && isLoggable(record.getLevel()))
            service.get().log(transformLogLevel(record.getLevel()),
                    transformRecord(record),
                    record.getThrown());
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

    private static String getCallerInfo(final LogRecord record){
        String callerInfo;
        if(!Strings.isNullOrEmpty(record.getSourceClassName())){
            callerInfo = record.getSourceClassName();
            if(!Strings.isNullOrEmpty(record.getSourceMethodName()))
                callerInfo += "::" + record.getSourceMethodName();
        }
        else if(!Strings.isNullOrEmpty(record.getSourceMethodName()))
            callerInfo = record.getSourceMethodName();
        else callerInfo = "Thread ID " + record.getThreadID();
        return callerInfo;
    }

    /**
     * Returns a textual log message constructed from {@link LogRecord} instance.
     *
     * @param record The record to transform.
     * @return A string representation of the {@link LogRecord} instance.
     */
    protected String transformRecord(final LogRecord record) {
        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        final Object parts[] = {
                getCallerInfo(record),
                String.format(record.getMessage(),
                        MoreObjects.firstNonNull(record.getParameters(), EMPTY_ARRAY)),
                record.getThrown()
        };
        return LOG_JOINER.join(parts);
    }

    /**
     * Provides transformation between {@link Level} and OSGi logging service levels.
     * <p>
     * In the default implementation this method provides the following transformation:
     * <li>
     * <ul>{@link Level#CONFIG} to {@link LogService#LOG_DEBUG}</ul>
     * <ul>{@link Level#WARNING} to {@link LogService#LOG_WARNING}</ul>
     * <ul>{@link Level#SEVERE} to {@link LogService#LOG_ERROR}</ul>
     * <ul>Any other log level into {@link LogService#LOG_INFO}</ul>
     * </li>
     * </p>
     *
     * @param logLevel The log level to transform.
     * @return Log level for OSGi logging service.
     */
    protected int transformLogLevel(final Level logLevel) {
        if (Level.CONFIG.equals(logLevel))
            return LogService.LOG_DEBUG;
        else if (Level.WARNING.equals(logLevel))
            return LogService.LOG_WARNING;
        else if (Level.SEVERE.equals(logLevel))
            return LogService.LOG_ERROR;
        else return LogService.LOG_INFO;
    }

    /**
     * Returns a string representation of this scope.
     * @return A string representation of this scope.
     */
    @Override
    public String toString() {
        return logger.toString();
    }

    /**
     * Determines whether this scope is closed.
     * @return {@literal true}, if this scope is closed; otherwise, {@literal false}.
     */
    public final boolean isClosed(){
        return context == null || service.get() == null;
    }

    final Logger getLogger(){
        return logger;
    }

    final BundleContext getBundleContext(){
        return context;
    }

    @MethodStub
    protected void beforeClose(){

    }

    /**
     * Closes this logging context and releases reference to {@link org.osgi.service.log.LogService}.
     */
    @Override
    public final void close() {
        try {
            beforeClose();
        }
        finally {
            if (context != null)
                service.release(context);
            context = null;
        }
    }
}
