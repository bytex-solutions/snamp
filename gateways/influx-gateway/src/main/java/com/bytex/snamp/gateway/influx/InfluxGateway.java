package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.concurrent.WeakRepeater;
import com.bytex.snamp.core.ClusterMember;
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
import java.util.stream.Stream;

import static com.bytex.snamp.internal.Utils.getBundleContextOfObject;

/**
 * Provides gateway that dumps attributes into InfluxDB for further analysis via Grafana.
 * @since 2.0
 * @version 2.1
 */
final class InfluxGateway extends AbstractGateway {
    private static final class PointsUploader extends WeakRepeater<InfluxGateway>{
        private final String threadName;
        private final ClusterMember clusterMember;

        private PointsUploader(final Duration period, final InfluxGateway gateway){
            super(period, gateway);
            threadName = "PointsUploader-".concat(gateway.instanceName);
            clusterMember = ClusterMember.get(getBundleContextOfObject(gateway));
        }

        @Override
        protected void doAction() throws JMException, InterruptedException {
            final InfluxGateway gateway = getReferenceOrTerminate();
            //only active cluster node is responsible for reporting
            if (clusterMember.isActive())
                gateway.dumpAttributes();
        }

        @Override
        protected String generateThreadName() {
            return threadName;
        }
    }
    private Reporter reporter;
    private final InfluxModelOfAttributes attributes;
    private Repeater pointsUploader;
    private final InfluxModelOfNotifications notifications;

    void dumpAttributes() throws JMException {
        final Reporter reporter = this.reporter;
        if (reporter != null)
            attributes.dumpPoints(reporter);
    }

    /**
     * Initializes a new instance of gateway.
     *
     * @param instanceName The name of the gateway instance.
     */
    InfluxGateway(final String instanceName) {
        super(instanceName);
        attributes = new InfluxModelOfAttributes();
        notifications = new InfluxModelOfNotifications(attributes, ClusterMember.get(getBundleContext()));
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
            getLogger().info(String.format("InfluxDB is connected. Version: %s. Response time: %s ms", databaseCheck.getVersion(), databaseCheck.getResponseTime()));
            notifications.setReporter(reporter = createReporter(database, databaseName));
        }
        //initialize uploader as periodic task
        {
            final Duration uploadPeriod = parser.getUploadPeriod(parameters);
            pointsUploader = new PointsUploader(uploadPeriod, this);
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
