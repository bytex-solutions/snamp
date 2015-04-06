package com.itworks.snamp.licensing;

import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.management.SnampComponentDescriptor;
import com.itworks.snamp.management.SnampManager;
import org.osgi.framework.BundleContext;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;

import java.util.Collection;

import static com.itworks.snamp.jmx.OpenMBean.OpenAttribute;

/**
 * Exposes an array of installed managed resource adapters.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
final class InstalledResourceAdaptersAttribute extends OpenAttribute<String[], ArrayType<String[]>> {
    private static final String NAME = "InstalledResourceAdapters";

    InstalledResourceAdaptersAttribute() throws OpenDataException {
        super(NAME, new ArrayType<String[]>(SimpleType.STRING, false));
    }

    @Override
    protected String getDescription() {
        return "An array of installed resource adapters";
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextByObject(this);
    }

    private static String[] getInstalledAdapters(final SnampManager manager){
        final Collection<SnampComponentDescriptor> adapters = manager.getInstalledResourceAdapters();
        final String[] result = new String[adapters.size()];
        int index = 0;
        for(final SnampComponentDescriptor adapter: adapters)
            result[index++] = adapter.get(SnampComponentDescriptor.ADAPTER_SYSTEM_NAME_PROPERTY);
        return result;
    }

    @Override
    public String[] getValue() {
        final ServiceReferenceHolder<SnampManager> managerRef = new ServiceReferenceHolder<>(getBundleContext(), SnampManager.class);
        try{
            return getInstalledAdapters(managerRef.get());
        }
        finally {
            managerRef.release(getBundleContext());
        }
    }
}
