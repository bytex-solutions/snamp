package com.itworks.snamp.licensing;

import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.configuration.AgentConfiguration.ManagedResourceConfiguration;
import com.itworks.snamp.configuration.PersistentConfigurationManager;
import com.itworks.snamp.internal.RecordReader;
import com.itworks.snamp.internal.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;

import javax.management.openmbean.SimpleType;

import java.io.IOException;

import static com.itworks.snamp.jmx.OpenMBean.OpenAttribute;

/**
 * Provides number of managed resources configured in the underlying SNAMP environment.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
final class NumberOfManagedResourcesAttribute extends OpenAttribute<Long, SimpleType<Long>> {
    private static final String NAME = "NumberOfManagedResources";

    NumberOfManagedResourcesAttribute() {
        super(NAME, SimpleType.LONG);
    }

    @Override
    protected String getDescription() {
        return "Number of managed resources";
    }

    private BundleContext getBundleContext(){
        return Utils.getBundleContextByObject(this);
    }

    private static long getNumberOfManagedResources(final ConfigurationAdmin admin) throws Exception{
        final MutableLong result = new MutableLong(0L);
        PersistentConfigurationManager.forEachResource(admin, new RecordReader<String, ManagedResourceConfiguration, ExceptionPlaceholder>() {
            @Override
            public void read(final String index, final ManagedResourceConfiguration value) {
                result.increment();
            }
        });
        return result.get();
    }

    @Override
    public Long getValue() throws Exception {
        final ServiceReferenceHolder<ConfigurationAdmin> configAdminRef = new ServiceReferenceHolder<>(getBundleContext(), ConfigurationAdmin.class);
        try{
            return getNumberOfManagedResources(configAdminRef.get());
        }
        finally {
            configAdminRef.release(getBundleContext());
        }
    }
}
