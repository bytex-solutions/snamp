package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceConfiguration;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * ManagedResourceConfigurationDTO
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("managedResource")
public final class ManagedResourceDataObject extends TemplateDataObject<ManagedResourceConfiguration> {
    private String connectionString;
    private String groupName;

    /**
     * Default constructor.
     */
    @SpecialUse //used for Jackson deserialization
    public ManagedResourceDataObject() {

    }

    public ManagedResourceDataObject(final ManagedResourceConfiguration configuration) {
        super(configuration);
        connectionString = configuration.getConnectionString();
        groupName = configuration.getGroupName();
    }

    @Override
    public void exportTo(final ManagedResourceConfiguration entity) {
        super.exportTo(entity);
        entity.setConnectionString(connectionString);
        entity.setGroupName(groupName);
    }

    @JsonProperty
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
    @JsonProperty
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
