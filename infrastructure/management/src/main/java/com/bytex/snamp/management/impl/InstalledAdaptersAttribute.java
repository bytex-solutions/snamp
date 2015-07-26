package com.bytex.snamp.management.impl;

import com.bytex.snamp.adapters.ResourceAdapterActivator;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.OpenMBean;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Collection;


/**
 * The type Installed adapters attribute.
 * @author Evgeniy Kirichenko
 */
final class InstalledAdaptersAttribute extends OpenMBean.OpenAttribute<String [], ArrayType<String []>>  {

    private static final String NAME = "InstalledAdapters";

    /**
     * Instantiates a new Installed adapters attribute.
     *
     * @throws OpenDataException the open data exception
     */
    InstalledAdaptersAttribute() throws OpenDataException {
        super(NAME, ArrayType.getArrayType(SimpleType.STRING));
    }

    @Override
    public String[] getValue() throws OpenDataException{
        final Collection<String> result = ResourceAdapterActivator.getInstalledResourceAdapters(Utils.getBundleContextByObject(this));
        return result.toArray(new String[result.size()]);
    }
}
