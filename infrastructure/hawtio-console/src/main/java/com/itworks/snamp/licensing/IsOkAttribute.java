package com.itworks.snamp.licensing;

import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.PersistentConfigurationManager;
import com.itworks.snamp.internal.RecordReader;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.OpenMBean;
import com.itworks.snamp.management.SnampComponentDescriptor;
import com.itworks.snamp.management.SnampManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.management.openmbean.SimpleType;
import javax.xml.bind.JAXBException;

import java.util.Collection;

import static com.itworks.snamp.jmx.OpenMBean.OpenAttribute;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class IsOkAttribute extends OpenAttribute<Boolean, SimpleType<Boolean>> {
    private static final String NAME = "IsOK";

    IsOkAttribute() {
        super(NAME, SimpleType.BOOLEAN);
    }

    @Override
    public boolean isIs() {
        return true;
    }

    private static long getNumberOfManagedResources(final ConfigurationAdmin admin) throws Exception{
        final MutableLong result = new MutableLong(0L);
        PersistentConfigurationManager.forEachResource(admin, new RecordReader<String, AgentConfiguration.ManagedResourceConfiguration, ExceptionPlaceholder>() {
            @Override
            public void read(final String index, final AgentConfiguration.ManagedResourceConfiguration value) {
                result.increment();
            }
        });
        return result.get();
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
    public Boolean getValue() throws Exception{
        final BundleContext context = Utils.getBundleContextByObject(this);
        //collect license-sensitive information
        final ServiceReferenceHolder<ConfigurationAdmin> configAdminRef = new ServiceReferenceHolder<>(context, ConfigurationAdmin.class);
        final ServiceReferenceHolder<SnampManager> managedRef = new ServiceReferenceHolder<>(context, SnampManager.class);
        final long numberOfResources;
        final String[] adapters;
        try{
            numberOfResources = getNumberOfManagedResources(configAdminRef.get());
            adapters = getInstalledAdapters(managedRef.get());
        }
        finally {
            configAdminRef.release(context);
            managedRef.release(context);
        }
        final XmlLicense license = getLicense();
        return license.checkNumberOfManagedResources(numberOfResources) &&
                license.isAdaptersAllowed(adapters);
    }

    private XmlLicense getLicense() throws JAXBException {
        final OpenMBean owner = getOwner();
        return owner instanceof LicenseProvider ?
                ((LicenseProvider)owner).getLicense():
                new XmlLicense();
    }
}
