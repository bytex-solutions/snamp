package com.bytex.snamp.web.serviceModel.logging;

import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;

import javax.ws.rs.Path;

/**
 * Provides notification about logs.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class LogNotifier extends AbstractPrincipalBoundedService<LoggingSettings> implements LogListener {
    /**
     * Represents name of this service.
     */
    public static final String NAME = "logNotifier";

    public LogNotifier() {
        super(LoggingSettings.class);
    }

    /**
     * Listener method called for each LogEntry object created.
     * <p>
     * <p>
     * As with all event listeners, this method should return to its caller as
     * soon as possible.
     *
     * @param entry A {@code LogEntry} object containing log information.
     * @see LogEntry
     */
    @Override
    public void logged(final LogEntry entry) {
        fireWebEvent(new LogEvent(this, entry), (event, settings) -> settings.getLogLevel().shouldBeLogged(entry));
    }

    @Override
    protected LoggingSettings createUserData() {
        return new LoggingSettings();
    }
}
