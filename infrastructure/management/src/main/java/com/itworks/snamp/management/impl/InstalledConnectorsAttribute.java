package com.itworks.snamp.management.impl;

import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.OpenMBean;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Collection;

/**
 * Created by temni on 2/8/2015.
 */
final class InstalledConnectorsAttribute extends OpenMBean.OpenAttribute<String [], ArrayType<String []>> {

    private static final String NAME = "InstalledConnectors";

    InstalledConnectorsAttribute() throws OpenDataException{
        super(NAME, ArrayType.getArrayType(SimpleType.STRING));
    }

    @Override
    public String[] getValue() throws OpenDataException{
        final Collection<String> result = ManagedResourceActivator.getInstalledResourceConnectors(Utils.getBundleContextByObject(this));
        return result.toArray(new String[result.size()]);
    }
}
