package com.itworks.snamp.configuration;

import com.itworks.snamp.internal.ReflectionUtils;

import static com.itworks.snamp.configuration.AgentConfiguration.ConfigurationEntity;

import java.util.*;

/**
 * Represents resource-based configuration entity descriptor.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class ResourceBasedConfigurationEntityDescription<T extends ConfigurationEntity> extends ArrayList<String> implements ConfigurationEntityDescription<T> {
    private static final String DESCRIPTION_POSTFIX = ".description";
    private static final String REQUIRED_POSTFIX = ".required";
    private static final String PATTERN_POSTFIX = ".pattern";
    private static final String ASSOCIATION_POSTFIX = "association";
    private static final String EXTENSION_POSTFIX = ".postfix";
    private static final String EXCLUSION_POSTFIX = ".exclusion";
    private static final String DEFVAL_POSTIFX = ".default";
    private final Class<T> entityType;

    /**
     * Initializes a new resource-based descriptor.
     * @param entityType Configuration element type. Cannot be {@literal null}.
     * @param parameters A collection of configuration parameters.
     */
    protected ResourceBasedConfigurationEntityDescription(final Class<T> entityType, final Collection<String> parameters){
        super(parameters);
        this.entityType = entityType;
    }

    /**
     * Initializes a new resource-based descriptor.
     * @param entityType Configuration element type. Cannot be {@literal null}.
     * @param parameters An array of configuration parameters.
     */
    protected ResourceBasedConfigurationEntityDescription(final Class<T> entityType, final String... parameters){
        this(entityType, Arrays.asList(parameters));
    }

    /**
     * Returns a type of the configuration entity.
     *
     * @return A type of the configuration entity.
     * @see AgentConfiguration.HostingConfiguration
     * @see AgentConfiguration.ManagementTargetConfiguration
     * @see AgentConfiguration.ManagementTargetConfiguration.EventConfiguration
     * @see AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration
     */
    @Override
    public final Class<T> getEntityType() {
        return entityType;
    }

    /**
     * Returns full resource name constructed from the namespace of the derived class and the
     * specified resource name.
     * @param baseName The name of the resource bundling.
     * @return The full resource name.
     */
    protected final String getResourceName(final String baseName){
        return ReflectionUtils.getFullyQualifiedResourceName(getClass(), baseName);
    }

    /**
     * Retrieves resource accessor for the specified locale.
     * <p>
     *     The following example shows recommended implementation of this method:
     *     <pre><code>
     *     protected final ResourceBundle getBundle(final Locale loc) {
     *      return loc != null ? ResourceBundle.getBundle(getResourceName("MyResource"), loc) :
     *      ResourceBundle.getBundle(getResourceName("MyResource"));
     *     }
     *     </code></pre>
     * </p>
     * @param loc The requested localization of the resource. May be {@literal null}.
     * @return The resource accessor.
     */
    protected abstract ResourceBundle getBundle(final Locale loc);

    /**
     * Determines whether the specified parameter is required.
     * @param parameterName The name of the configuration parameter.
     * @return {@literal true}, if the specified configuration parameter must be presented in the configuration;
     * otherwise, {@literal false}.
     */
    protected boolean isRequiredParameter(final String parameterName){
        return Boolean.getBoolean(getBundle(null).getString(parameterName + REQUIRED_POSTFIX));
    }

    /**
     * Returns description of the specified configuration parameter.
     * @param parameterName The name of the configuration parameter.
     * @param loc Required localization of the description.
     * @return The localized description of the configuration parameter.
     */
    protected String getParameterDescription(final String parameterName, final Locale loc){
        return getBundle(loc).getString(parameterName + DESCRIPTION_POSTFIX);
    }

    /**
     * Returns input value pattern (regular expression) of the specified configuration parameter.
     * @param parameterName The name of the configuration parameter.
     * @param loc Required localization of the pattern.
     * @return The localized input value pattern.
     */
    protected String getParameterValuePattern(final String parameterName, final Locale loc){
        return getBundle(loc).getString(parameterName + PATTERN_POSTFIX);
    }

    private Collection<String> getRelatedParameters(final String parameterName, final String relationPostfix){
        final String params = getBundle(null).getString(parameterName + relationPostfix);
        if(params == null || params.isEmpty()) return Collections.emptyList();
        final String[] values =  params.split(",");
        final Collection<String> result = new ArrayList<>(values.length);
        for(final String p: values)
            result.add(p.trim());
        return result;
    }

    /**
     * Retrieves a read-only collection of related configuration parameters.
     * @param parameterName The name of the configuration parameters.
     * @param relationship The type of relationship between two configuration parameters.
     * @return A read-only collection of related configuration parameters.
     */
    protected Collection<String> getParameterRelations(final String parameterName, final ParameterRelationship relationship){
        switch (relationship){
            case ASSOCIATION: return getRelatedParameters(parameterName, ASSOCIATION_POSTFIX);
            case EXTENSION: return getRelatedParameters(parameterName, EXTENSION_POSTFIX);
            case EXCLUSION: return getRelatedParameters(parameterName, EXCLUSION_POSTFIX);
            default: return Collections.emptyList();
        }
    }

    /**
     * Returns the default value for of the specified configuration parameter.
     * @param parameterName The name of the configuration parameter.
     * @param loc The localization of the default value.
     * @return The configuration parameter default value.
     */
    protected String getParameterDefaultValue(final String parameterName, final Locale loc){
        return getBundle(loc).getString(parameterName + DEFVAL_POSTIFX);
    }

    /**
     * Returns the description of the specified parameter.
     *
     * @param parameterName The name of the parameter.
     * @return The description of the specified parameter; or {@literal null}, if the specified parameter doesn't exist.
     */
    @Override
    public final ParameterDescription getParameterDescriptor(final String parameterName) {
        return contains(parameterName) ?
                new ParameterDescription() {
                    @Override
                    public final String getName() {
                        return parameterName;
                    }

                    @Override
                    public final String getDescription(final Locale loc) {
                        return getParameterDescription(parameterName, loc);
                    }

                    @Override
                    public final boolean isRequired() {
                        return isRequiredParameter(parameterName);
                    }

                    @Override
                    public final String getValuePattern(final Locale loc) {
                        return getParameterValuePattern(parameterName, loc);
                    }

                    @Override
                    public final boolean validateValue(final String value, final Locale loc) {
                        if(value == null) return false;
                        final String pattern = getValuePattern(loc);
                        return pattern == null || pattern.isEmpty() ? true : value.matches(pattern);
                    }

                    @Override
                    public final Collection<String> getRelatedParameters(final ParameterRelationship relationship) {
                        return getParameterRelations(parameterName, relationship);
                    }

                    /**
                     * Returns the default value of this configuration parameter.
                     *
                     * @param loc The localization of the default value. May be {@literal null}.
                     * @return The default value of this configuration parameter; or {@literal null} if value is not available.
                     */
                    @Override
                    public String getDefaultValue(final Locale loc) {
                        return getParameterDefaultValue(parameterName, loc);
                    }
                }: null;
    }
}
