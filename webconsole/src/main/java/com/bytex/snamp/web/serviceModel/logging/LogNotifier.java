package com.bytex.snamp.web.serviceModel.logging;

import com.bytex.snamp.MapUtils;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;

import javax.ws.rs.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Provides notification about logs.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class LogNotifier extends AbstractPrincipalBoundedService<LoggingSettings> implements WebConsoleLogService {
    private final ExecutorService executor;
    private final Supplier<String> wcBundleName;

    /**
     * Represents name of this service.
     */
    public static final String NAME = "logNotifier";
    public static final String URL_CONTEXT = "/logging";

    public LogNotifier(final ExecutorService executor) {
        super(LoggingSettings.class);
        this.executor = Objects.requireNonNull(executor);
        final String bundleName = Utils.getBundleContextOfObject(this).getBundle().getSymbolicName();
        wcBundleName = () -> bundleName;
    }

    private boolean logNotFromWebConsole(final PaxLoggingEvent event) {
        final Map logProperties = event.getProperties();
        if (logProperties == null)
            return false;
        final String bundleName = MapUtils.getValue(logProperties, "bundle.name", String.class, wcBundleName);
        return Objects.equals(bundleName, wcBundleName.get());
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
        if (logNotFromWebConsole(entry))
            fireWebEvent(entry, (event, settings) -> settings.shouldBeLogged(entry.getLevel()), LogEvent::new, executor);
    }

    @Override
    protected LoggingSettings createUserData() {
        return new LoggingSettings();
    }
}
