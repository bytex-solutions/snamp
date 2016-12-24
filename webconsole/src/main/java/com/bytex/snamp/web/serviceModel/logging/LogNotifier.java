package com.bytex.snamp.web.serviceModel.logging;

import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
}
