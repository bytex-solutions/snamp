package com.bytex.snamp.configuration;

import com.bytex.snamp.Localizable;

import java.util.Collection;
import java.util.Locale;

/**
 * Represents description of the SNAMP plugin configuration model.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public interface ConfigurationEntityDescription<T extends EntityConfiguration> extends Collection<String> {

    /**
     * Represents relationship between configuration parameters.
     */
    enum ParameterRelationship{
        /**
         * Parameter X should be always used with parameter Y.
         */
        ASSOCIATION,

        /**
         * Parameter X may be used with parameter Y, but Y cannot be used without X.
         */
        EXTENSION,

        /**
         * Parameter X cannot be used with parameter Y.
         */
        EXCLUSION
    }

    /**
     * Represents description of the configuration parameter.
     * @author Roman Sakno
     * @since 1.0
     * @version 2.0
     */
    interface ParameterDescription extends Localizable {
        /**
         * Gets the name of this parameter.
         * @return The name of this parameter.
         */
        String getName();

        /**
         * Determines whether the configuration parameter must be presented in the configuration.
         * @return {@literal true}, if this parameter should be presented in the configuration; otherwise, {@literal false}.
         */
        boolean isRequired();

        /**
         * Returns regular expression pattern that can be used to validate parameter value.
         * @param loc The language specification of the input pattern.
         * @return The regular expression pattern that can be used to validate parameter value; or {@literal null} if it
         * is unknown.
         */
        String getValuePattern(final Locale loc);

        /**
         * Determines whether the specified parameter value is correct.
         * @param value The parameter value to check.
         * @param loc The locale of the parameter value.
         * @return {@literal true}, if the specified value is correct; otherwise, {@literal false}.
         */
        boolean validateValue(final String value, final Locale loc);

        /**
         * Returns a collection of related parameters.
         * @param relationship Required relationship between this parameter and other parameters.
         * @return A read-only collection of related parameters.
         */
        Collection<String> getRelatedParameters(final ParameterRelationship relationship);

        /**
         * Returns the default value of this configuration parameter.
         * @param loc The localization of the default value. May be {@literal null}.
         * @return The default value of this configuration parameter; or {@literal null} if value is not available.
         */
        String getDefaultValue(final Locale loc);
    }

    /**
     * Returns a type of the configuration entity.
     * @return A type of the configuration entity.
     * @see GatewayConfiguration
     * @see ManagedResourceConfiguration
     * @see EventConfiguration
     * @see AttributeConfiguration
     */
    Class<T> getEntityType();

    /**
     * Returns the description of the specified parameter.
     * @param parameterName The name of the parameter.
     * @return The description of the specified parameter; or {@literal null}, if the specified parameter doesn't exist.
     */
    ParameterDescription getParameterDescriptor(final String parameterName);
}
