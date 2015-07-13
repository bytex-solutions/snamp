package com.itworks.snamp.connectors.openstack.flavor;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Flavor;

import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class FlavorDisabledAttribute extends AbstractFlavorAttribute<Boolean> {
    public static final String NAME = "disabled";
    static final String DESCRIPTION = "Determines whether the Flavor is disabled";
    static final SimpleType<Boolean> TYPE = SimpleType.BOOLEAN;

    public FlavorDisabledAttribute(final String entityID,
                                   final String attributeID,
                            final AttributeDescriptor descriptor,
                            final OSClient openStackService) {
        super(entityID, attributeID, DESCRIPTION, TYPE, descriptor, true, openStackService);
    }

    static boolean getValueCore(final Flavor flavor){
        return flavor.isDisabled();
    }

    @Override
    protected Boolean getValue(final Flavor flavor) {
        return getValueCore(flavor);
    }
}
