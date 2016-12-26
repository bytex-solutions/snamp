package com.bytex.snamp.web.serviceModel;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.KeyValueStorage;
import com.bytex.snamp.core.LoggerProvider;
import com.bytex.snamp.internal.Utils;
import org.codehaus.jackson.map.ObjectMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.security.Principal;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.bytex.snamp.core.KeyValueStorage.JsonRecordView;

/**
 * Represents
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractPrincipalBoundedService<USERDATA> extends AbstractWebConsoleService {
    private final KeyValueStorage userDataStorage;
    private final ObjectMapper mapper;
    private final Class<USERDATA> userDataType;

    protected AbstractPrincipalBoundedService(final Class<USERDATA> userDataType) {
        mapper = new ObjectMapper();
        this.userDataType = Objects.requireNonNull(userDataType);
        this.userDataStorage = DistributedServices.getDistributedObject(
                Utils.getBundleContextOfObject(this),
                userDataType.getName(),
                ClusterMember.PERSISTENT_KV_STORAGE);
    }

    @Path("/settings")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public final USERDATA getUserData(@Context final SecurityContext context) throws IOException {
        return getUserData(context.getUserPrincipal());
    }

    @Path("/settings")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public final void setUserData(@Context final SecurityContext context, final USERDATA value) throws IOException {
        setUserData(context.getUserPrincipal(), value);
    }

    private Logger getLogger(){
        return LoggerProvider.getLoggerForObject(this);
    }

    protected final void fireWebEvent(final Predicate<? super Principal> filter, final Function<? super USERDATA, ? extends WebEvent> eventProvider) {
        fireWebEvent(listener -> {
            if (filter.test(listener.getPrincipal())) {
                final WebEvent event;
                try {
                    final USERDATA userData = getUserData(listener.getPrincipal());
                    event = eventProvider.apply(userData);
                } catch (final IOException e) {
                    getLogger().log(Level.SEVERE, String.format("Failed to deliver event for user %s", listener.getPrincipal()), e);
                    return;
                }
                listener.accept(event);
            }
        });
    }

    protected final void fireWebEvent(final WebEvent event, final BiPredicate<? super Principal, ? super USERDATA> filter) {
        fireWebEvent(listener -> {
            final USERDATA userData;
            try {
                userData = getUserData(listener.getPrincipal());
            } catch (final IOException e) {
                getLogger().log(Level.SEVERE, String.format("Failed to deliver event %s for user %s", event, listener.getPrincipal()), e);
                return;
            }
            if (filter.test(listener.getPrincipal(), userData))
                listener.accept(event);
        });
    }

    protected abstract USERDATA createUserData();

    private void setUserData(final USERDATA data, final JsonRecordView record) throws IOException {
        try (final Writer output = record.createJsonWriter()) {
            mapper.writeValue(output, data);
        }
    }

    private void setDefaultUserData(final JsonRecordView record) throws IOException {
        final USERDATA data = createUserData();
        if(data == null)
            throw new IOException("User data cannot be null");
        setUserData(data, record);
    }

    private USERDATA getUserData(final JsonRecordView record) throws IOException {
        try(final Reader reader = record.getAsJson()){
            return mapper.readValue(reader, userDataType);
        }
    }

    /**
     * Obtains persistent data associated with the specified user.
     * @param principal User principal.
     * @return Data associated with the specified user.
     * @throws IOException Unable to read persistent data.
     */
    public final USERDATA getUserData(final Principal principal) throws IOException {
        final JsonRecordView record = userDataStorage.getOrCreateRecord(principal.getName(), JsonRecordView.class, this::setDefaultUserData);
        return getUserData(record);
    }

    public final void setUserData(final Principal principal, final USERDATA data) throws IOException {
        userDataStorage.updateOrCreateRecord(principal.getName(), JsonRecordView.class, record -> setUserData(data, record));
    }
}
