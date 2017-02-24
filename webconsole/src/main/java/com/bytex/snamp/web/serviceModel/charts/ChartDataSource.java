package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.web.serviceModel.AbstractPrincipalBoundedService;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.osgi.framework.BundleContext;

import javax.annotation.Nonnull;
import javax.management.AttributeList;
import javax.management.JMException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Represents source of charts data.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@Path("/")
public final class ChartDataSource extends AbstractPrincipalBoundedService<Dashboard> {
    public static final String NAME = "charts";
    public static final String URL_CONTEXT = "/charts";

    public ChartDataSource() throws IOException {
        super(Dashboard.class);
    }

    private BundleContext getBundleContext(){
        return getBundleContextOfObject(this);
    }

    @POST
    @Path("/compute")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Collection<ChartData>> getChartData(final Chart[] charts) throws WebApplicationException {
        final BundleContext context = getBundleContext();
        final Multimap<String, ChartData> result = HashMultimap.create(charts.length, 3);
        for (final String resourceName : ManagedResourceConnectorClient.getResources(context)) {
            final ManagedResourceConnectorClient client = ManagedResourceConnectorClient.tryCreate(context, resourceName);
            if (client == null)
                continue;
            final AttributeList attributes;
            final String instanceName = client.getManagedResourceName();
            try {
                attributes = client.getAttributes();
            } catch (final JMException e) {
                throw new WebApplicationException(e);
            } finally {
                client.release(context); //release active reference to the managed resource connector as soon as possible to relax OSGi ServiceRegistry
            }

            for (final Chart chart : charts) {
                final String chartName = chart.getName();
                if (chart instanceof ChartOfAttributeValues)
                    ((ChartOfAttributeValues) chart).fillCharData(instanceName, attributes, data -> result.put(chartName, data));
            }
        }
        return result.asMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
    }

    @Nonnull
    @Override
    protected Dashboard createUserData() {
        return new Dashboard();
    }
}
