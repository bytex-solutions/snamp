package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * ManagedResourceConfigurationDTO
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("managedResource")
public final class ResourceDataObject extends TemplateDataObject<ManagedResourceConfiguration> {
    private String connectionString;
    private String groupName;
    private final Set<String> overriddenProperties;

    /**
     * Default constructor.
     */
    @SpecialUse(SpecialUse.Case.SERIALIZATION) //used for Jackson deserialization
    public ResourceDataObject() {
        overriddenProperties = new HashSet<>();
    }

    public ResourceDataObject(final ManagedResourceConfiguration configuration) {
        super(configuration);
        connectionString = configuration.getConnectionString();
        groupName = configuration.getGroupName();
        overriddenProperties = new HashSet<>();
    }

    @Override
    public void exportTo(@Nonnull final ManagedResourceConfiguration entity) {
        super.exportTo(entity);
        entity.setConnectionString(connectionString);
        entity.setGroupName(groupName);
        entity.overrideProperties(overriddenProperties);
    }

    @JsonProperty("overriddenProperties")
    public Set<String> getOverriddenProperties(){
        return overriddenProperties;
    }

    public void setOverriddenProperties(final Set<String> values){
        overriddenProperties.clear();
        overriddenProperties.addAll(values);
    }

    @JsonProperty("groupName")
    public String getGroupName(){
        return groupName;
    }

    public void setGroupName(final String value){
        groupName = value;
    }

    /**
     * Gets connection string.
     *
     * @return the connection string
     */
    @JsonProperty("connectionString")
    public String getConnectionString() {
        return connectionString;
    }

    /**
     * Sets connection string.
     *
     * @param connectionString the connection string
     */
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }
}
