package com.itworks.snamp.connectors.openstack.server;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.openstack.OpenStackAbsentConfigurationParameterException;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Server;

import javax.management.openmbean.SimpleType;
import java.util.Map;

import static com.itworks.snamp.connectors.openstack.OpenStackResourceConnectorConfigurationDescriptor.getMetricName;

/**
 * Provides access to one of the available server diagnostics parameters.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class ServerDiagnosticsAttribute extends AbstractServerAttribute<Long> {
    public static final String NAME = "metric";
    private static final String DESCRIPTION = "Diagnostics metric";
    private static final SimpleType<Long> TYPE = SimpleType.LONG;

    private final String metricName;

    public ServerDiagnosticsAttribute(final String serverID,
                                      final String attributeID,
                                      final AttributeDescriptor descriptor,
                                      final OSClient client) throws OpenStackAbsentConfigurationParameterException {
        super(serverID, attributeID, DESCRIPTION, TYPE, descriptor, client);
        metricName = getMetricName(descriptor);
    }

    @Override
    protected Long getValue(final Server srv) {
        Map<String, ? extends Number> diag = openStackService.servers().diagnostics(serverID);
        if (diag == null) diag = ImmutableMap.of();
        final Number metricValue = diag.containsKey(metricName) ? diag.get(metricName) : 0L;
        return metricValue.longValue();
    }
}
