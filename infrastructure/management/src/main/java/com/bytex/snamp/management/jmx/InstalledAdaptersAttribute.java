package com.bytex.snamp.management.jmx;

import com.bytex.snamp.gateway.GatewayActivator;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.OpenMBean;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Collection;


/**
 * The type Installed gateway attribute.
 * @author Evgeniy Kirichenko
 */
final class InstalledAdaptersAttribute extends OpenMBean.OpenAttribute<String[], ArrayType<String[]>>  {

    private static final String NAME = "InstalledAdapters";

    /**
     * Instantiates a new Installed gateway attribute.
     *
     * @throws OpenDataException the open data exception
     */
    InstalledAdaptersAttribute() throws OpenDataException {
        super(NAME, ArrayType.getArrayType(SimpleType.STRING));
    }

    @Override
    public String[] getValue() throws OpenDataException{
        final Collection<String> result = GatewayActivator.getInstalledGateways(Utils.getBundleContextOfObject(this));
        return result.toArray(new String[result.size()]);
    }
}
