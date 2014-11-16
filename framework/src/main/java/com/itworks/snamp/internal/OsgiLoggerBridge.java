package com.itworks.snamp.internal;

import org.osgi.service.log.LogService;

import java.util.Objects;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Represents allow to operate with {@link LogService} through standard Java logging {@link Logger} class.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class OsgiLoggerBridge extends Handler {
    private final LogService service;

    /**
     * Initializes a new {@link Logger} handler based on the OSGi logging service.
     * @param service a reference to OSGi logging service. Cannot be {@literal null}.
     * @throws IllegalArgumentException service is {@literal null}.
     */
    public OsgiLoggerBridge(final LogService service){
        if(service == null) throw new IllegalArgumentException("service is null.");
        else this.service = service;
    }

    /**
     * Provides transformation between {@link Level} and OSGi logging service levels.
     * <p>
     *     In the default implementation this method provides the following transformation:
     *     <li>
     *         <ul>{@link Level#CONFIG} to {@link LogService#LOG_DEBUG}</ul>
     *         <ul>{@link Level#WARNING} to {@link LogService#LOG_WARNING}</ul>
     *         <ul>{@link Level#SEVERE} to {@link LogService#LOG_ERROR}</ul>
     *         <ul>Any other log level into {@link LogService#LOG_INFO}</ul>
     *     </li>
     * </p>
     * @param logLevel The log level to transform.
     * @return Log level for OSGi logging service.
     */
    protected int transformLogLevel(final Level logLevel){
        if(Objects.equals(logLevel, Level.CONFIG))
            return LogService.LOG_DEBUG;
        else if(Objects.equals(logLevel, Level.WARNING))
            return LogService.LOG_WARNING;
        else if(Objects.equals(logLevel, Level.SEVERE))
            return LogService.LOG_ERROR;
        else return LogService.LOG_INFO;
    }

    /**
     * Returns a textual log message constructed from {@link LogRecord} instance.
     * @param record The record to transform.
     * @return A string representation of the {@link LogRecord} instance.
     */
    protected String transformRecord(final LogRecord record){
        return record.getMessage();
    }

    /**
     * Publish a <tt>LogRecord</tt>.
     * <p/>
     * The logging request was made initially to a <tt>Logger</tt> object,
     * which initialized the <tt>LogRecord</tt> and forwarded it here.
     * <p/>
     * The <tt>Handler</tt>  is responsible for formatting the message, when and
     * if necessary.  The formatting should include localization.
     *
     * @param record description of the log event. A null record is
     *               silently ignored and is not published
     */
    @Override
    public final void publish(final LogRecord record) {
        service.log(transformLogLevel(record.getLevel()), transformRecord(record), record.getThrown());
    }

    /**
     * Flush any buffered output.
     */
    @Override
    public void flush() {

    }

    /**
     * Releases all resources associated with this bridge.
     */
    @Override
    public void close() {

    }

    /**
     * Adds log handler based on OSGi log service to the specified logger.
     * @param logger The logger to be connected to the {@link org.osgi.service.log.LogService} service.
     * @param logService OSGi logger service to wrap.
     */
    public static void connectToLogService(final Logger logger, final LogService logService){
        final OsgiLoggerBridge bridge = new OsgiLoggerBridge(logService);
        logger.addHandler(bridge);
    }

    /**
     * Gets a logger with the specified name that wraps the OSGi logging service.
     * @param name The name of the logger.
     * @param logService OSGi logger service to wrap.
     * @return A logger that can be used to transfer logs into OSGi logging service.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static Logger connectToLogService(final String name, final LogService logService){
        final Logger logger = Logger.getLogger(name);
        if(logService != null)
            connectToLogService(logger, logService);
        return logger;
    }

    /**
     * Removes OSGi logging service bridge from the specified logger.
     * @param logger The logger connected to OSGi logging service.
     */
    public static void disconnectFromLogService(final Logger logger) {
        for(final Handler handler: logger.getHandlers())
            if(handler instanceof OsgiLoggerBridge)
                logger.removeHandler(handler);
    }
}
