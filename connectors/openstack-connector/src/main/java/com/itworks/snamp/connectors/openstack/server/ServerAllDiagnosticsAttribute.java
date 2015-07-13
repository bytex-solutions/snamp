package com.itworks.snamp.connectors.openstack.server;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.KeyValueTypeBuilder;
import jersey.repackaged.com.google.common.collect.Maps;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.compute.ServerService;
import org.openstack4j.model.compute.Server;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;
import java.util.Map;
import java.util.concurrent.Callable;
import static com.itworks.snamp.jmx.TabularDataUtils.makeKeyValuePairs;

/**
 * Represents diagnostics information about server.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class ServerAllDiagnosticsAttribute extends AbstractServerAttribute<TabularData> {
    public static final String NAME = "diagnostics";
    static final String DESCRIPTION = "Server diagnostics";
    static final TabularType TYPE = Utils.interfaceStaticInitialize(new Callable<TabularType>() {
        @Override
        public TabularType call() throws OpenDataException {
            return new KeyValueTypeBuilder<String, Long>()
                    .setKeyColumn("parameter", "Diagnostics parameter", SimpleType.STRING)
                    .setValueColumn("value", "The value of diagnostics parameter", SimpleType.LONG)
                    .setTypeName("ServerDiagnostics")
                    .setTypeDescription("Server diagnostics parameters")
                    .build();
        }
    });
    private static final Maps.EntryTransformer<String, Number, Long> NUMBER_TO_LONG_TRANSFORMER = new Maps.EntryTransformer<String, Number, Long>() {
        @Override
        public Long transformEntry(final String parameter, final Number value) {
            return value == null ? 0L : value.longValue();
        }
    };

    public ServerAllDiagnosticsAttribute(final String serverID,
                                         final String attributeID,
                                         final AttributeDescriptor descriptor,
                                         final OSClient client) {
        super(serverID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static TabularData getValueCore(final ServerService service, final String serverID) throws OpenDataException {
        Map<String, ? extends Number> diag = service.diagnostics(serverID);
        if (diag == null) diag = ImmutableMap.of();
        return makeKeyValuePairs(TYPE, Maps.transformEntries(diag, NUMBER_TO_LONG_TRANSFORMER));
    }

    @Override
    protected TabularData getValue(final Server srv) throws OpenDataException {
        return getValueCore(openStackService.servers(), serverID);
    }
}
