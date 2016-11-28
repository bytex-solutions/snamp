package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Objects;
import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public abstract class GroovyFeatureBuilder<F extends FeatureConfiguration> {
    String description;
    Map<String, String> parameters;

    GroovyFeatureBuilder(){
        parameters = ImmutableMap.of();
    }

    public final void description(final String value){
        description = Objects.requireNonNull(value);
    }

    public final void configuration(final Map<String, String> value){
        parameters = Objects.requireNonNull(value);
    }

    abstract F createConfiguration();

    final F createConfiguration(final Class<F> featureType){
        final F result = ConfigurationManager.createEntityConfiguration(getClass().getClassLoader(), featureType);
        assert result != null;
        result.setParameters(parameters);
        result.setAutomaticallyAdded(true);
        if(!isNullOrEmpty(description))
            result.setDescription(description);
        return result;
    }
}
