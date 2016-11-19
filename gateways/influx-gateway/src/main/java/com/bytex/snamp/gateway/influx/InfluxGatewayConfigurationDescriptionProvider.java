package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.concurrent.LazySoftReference;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.getIfPresent;
import static com.bytex.snamp.MapUtils.getValueAsLong;

/**
 * Describes configuration schema of InfluxDB Gateway.
 * @since 2.0
 * @version 2.0
 */
final class InfluxGatewayConfigurationDescriptionProvider extends ConfigurationEntityDescriptionProviderImpl {
    private static final String DB_URL_PARAM = "databaseLocation";
    private static final String DB_USER_NAME_PARAM = "databaseLogin";
    private static final String DB_PASSWORD_PARAM = "databasePassword";
    private static final String DB_NAME_PARAM = "databaseName";
    private static final String PERIOD_PARAM = "uploadPeriod";

    private static final LazySoftReference<InfluxGatewayConfigurationDescriptionProvider> INSTANCE = new LazySoftReference<>();

    private static final class GatewayConfigurationDescriptionProvider extends ResourceBasedConfigurationEntityDescription<GatewayConfiguration>{

        private GatewayConfigurationDescriptionProvider() {
            super("GatewayConfiguration", GatewayConfiguration.class, DB_URL_PARAM, DB_USER_NAME_PARAM, DB_PASSWORD_PARAM, DB_NAME_PARAM, PERIOD_PARAM);
        }
    }

    private InfluxGatewayConfigurationDescriptionProvider(){
        super(new GatewayConfigurationDescriptionProvider());
    }

    static InfluxGatewayConfigurationDescriptionProvider getInstance(){
        return INSTANCE.lazyGet(InfluxGatewayConfigurationDescriptionProvider::new);
    }

    Duration getUploadPeriod(final Map<String, String> parameters) {
        final long period = getValueAsLong(parameters, PERIOD_PARAM, Long::parseLong, () -> 1000L);
        return Duration.ofMillis(period);
    }

    String getDatabaseName(final Map<String, String> parameters) throws InfluxGatewayAbsentConfigurationParameterException {
        return getIfPresent(parameters, DB_NAME_PARAM, Function.identity(), InfluxGatewayAbsentConfigurationParameterException::new);
    }

    InfluxDB createDB(final Map<String, String> parameters) throws InfluxGatewayAbsentConfigurationParameterException {
        final String location = getIfPresent(parameters, DB_URL_PARAM, Function.identity(), InfluxGatewayAbsentConfigurationParameterException::new);
        final String login = getIfPresent(parameters, DB_USER_NAME_PARAM, Function.identity(), InfluxGatewayAbsentConfigurationParameterException::new);
        final String password = getIfPresent(parameters, DB_PASSWORD_PARAM, Function.identity(), InfluxGatewayAbsentConfigurationParameterException::new);
        final InfluxDB db = InfluxDBFactory.connect(location, login, password);
        return db;
    }
}
