package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.gateway.modeling.ModelOfAttributes;
import org.influxdb.InfluxDB;

import java.time.Duration;
import java.util.Objects;

/**
 * Provides periodic upload of attributes into
 */
final class InfluxPointsUploader extends Repeater {
    private final InfluxDB database;
    private final InfluxModelOfAttributes attributes;

    InfluxPointsUploader(final Duration period, final InfluxDB database){
        super(period);
        this.database = Objects.requireNonNull(database);
        attributes = new InfluxModelOfAttributes();
    }

    ModelOfAttributes<AttributePoint> getAttributes(){
        return attributes;
    }

    @Override
    protected void doAction() {

    }
}
