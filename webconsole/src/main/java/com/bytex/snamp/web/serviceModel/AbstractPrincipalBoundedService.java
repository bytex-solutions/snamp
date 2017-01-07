package com.bytex.snamp.web.serviceModel;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.KeyValueStorage;
import com.bytex.snamp.internal.Utils;
import org.codehaus.jackson.map.ObjectMapper;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.security.Principal;
import java.util.Objects;
import java.util.Optional;

import static com.bytex.snamp.core.KeyValueStorage.JsonRecordView;

/**
 * Represents abstract service for SNAMP Web Console which is bounded to the user's profile data.
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
        final BundleContext context = Utils.getBundleContextOfObject(this);
        final String storageName = userDataType.getName();
        userDataStorage = DistributedServices.getDistributedObject(
                context,
                storageName,
                ClusterMember.PERSISTENT_KV_STORAGE);
    }

    @Path("/settings")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public final USERDATA getUserData(@Context final SecurityContext context) {
        return getUserData(context.getUserPrincipal());
    }

    @Path("/settings")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public final void setUserData(@Context final SecurityContext context, final USERDATA value) {
        setUserData(context.getUserPrincipal(), value);
    }

    @Nonnull
    protected abstract USERDATA createUserData();

    private void setUserData(final USERDATA data, final JsonRecordView record) {
        try (final Writer output = record.createJsonWriter()) {
            mapper.writeValue(output, data);
        } catch (final IOException e){
            throw new UncheckedIOException(e);
        }
    }

    private void setDefaultUserData(final JsonRecordView record) {
        setUserData(createUserData(), record);
    }

    private USERDATA getUserData(final JsonRecordView record) {
        try (final Reader reader = record.getAsJson()) {
            return mapper.readValue(reader, userDataType);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Obtains persistent data associated with the specified user.
     * @param principal User principal.
     * @return Data associated with the specified user.
     */
    protected final USERDATA getUserData(final Principal principal) {
        final JsonRecordView record = userDataStorage.getOrCreateRecord(principal.getName(), JsonRecordView.class, this::setDefaultUserData);
        return getUserData(record);
    }

    protected final USERDATA getUserData(final WebConsoleSession session) {
        final Optional<USERDATA> cachedData = session.getUserData(userDataType);
        return cachedData.isPresent() ? cachedData.get() : getUserData(session.getPrincipal());
    }

    protected final void setUserData(final Principal principal, final USERDATA data) {
        userDataStorage.updateOrCreateRecord(principal.getName(), JsonRecordView.class, record -> setUserData(data, record));
    }
}
