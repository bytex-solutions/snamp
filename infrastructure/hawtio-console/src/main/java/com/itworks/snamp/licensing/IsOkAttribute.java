package com.itworks.snamp.licensing;

import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.configuration.AgentConfiguration;
import com.itworks.snamp.configuration.PersistentConfigurationManager;
import com.itworks.snamp.internal.RecordReader;
import com.itworks.snamp.internal.Utils;
import com.itworks.snamp.jmx.OpenMBean;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.management.openmbean.SimpleType;
import javax.xml.bind.JAXBException;
import java.util.Collection;
import java.util.LinkedHashSet;

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

    private static long getNumberOfManagedResources(final ConfigurationAdmin admin) throws Exception {
        final MutableLong result = new MutableLong(0L);
        PersistentConfigurationManager.forEachResource(admin, new RecordReader<String, AgentConfiguration.ManagedResourceConfiguration, ExceptionPlaceholder>() {
            @Override
            public void read(final String index, final AgentConfiguration.ManagedResourceConfiguration value) {
                result.increment();
            }
        });
        return result.get();
    }

    private static String[] getActiveAdapters(final ConfigurationAdmin admin) throws Exception {
        final Collection<String> adapters = new LinkedHashSet<>();
        PersistentConfigurationManager.forEachAdapter(admin, new RecordReader<String, AgentConfiguration.ResourceAdapterConfiguration, ExceptionPlaceholder>() {
            @Override
            public void read(final String instanceName, final AgentConfiguration.ResourceAdapterConfiguration config) {
                adapters.add(config.getAdapterName());
            }
        });
        return ArrayUtils.toArray(adapters, String.class);
    }

    @Override
    public Boolean getValue() throws Exception {
        final BundleContext context = Utils.getBundleContextByObject(this);
        //collect license-sensitive information
        final ServiceReferenceHolder<ConfigurationAdmin> configAdminRef = new ServiceReferenceHolder<>(context, ConfigurationAdmin.class);
        final long numberOfResources;
        final String[] adapters;
        try {
            numberOfResources = getNumberOfManagedResources(configAdminRef.get());
            adapters = getActiveAdapters(configAdminRef.get());
        } finally {
            configAdminRef.release(context);
        }
        final XmlLicense license = getLicense();
        return license.checkNumberOfManagedResources(numberOfResources) &&
                license.isAdaptersAllowed(adapters);
    }

    private XmlLicense getLicense() throws JAXBException {
        final OpenMBean owner = getOwner();
        return owner instanceof LicenseProvider ?
                ((LicenseProvider) owner).getLicense() :
                new XmlLicense();
    }
}