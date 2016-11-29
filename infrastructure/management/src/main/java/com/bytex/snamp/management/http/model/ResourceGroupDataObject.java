package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ManagedResourceGroupConfiguration;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("resourceGroup")
public class ResourceGroupDataObject extends TemplateDataObject<ManagedResourceGroupConfiguration> {
    @SpecialUse
    public ResourceGroupDataObject(){

    }

    public ResourceGroupDataObject(final ManagedResourceGroupConfiguration configuration){
        super(configuration);
    }
}
