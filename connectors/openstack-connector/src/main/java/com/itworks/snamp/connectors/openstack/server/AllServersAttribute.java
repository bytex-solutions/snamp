package com.itworks.snamp.connectors.openstack.server;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import com.itworks.snamp.connectors.openstack.flavor.FlavorAttribute;
import com.itworks.snamp.internal.Utils;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.compute.ComputeService;
import org.openstack4j.api.compute.ServerService;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.Server;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Exposes information about all servers.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
public final class AllServersAttribute extends OpenStackResourceAttribute<CompositeData[], ComputeService> {
    public static final String NAME = "servers";
    private static final String DESCRIPTION = "Information about all servers";
    private static final ArrayType<CompositeData[]> TYPE = Utils.interfaceStaticInitialize(new Callable<ArrayType<CompositeData[]>>() {
        @Override
        public ArrayType<CompositeData[]> call() throws OpenDataException {
            return new ArrayType<>(1, ServerAttribute.TYPE);
        }
    });

    public AllServersAttribute(final String attributeID,
                               final AttributeDescriptor descriptor,
                               final OSClient client) {
        super(attributeID, DESCRIPTION, TYPE, AttributeSpecifier.READ_ONLY, descriptor, client.compute());
    }

    @Override
    public CompositeData[] getValue() throws OpenDataException {
        final List<? extends Server> servers = openStackService.servers().list();
        final CompositeData[] result = new CompositeData[servers.size()];
        for (int i = 0; i < servers.size(); i++)
            result[i] = ServerAttribute.getValueCore(openStackService.servers(), servers.get(i));
        return result;
    }
}
