package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import javax.annotation.Nonnull;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("resourceGroup")
public class ResourceGroupDataObject extends TemplateDataObject<ManagedResourceGroupConfiguration> {
    private String supervisor;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public ResourceGroupDataObject(){
        supervisor = "";
    }

    public ResourceGroupDataObject(final ManagedResourceGroupConfiguration configuration){
        super(configuration);
        supervisor = nullToEmpty(configuration.getSupervisor());
    }

    @Override
    public void exportTo(@Nonnull final ManagedResourceGroupConfiguration entity) {
        super.exportTo(entity);
        entity.setSupervisor(supervisor);
    }

    @JsonProperty("supervisor")
    public String getSupervisor(){
        return supervisor;
    }

    public void setSupervisor(final String value){
        supervisor = nullToEmpty(value);
    }
}
