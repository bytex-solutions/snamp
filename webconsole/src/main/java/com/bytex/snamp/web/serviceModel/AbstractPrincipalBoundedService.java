package com.bytex.snamp.web.serviceModel;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.DistributedServices;
import com.bytex.snamp.core.KeyValueStorage;
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
