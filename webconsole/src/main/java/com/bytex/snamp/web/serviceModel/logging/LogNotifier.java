package com.bytex.snamp.web.serviceModel.logging;

import com.bytex.snamp.concurrent.GroupedThreadFactory;
import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

import javax.ws.rs.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Provides notification about logs.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class LogNotifier extends AbstractPrincipalBoundedService<LoggingSettings> implements WebConsoleLogService {
    private final ExecutorService executor;

    /**
     * Represents name of this service.
     */
    public static final String NAME = "logNotifier";

    public LogNotifier() {
        super(LoggingSettings.class);
        final ThreadFactory factory = new GroupedThreadFactory("LogNotifierThreadGroup", Thread.NORM_PRIORITY - 1);
        executor = Executors.newSingleThreadExecutor(factory);
    }

    /**
     * Log in <code>Appender</code> specific way. When appropriate,
     * Loggers will call the <code>doAppend</code> method of appender
     * implementations in order to log.
     *
     * @param entry The PaxLoggingEvent that has occurred.
     */
    @Override
    public void doAppend(final PaxLoggingEvent entry) {
        fireWebEvent(entry, (event, settings) -> settings.shouldBeLogged(entry.getLevel()), LogEvent::new, executor);
    }

    @Override
    protected LoggingSettings createUserData() {
        return new LoggingSettings();
    }

    @Override
    public void close() throws Exception {
        super.close();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
}
