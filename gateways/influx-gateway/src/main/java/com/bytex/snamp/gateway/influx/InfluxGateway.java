package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.modeling.FeatureAccessor;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Provides gateway that dumps attributes into InfluxDB for further analysis via Grafana.
 * @since 2.0
 * @version 2.0
 */
final class InfluxGateway extends AbstractGateway {
    private final class PointsUploader extends Repeater{
        private PointsUploader(final Duration period){
            super(period);
        }

        @Override
        protected void doAction() throws JMException {
            attributes.dumpPoints(reporter);
        }

        @Override
        protected String generateThreadName() {
            return "PointsUploader-".concat(getInstanceName());
        }

        @Override
        protected void stateChanged(final RepeaterState s) {
            switch (s){
                case STARTED:
                    getLogger().fine(String.format("Points uploader for '%s' instance is started", getInstanceName()));
                return;
                case FAILED:
                    getLogger().log(Level.SEVERE, String.format("Points uploader for '%s' instance is broken", getInstanceName()), getException());
                return;
                case CLOSED:
                    getLogger().fine(String.format("Points uploader for '%s' instance is closed", getInstanceName()));
                return;
                case STOPPED:
                    getLogger().fine(String.format("Points uploader for '%s' instance is stopped", getInstanceName()));
                return;
                case STOPPING:
                    getLogger().fine(String.format("Points uploader for '%s' instance is stopping", getInstanceName()));
            }
        }
    }
    private Reporter reporter;
    private final InfluxModelOfAttributes attributes;
    private Repeater pointsUploader;
    private final InfluxModelOfNotifications notifications;

    /**
     * Initializes a new instance of gateway.
     *
     * @param instanceName The name of the gateway instance.
     */
    InfluxGateway(final String instanceName) {
        super(instanceName);
        attributes = new InfluxModelOfAttributes();
        notifications = new InfluxModelOfNotifications(attributes);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName, final M feature) throws Exception {
        if (feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>) attributes.addAttribute(resourceName, (MBeanAttributeInfo) feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>) notifications.addNotification(resourceName, (MBeanNotificationInfo) feature);
        else
            return null;
    }

    @Override
    protected Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) throws Exception {
        return Stream.concat(
                attributes.clear(resourceName).stream(),
                notifications.clear(resourceName).stream()
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>) attributes.removeAttribute(resourceName, (MBeanAttributeInfo) feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>) notifications.removeNotification(resourceName, (MBeanNotificationInfo) feature);
        else
            return null;
    }

    private static Reporter createReporter(final InfluxDB database, final String databaseName){
        return new Reporter() {

            @Override
            public String getDatabaseName() {
                return databaseName;
            }

            @Override
            public String getRetentionPolicy() {
                return database.version().startsWith("0.") ? "default" : "autogen";
            }

            @Override
            public void report(final BatchPoints points) {
                database.write(points);
            }

            @Override
            public void report(final Point point) {
                database.write(databaseName, getRetentionPolicy(), point);
            }
        };
    }

    @Override
    protected void start(final Map<String, String> parameters) throws InfluxGatewayAbsentConfigurationParameterException {
        final InfluxGatewayConfigurationDescriptionProvider parser = InfluxGatewayConfigurationDescriptionProvider.getInstance();
        //initialize reporter
        {
            final InfluxDB database = parser.createDB(parameters);
            final String databaseName = parser.getDatabaseName(parameters);
            database.createDatabase(databaseName);
            final Pong databaseCheck = database.ping();
            getLogger().info(String.format("InfluxDB is connected. Version: %s. Response time: %s", databaseCheck.getVersion(), databaseCheck.getResponseTime()));
            notifications.setReporter(reporter = createReporter(database, databaseName));
        }
        //initialize uploader as periodic task
        {
            final Duration uploadPeriod = parser.getUploadPeriod(parameters);
            pointsUploader = new PointsUploader(uploadPeriod);
            pointsUploader.run();
        }
    }

    @Override
    protected void stop() throws InterruptedException, TimeoutException {
        notifications.clear();
        try {
            pointsUploader.close(pointsUploader.getPeriod());
        } finally {
            reporter = null;
            pointsUploader = null;
            attributes.clear();
        }
    }
}
