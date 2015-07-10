package com.itworks.snamp.connectors.openstack.flavor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.connectors.attributes.AttributeSpecifier;
import com.itworks.snamp.connectors.openstack.OpenStackResourceAttribute;
import com.itworks.snamp.jmx.CompositeTypeBuilder;
import org.openstack4j.api.OSClient;
import org.openstack4j.api.compute.FlavorService;
import org.openstack4j.model.compute.Flavor;

import javax.management.MBeanException;
import javax.management.openmbean.*;
import java.util.List;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class AllFlavorsAttribute extends OpenStackResourceAttribute<CompositeData[], FlavorService> {
    public static final String NAME = "flavors";
    private static final String DESCRIPTION = "Gets information about all flavors";
    private static final CompositeType FLAVOR_TYPE;
    private static final ArrayType<CompositeData[]> TYPE;

    static {
        try {
            FLAVOR_TYPE = new CompositeTypeBuilder("Flavor", "OpenStack Flavor")
                    .addItem(FlavorCpuCountAttribute.NAME, FlavorCpuCountAttribute.DESCRIPTION, FlavorCpuCountAttribute.TYPE)
                    .addItem(FlavorDisabledAttribute.NAME, FlavorDisabledAttribute.DESCRIPTION, FlavorDisabledAttribute.TYPE)
                    .addItem(FlavorDiskAttribute.NAME, FlavorDiskAttribute.DESCRIPTION, FlavorDiskAttribute.TYPE)
                    .addItem(FlavorPublicAttribute.NAME, FlavorPublicAttribute.DESCRIPTION, FlavorPublicAttribute.TYPE)
                    .addItem(FlavorRamAttribute.NAME, FlavorRamAttribute.DESCRIPTION, FlavorRamAttribute.TYPE)
                    .build();
            TYPE = new ArrayType<>(1, FLAVOR_TYPE);
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public AllFlavorsAttribute(final String attributeID,
                               final AttributeDescriptor descriptor,
                               final OSClient client){
        super(attributeID, DESCRIPTION, TYPE, AttributeSpecifier.READ_ONLY, descriptor, client.compute().flavors());
    }

    private static CompositeData toCompositeData(final Flavor flavor) throws OpenDataException {
        final Map<String, Object> result = ImmutableMap.<String, Object>of(
                FlavorCpuCountAttribute.NAME, flavor.getVcpus(),
                FlavorDisabledAttribute.NAME, flavor.isDisabled(),
                FlavorDiskAttribute.NAME, flavor.getDisk(),
                FlavorPublicAttribute.NAME, flavor.isPublic(),
                FlavorRamAttribute.NAME, flavor.getRam()
        );
        return new CompositeDataSupport(FLAVOR_TYPE, result);
    }

    /**
     * Gets value of this attribute.
     *
     * @return The value of this attribute.
     * @throws Exception Unable to read attribute value.
     */
    @Override
    public CompositeData[] getValue() throws OpenDataException {
        final List<? extends Flavor> flavors = openStackService.list();
        final CompositeData[] result = new CompositeData[flavors.size()];
        for(int i = 0; i < flavors.size(); i++)
            result[i] = toCompositeData(flavors.get(i));
        return result;
    }

    /**
     * Sets value of this attribute.
     *
     * @param value The value of this attribute.
     * @throws Exception Unable to write attribute value.
     */
    @Override
    public void setValue(final CompositeData[] value) throws MBeanException {
        throw new MBeanException(new UnsupportedOperationException("Attribute is read-only"));
    }
}
