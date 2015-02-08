package com.itworks.snamp.management.impl;

import com.itworks.snamp.connectors.ManagedResourceActivator;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.management.jmx.OpenMBean;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Collection;

/**
 * Created by temni on 2/8/2015.
 */
final class GetInstalledConnectorsOperation extends OpenMBean.OpenAttribute<String [], ArrayType<String []>> {

    private static final String NAME = "getInstalledConnectorsOperation";

    GetInstalledConnectorsOperation() throws OpenDataException{
        super(NAME, new ArrayType<String[]>(SimpleType.STRING, true));
    }

    @Override
    public String[] getValue() throws OpenDataException{
        final Collection<String> result = ManagedResourceActivator.getInstalledResourceConnectors(Utils.getBundleContextByObject(this));
        return result.toArray(new String[result.size()]);
    }
}
