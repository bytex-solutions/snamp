package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.gateway.modeling.ModelOfAttributes;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.util.Map;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Provides collection of attributes wrapped into measurement points.
 * @since 2.0
 * @version 2.0
 */
final class InfluxModelOfAttributes extends ModelOfAttributes<AttributePoint> {
    @Override
    protected AttributePoint createAccessor(final String resourceName, final MBeanAttributeInfo metadata) throws Exception {
        return new AttributePoint(metadata);
    }

    private Map<String, String> extractTags(final String resourceName) throws InstanceNotFoundException {
        return Helpers.extractTags(getBundleContextOfObject(this), resourceName);
    }

    void dumpPoints(final Reporter reporter) throws JMException {
        final BatchPoints points = BatchPoints
                .database(reporter.getDatabaseName())
                .retentionPolicy(reporter.getRetentionPolicy())
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
        forEachAttribute((resourceName, attribute) -> {
            final Map<String, String> tags = extractTags(resourceName);
            points.point(attribute.toPoint(tags));
            return true;
        });
        reporter.report(points);
    }
}
