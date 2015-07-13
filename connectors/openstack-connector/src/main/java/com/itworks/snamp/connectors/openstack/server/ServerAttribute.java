package com.itworks.snamp.connectors.openstack.server;

import com.google.common.collect.Maps;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.compute.ServerService;
import org.openstack4j.model.compute.Server;

import javax.management.openmbean.*;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Represents full information about server.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class ServerAttribute extends AbstractServerAttribute<CompositeData> {
    public static final String NAME = "serverInfo";
    private static final String DESCRIPTION = "Full information about server";
    private static final String ID_NAME = "serverID";
    private static final String ID_DESCR = "ID of the server";

    static final CompositeType TYPE = Utils.interfaceStaticInitialize(new Callable<CompositeType>() {
        @Override
        public CompositeType call() throws OpenDataException {
            return new CompositeTypeBuilder("Server", "OpenStack Server")
                    .addItem(ServerAllDiagnosticsAttribute.NAME, ServerAllDiagnosticsAttribute.DESCRIPTION, ServerAllDiagnosticsAttribute.TYPE)
                    .addItem(ServerFaultAttribute.NAME, ServerFaultAttribute.DESCRIPTION, ServerFaultAttribute.TYPE)
                    .addItem(ServerFlavorAttribute.NAME, ServerFlavorAttribute.DESCRIPTION, ServerFlavorAttribute.TYPE)
                    .addItem(ServerHostAttribute.NAME, ServerHostAttribute.DESCRIPTION, ServerHostAttribute.TYPE)
                    .addItem(ServerInstanceNameAttribute.NAME, ServerInstanceNameAttribute.DESCRIPTION, ServerInstanceNameAttribute.TYPE)
                    .addItem(ServerLaunchedAtAttribute.NAME, ServerLaunchedAtAttribute.DESCRIPTION, ServerLaunchedAtAttribute.TYPE)
                    .addItem(ServerNameAttribute.NAME, ServerNameAttribute.DESCRIPTION, ServerNameAttribute.TYPE)
                    .addItem(ServerPowerStateAttribute.NAME, ServerPowerStateAttribute.DESCRIPTION, ServerPowerStateAttribute.TYPE)
                    .addItem(ServerStatusAttribute.NAME, ServerStatusAttribute.DESCRIPTION, ServerStatusAttribute.TYPE)
                    .addItem(ServerTerminatedAtAttribute.NAME, ServerTerminatedAtAttribute.DESCRIPTION, ServerTerminatedAtAttribute.TYPE)
                    .addItem(ServerVmStateAttribute.NAME, ServerVmStateAttribute.DESCRIPTION, ServerVmStateAttribute.TYPE)
                    .addItem(ID_NAME, ID_DESCR, SimpleType.STRING)
                    .build();
        }
    });

    public ServerAttribute(final String serverID,
                           final String attributeID,
                           final AttributeDescriptor descriptor,
                           final OSClient client){
        super(serverID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static CompositeData getValueCore(final ServerService sevice,
                                      final Server srv) throws OpenDataException {
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(TYPE.keySet().size());
        result.put(ServerAllDiagnosticsAttribute.NAME, ServerAllDiagnosticsAttribute.getValueCore(sevice, srv.getId()));
        result.put(ServerFaultAttribute.NAME, ServerFaultAttribute.getValueCore(srv));
        result.put(ServerFlavorAttribute.NAME, ServerFlavorAttribute.getValueCore(srv));
        result.put(ServerHostAttribute.NAME, ServerHostAttribute.getValueCore(srv));
        result.put(ServerInstanceNameAttribute.NAME, ServerInstanceNameAttribute.getValueCore(srv));
        result.put(ServerLaunchedAtAttribute.NAME, ServerLaunchedAtAttribute.getValueCore(srv));
        result.put(ServerNameAttribute.NAME, ServerNameAttribute.getValueCore(srv));
        result.put(ServerPowerStateAttribute.NAME, ServerPowerStateAttribute.getValueCore(srv));
        result.put(ServerStatusAttribute.NAME, ServerStatusAttribute.getValueCore(srv));
        result.put(ServerTerminatedAtAttribute.NAME, ServerTerminatedAtAttribute.getValueCore(srv));
        result.put(ServerVmStateAttribute.NAME, ServerVmStateAttribute.getValueCore(srv));
        result.put(ID_NAME, srv.getId());
        return new CompositeDataSupport(TYPE, result);
    }

    @Override
    protected CompositeData getValue(final Server srv) throws OpenDataException {
        return getValueCore(openStackService.servers(), srv);
    }
}
