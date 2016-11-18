package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.connector.ManagedResourceConnectorClient;
import com.bytex.snamp.gateway.modeling.ModelOfAttributes;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.osgi.framework.BundleContext;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.util.Map;
import java.util.Objects;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Provides collection of attributes wrapped into measurement points.
 * @since 2.0
 * @version 2.0
 */
final class InfluxModelOfAttributes extends ModelOfAttributes<AttributePoint> {
    @Override
    protected AttributePoint createAccessor(final MBeanAttributeInfo metadata) throws Exception {
        return new AttributePoint(metadata);
    }

    private Map<String, String> extractTags(final String resourceName) throws InstanceNotFoundException {
        final BundleContext context = getBundleContextOfObject(this);
        final ManagedResourceConnectorClient client = new ManagedResourceConnectorClient(context, resourceName);
        try{
            return client.getProperties((k, v) -> v instanceof String, Objects::toString);
        } finally {
            client.release(context);
        }
    }

    void dumpPoints(final InfluxDB database, final String databaseName) throws JMException {
        final BatchPoints points = BatchPoints
                .database(databaseName)
                .retentionPolicy(database.version().startsWith("0.") ? "default" : "autogen")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
        forEachAttribute((resourceName, attribute) -> {
            final Map<String, String> tags = extractTags(resourceName);
            points.point(attribute.toPoint(tags));
            return true;
        });
        database.write(points);
    }
}
