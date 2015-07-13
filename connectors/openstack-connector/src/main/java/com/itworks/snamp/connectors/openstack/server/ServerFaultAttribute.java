package com.itworks.snamp.connectors.openstack.server;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Fault;
import org.openstack4j.model.compute.Server;

import javax.management.openmbean.*;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ServerFaultAttribute extends AbstractServerAttribute<CompositeData> {
    public static final String NAME = "fault";
    static final String DESCRIPTION = "The last fault of the server";

    private static final String CODE = "code";
    private static final String MESSAGE = "message";
    private static final String DETAILS = "details";
    private static final String CREATED = "created";

    static final CompositeType TYPE = Utils.interfaceStaticInitialize(new Callable<CompositeType>() {
        @Override
        public CompositeType call() throws OpenDataException {
            return new CompositeTypeBuilder("ServerFault", "Information about server fault")
                    .addItem(CODE, "Fault code", SimpleType.INTEGER)
                    .addItem(MESSAGE, "Fault message", SimpleType.STRING)
                    .addItem(DETAILS, "Details about fault", SimpleType.STRING)
                    .addItem(CREATED, "Fault timestamp", SimpleType.DATE)
                    .build();
        }
    });
    private static final Fault EMPTY_FAULT = new Fault() {
        @Override
        public int getCode() {
            return 0;
        }

        @Override
        public String getMessage() {
            return "";
        }

        @Override
        public String getDetails() {
            return "";
        }

        @Override
        public Date getCreated() {
            return new Date();
        }
    };

    public ServerFaultAttribute(final String serverID,
                                final String attributeID,
                                final AttributeDescriptor descriptor,
                                final OSClient client){
        super(serverID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static CompositeData getValueCore(final Server srv) throws OpenDataException {
        Fault f = srv.getFault();
        if (f == null) f = EMPTY_FAULT;
        return new CompositeDataSupport(TYPE, ImmutableMap.<String, Object>of(
                CODE, f.getCode(),
                MESSAGE, MoreObjects.firstNonNull(f.getMessage(), ""),
                DETAILS, MoreObjects.firstNonNull(f.getDetails(), ""),
                CREATED, MoreObjects.firstNonNull(f.getCreated(), new Date())
        ));
    }

    @Override
    protected CompositeData getValue(final Server srv) throws OpenDataException {
        return getValueCore(srv);
    }
}
