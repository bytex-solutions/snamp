package com.bytex.snamp.gateway.influx;

import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
interface Reporter {
    String getDatabaseName();
    String getRetentionPolicy();
    void report(final BatchPoints points);
    void report(final Point point);
}
