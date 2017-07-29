package com.bytex.snamp.gateway.influx;

import com.bytex.snamp.concurrent.LazyReference;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.GatewayConfiguration;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

import java.time.Duration;
import java.util.Map;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.getValue;
import static com.bytex.snamp.MapUtils.getValueAsLong;

/**
 * Describes configuration schema of InfluxDB Gateway.
 * @since 2.0
 * @version 2.1
 */
final class InfluxGatewayConfigurationDescriptionProvider extends ConfigurationEntityDescriptionProviderImpl {
    private static final String DB_URL_PARAM = "databaseLocation";
    private static final String DB_USER_NAME_PARAM = "databaseLogin";
    private static final String DB_PASSWORD_PARAM = "databasePassword";
    private static final String DB_NAME_PARAM = "databaseName";
    private static final String PERIOD_PARAM = "uploadPeriod";

    private static final LazyReference<InfluxGatewayConfigurationDescriptionProvider> INSTANCE = LazyReference.soft();

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
        final long period = getValueAsLong(parameters, PERIOD_PARAM, Long::parseLong).orElse(1000L);
        return Duration.ofMillis(period);
    }

    String getDatabaseName(final Map<String, String> parameters) throws InfluxGatewayAbsentConfigurationParameterException {
        return getValue(parameters, DB_NAME_PARAM, Function.identity()).orElseThrow(() -> new InfluxGatewayAbsentConfigurationParameterException(DB_NAME_PARAM));
    }

    InfluxDB createDB(final Map<String, String> parameters) throws InfluxGatewayAbsentConfigurationParameterException {
        final String location = getValue(parameters, DB_URL_PARAM, Function.identity()).orElseThrow(() -> new InfluxGatewayAbsentConfigurationParameterException(DB_URL_PARAM));
        final String login = getValue(parameters, DB_USER_NAME_PARAM, Function.identity()).orElseThrow(() -> new InfluxGatewayAbsentConfigurationParameterException(DB_USER_NAME_PARAM));
        final String password = getValue(parameters, DB_PASSWORD_PARAM, Function.identity()).orElseThrow(() -> new InfluxGatewayAbsentConfigurationParameterException(DB_PASSWORD_PARAM));
        return InfluxDBFactory.connect(location, login, password);
    }
}
