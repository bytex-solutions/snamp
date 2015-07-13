package com.itworks.snamp.connectors.openstack.flavor;

import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.ext.Hypervisor;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class FlavorAttribute extends AbstractFlavorAttribute<CompositeData> {
    public static final String NAME = "flavorInfo";
    private static final String DESCRIPTION = "Full information about flavor";
    static final CompositeType TYPE;

    static {
        try {
            TYPE = new CompositeTypeBuilder("Flavor", "OpenStack Flavor")
                    .addItem(FlavorCpuCountAttribute.NAME, FlavorCpuCountAttribute.DESCRIPTION, FlavorCpuCountAttribute.TYPE)
                    .addItem(FlavorDisabledAttribute.NAME, FlavorDisabledAttribute.DESCRIPTION, FlavorDisabledAttribute.TYPE)
                    .addItem(FlavorDiskAttribute.NAME, FlavorDiskAttribute.DESCRIPTION, FlavorDiskAttribute.TYPE)
                    .addItem(FlavorPublicAttribute.NAME, FlavorPublicAttribute.DESCRIPTION, FlavorPublicAttribute.TYPE)
                    .addItem(FlavorRamAttribute.NAME, FlavorRamAttribute.DESCRIPTION, FlavorRamAttribute.TYPE)
                    .build();
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public FlavorAttribute(final String flavorID,
                           final String attributeID,
                           final AttributeDescriptor descriptor,
                           final OSClient client) {
        super(flavorID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static CompositeData getValueCore(final Flavor flavor) throws OpenDataException {
        final Map<String, Object> result = ImmutableMap.<String, Object>of(
                FlavorCpuCountAttribute.NAME, FlavorCpuCountAttribute.getValueCore(flavor),
                FlavorDisabledAttribute.NAME, FlavorDisabledAttribute.getValueCore(flavor),
                FlavorDiskAttribute.NAME, FlavorDiskAttribute.getValueCore(flavor),
                FlavorPublicAttribute.NAME, FlavorPublicAttribute.getValueCore(flavor),
                FlavorRamAttribute.NAME, FlavorRamAttribute.getValueCore(flavor)
        );
        return new CompositeDataSupport(TYPE, result);
    }

    @Override
    protected CompositeData getValue(final Flavor flavor) throws Exception {
        return getValueCore(flavor);
    }
}
