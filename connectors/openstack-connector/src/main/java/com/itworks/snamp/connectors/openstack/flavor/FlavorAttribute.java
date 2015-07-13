package com.itworks.snamp.connectors.openstack.flavor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.Flavor;
import org.openstack4j.model.compute.ext.Hypervisor;

import javax.management.openmbean.*;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class FlavorAttribute extends AbstractFlavorAttribute<CompositeData> {
    public static final String NAME = "flavorInfo";
    private static final String DESCRIPTION = "Full information about flavor";
    public static final CompositeType TYPE;
    private static final String ID_NAME = "flavorID";
    private static final String ID_DESCR = "ID of the flavor";

    static {
        try {
            TYPE = new CompositeTypeBuilder("Flavor", "OpenStack Flavor")
                    .addItem(FlavorCpuCountAttribute.NAME, FlavorCpuCountAttribute.DESCRIPTION, FlavorCpuCountAttribute.TYPE)
                    .addItem(FlavorDisabledAttribute.NAME, FlavorDisabledAttribute.DESCRIPTION, FlavorDisabledAttribute.TYPE)
                    .addItem(FlavorDiskAttribute.NAME, FlavorDiskAttribute.DESCRIPTION, FlavorDiskAttribute.TYPE)
                    .addItem(FlavorPublicAttribute.NAME, FlavorPublicAttribute.DESCRIPTION, FlavorPublicAttribute.TYPE)
                    .addItem(FlavorRamAttribute.NAME, FlavorRamAttribute.DESCRIPTION, FlavorRamAttribute.TYPE)
                    .addItem(ID_NAME, ID_DESCR, SimpleType.STRING)
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

    public static CompositeData getValueCore(final Flavor flavor) throws OpenDataException {
        final Map<String, Object> result = Maps.newHashMapWithExpectedSize(TYPE.keySet().size());
        result.put(FlavorCpuCountAttribute.NAME, FlavorCpuCountAttribute.getValueCore(flavor));
        result.put(FlavorDisabledAttribute.NAME, FlavorDisabledAttribute.getValueCore(flavor));
        result.put(FlavorDiskAttribute.NAME, FlavorDiskAttribute.getValueCore(flavor));
        result.put(FlavorPublicAttribute.NAME, FlavorPublicAttribute.getValueCore(flavor));
        result.put(FlavorRamAttribute.NAME, FlavorRamAttribute.getValueCore(flavor));
        result.put(ID_NAME, flavor.getId());
        return new CompositeDataSupport(TYPE, result);
    }

    @Override
    protected CompositeData getValue(final Flavor flavor) throws OpenDataException {
        return getValueCore(flavor);
    }
}
