package com.itworks.snamp.connectors.openstack.flavor;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Flavor;

import javax.management.openmbean.SimpleType;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class FlavorPublicAttribute extends AbstractFlavorAttribute<Boolean> {
    public static final String NAME = "public";
    static final String DESCRIPTION = "Determines whether the flavor is public";
    static final SimpleType<Boolean> TYPE = SimpleType.BOOLEAN;

    public FlavorPublicAttribute(final String entityID,
                                 final String attributeID,
                                 final AttributeDescriptor descriptor,
                                 final OSClient client) {
        super(entityID, attributeID, DESCRIPTION, TYPE, descriptor, true, client);
    }

    static boolean getValueCore(final Flavor flavor){
        return flavor.isPublic();
    }

    @Override
    protected Boolean getValue(final Flavor flavor) {
        return getValueCore(flavor);
    }
}
