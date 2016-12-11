package com.bytex.snamp.webconsole.serviceModel.logging;

import com.bytex.snamp.webconsole.serviceModel.AbstractPrincipalBoundedService;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Dictionary;
import java.util.Objects;

/**
 * Provides notification about logs.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/logging")
public final class LogNotifier extends AbstractPrincipalBoundedService implements LogListener {
    private volatile LogLevel level = LogLevel.ERROR;

    @Path("/level")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public LogLevel getLevel(){
        return level;
    }

    @Path("/level")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void setLevel(final LogLevel value){
        this.level = Objects.requireNonNull(value);
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
        if (level.shouldBeLogged(entry))
            fireWebEvent(new LogEvent(this, entry));
    }

    /**
     * Update the configuration for a Managed Service.
     * <p>
     * <p>
     * When the implementation of {@code updated(Dictionary)} detects any kind
     * of error in the configuration properties, it should create a new
     * {@code ConfigurationException} which describes the problem. This can
     * allow a management system to provide useful information to a human
     * administrator.
     * <p>
     * <p>
     * If this method throws any other {@code Exception}, the Configuration
     * Admin service must catch it and should log it.
     * <p>
     * The Configuration Admin service must call this method asynchronously with
     * the method that initiated the callback. This implies that implementors of
     * Managed Service can be assured that the callback will not take place
     * during registration when they execute the registration in a synchronized
     * method.
     * <p>
     * <p>
     * If the the location allows multiple managed services to be called back
     * for a single configuration then the callbacks must occur in service
     * ranking order. Changes in the location must be reflected by deleting the
     * configuration if the configuration is no longer visible and updating when
     * it becomes visible.
     * <p>
     * <p>
     * If no configuration exists for the corresponding PID, or the bundle has
     * no access to the configuration, then the bundle must be called back with
     * a {@code null} to signal that CM is active but there is no data.
     *
     * @param properties A copy of the Configuration properties, or {@code null}
     *                   . This argument must not contain the "service.bundleLocation"
     *                   property. The value of this property may be obtained from the
     *                   {@code Configuration.getBundleLocation} method.
     * @throws ConfigurationException when the update fails
     * bundle that registered this service
     */
    @Override
    public void updated(final Dictionary<String, ?> properties) throws ConfigurationException {

    }
}
