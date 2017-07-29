package com.bytex.snamp.supervision.def;

import com.bytex.snamp.ResourceReader;
import com.bytex.snamp.configuration.ConfigurationEntityDescription;
import com.bytex.snamp.configuration.ConfigurationEntityDescriptionProviderImpl;
import com.bytex.snamp.configuration.ResourceBasedConfigurationEntityDescription;
import com.bytex.snamp.configuration.SupervisorConfiguration;
import com.bytex.snamp.supervision.SupervisorDescriptionProvider;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.bytex.snamp.MapUtils.getValue;

/**
 * Represents description of configuration properties related to default supervisor.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public class DefaultSupervisorConfigurationDescriptionProvider extends ConfigurationEntityDescriptionProviderImpl implements SupervisorDescriptionProvider {
    private static final String CHECK_PERIOD_PARAM = "checkPeriod";

    protected static class DefaultSupervisorDescription extends ResourceBasedConfigurationEntityDescription<SupervisorConfiguration>{
        private static final String RESOURCE_NAME = "SupervisorConfiguration";
        private final ResourceReader fallbackReader;
        private static final String[] DEFAULT_PARAMS = {CHECK_PERIOD_PARAM};

        protected DefaultSupervisorDescription(final String baseName, final String... parameters){
            super(baseName, SupervisorConfiguration.class, parameters);
            fallbackReader = new ResourceReader(DefaultSupervisorDescription.class, RESOURCE_NAME);
        }

        private DefaultSupervisorDescription(){
            super(RESOURCE_NAME, SupervisorConfiguration.class, DEFAULT_PARAMS);
            fallbackReader = null;
        }

        public static DefaultSupervisorDescription create(){
            return new DefaultSupervisorDescription();
        }

        @Override
        protected Optional<String> getStringFallback(final String key, final Locale loc) {
            return fallbackReader == null ? Optional.empty() : fallbackReader.getString(key, loc);
        }
    }

    protected DefaultSupervisorConfigurationDescriptionProvider(final ConfigurationEntityDescription<?>... descriptions){
        super(descriptions);
    }

    DefaultSupervisorConfigurationDescriptionProvider() {
        this(DefaultSupervisorDescription.create());
    }

    public Duration parseCheckPeriod(final Map<String, String> parameters){
        return getValue(parameters, CHECK_PERIOD_PARAM, Long::parseLong)
                .map(Duration::ofMillis)
                .orElseGet(() -> Duration.ofMillis(900L));
    }
}
