package com.itworks.snamp.connectors.openstack.hypervisor;

import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.compute.ext.Hypervisor;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.List;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class HypervisorCpuFeaturesAttribute extends AbstractHypervisorAttribute<String[]> {
    public static final String NAME = "cpuFeatures";
    static final String DESCRIPTION = "A set of CPU features";
    static final ArrayType<String[]> TYPE;

    static {
        try {
            TYPE = new ArrayType<String[]>(SimpleType.STRING, false);
        } catch (final OpenDataException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public HypervisorCpuFeaturesAttribute(final String hypervisorID,
                                          final String attributeID,
                                          final AttributeDescriptor descriptor,
                                          final OSClient client) {
        super(hypervisorID, attributeID, DESCRIPTION, TYPE, descriptor, client);
    }

    static String[] getValueCore(final Hypervisor hv) {
        final List<String> result = hv.getCPUInfo().getFeatures();
        return result == null || result.size() == 0 ? new String[0] : ArrayUtils.toArray(result, String.class);
    }

    @Override
    protected String[] getValue(final Hypervisor hv) {
        return getValueCore(hv);
    }
}
