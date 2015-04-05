package org.snmp4j.log;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class OSGiLogAdapter implements LogAdapter {

    private final String name;

    OSGiLogAdapter(final String name){
        this.name = name;
    }

    /**
     * Checks whether DEBUG level logging is activated for this log adapter.
     *
     * @return <code>true</code> if logging is enabled or <code>false</code> otherwise.
     */
    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    /**
     * Checks whether INFO level logging is activated for this log adapter.
     *
     * @return <code>true</code> if logging is enabled or <code>false</code> otherwise.
     */
    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    /**
     * Checks whether WARN level logging is activated for this log adapter.
     *
     * @return <code>true</code> if logging is enabled or <code>false</code> otherwise.
     */
    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    private BundleContext getBundleContext(){
        return FrameworkUtil.getBundle(getClass()).getBundleContext();
    }

    private static void log(final LogService logger,
                            final int logLevel,
                            final Object msg,
                            final Throwable e){
        if(e == null)
            logger.log(logLevel, msg.toString());
        else
            logger.log(logLevel, msg.toString(), e);
    }

    private void log(final int logLevel, final Object msg, final Throwable e){
        final BundleContext context = getBundleContext();
        final ServiceReference<LogService> logServiceRef = context.getServiceReference(LogService.class);
        if(logServiceRef == null) return;
        final LogService service = context.getService(logServiceRef);
        if(service != null)
        try{
            log(service, logLevel, msg, e);
        }
        finally {
            context.ungetService(logServiceRef);
        }
    }

    /**
     * Logs a debug message.
     *
     * @param message the message to log.
     */
    @Override
    public void debug(final Serializable message) {
        log(LogService.LOG_DEBUG, message, null);
    }

    /**
     * Logs an informational message.
     *
     * @param message the message to log.
     */
    @Override
    public void info(final CharSequence message) {
        log(LogService.LOG_INFO, message, null);
    }

    /**
     * Logs an warning message.
     *
     * @param message the message to log.
     */
    @Override
    public void warn(final Serializable message) {
        log(LogService.LOG_WARNING, message, null);
    }

    /**
     * Logs an error message.
     *
     * @param message the message to log.
     */
    @Override
    public void error(final Serializable message) {
        log(LogService.LOG_ERROR, message, null);
    }

    /**
     * Logs an error message.
     *
     * @param message   the message to log.
     * @param throwable
     */
    @Override
    public void error(final CharSequence message, final Throwable throwable) {
        log(LogService.LOG_ERROR, message, throwable);
    }

    /**
     * Logs a fatal message.
     *
     * @param message the message to log.
     */
    @Override
    public void fatal(final Object message) {
        log(LogService.LOG_ERROR, message, null);
    }

    /**
     * Logs a fatal message.
     *
     * @param message   the message to log.
     * @param throwable
     */
    @Override
    public void fatal(final CharSequence message, final Throwable throwable) {
        log(LogService.LOG_ERROR, message, throwable);
    }

    /**
     * Sets the log level for this log adapter (if applicable).
     *
     * @param level a LogLevel instance.
     * @since 1.6.1
     */
    @Override
    public void setLogLevel(final LogLevel level) {

    }

    /**
     * Returns the log level defined for this log adapter.
     *
     * @return a LogLevel instance.
     * @since 1.6.1
     */
    @Override
    public LogLevel getLogLevel() {
        return LogLevel.ALL;
    }

    /**
     * Returns the log level that is effective for this log adapter.
     * The effective log level is the first log level different from
     * {@link LogLevel#NONE} to the root.
     *
     * @return a LogLevel different than {@link LogLevel#NONE}.
     * @since 1.6.1
     */
    @Override
    public LogLevel getEffectiveLogLevel() {
        return LogLevel.ALL;
    }

    /**
     * Returns the name of the logger.
     *
     * @return the name of the logger.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the log handlers associated with this logger.
     *
     * @return an Iterator of log system dependent log handlers.
     * @since 1.6.1
     */
    @Override
    public Iterator getLogHandler() {
        return Collections.emptyIterator();
    }
}
