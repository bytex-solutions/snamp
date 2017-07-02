package com.bytex.snamp.management.jmx;

import com.bytex.snamp.connector.ManagedResourceActivator;
import com.bytex.snamp.internal.Utils;
import com.bytex.snamp.jmx.OpenMBean;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.util.Collection;

import static com.bytex.snamp.ArrayUtils.emptyArray;


/**
 * The type Installed connector attribute.
 * @author Evgeniy Kirichenko
 */
final class InstalledConnectorsAttribute extends OpenMBean.OpenAttribute<String [], ArrayType<String []>> {

    private static final String NAME = "InstalledConnectors";

    /**
     * Instantiates a new Installed connector attribute.
     *
     * @throws OpenDataException the open data exception
     */
    InstalledConnectorsAttribute() throws OpenDataException{
        super(NAME, ArrayType.getArrayType(SimpleType.STRING));
    }

    @Override
    public String[] getValue() throws OpenDataException{
        final Collection<String> result = ManagedResourceActivator.getInstalledResourceConnectors(Utils.getBundleContextOfObject(this));
        return result.toArray(emptyArray(String[].class));
    }
}
