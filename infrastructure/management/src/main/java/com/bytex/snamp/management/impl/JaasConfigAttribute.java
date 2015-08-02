package com.bytex.snamp.management.impl;

import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.jmx.OpenMBean;
import com.bytex.snamp.security.LoginConfigurationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.bytex.snamp.internal.Utils.getBundleContextByObject;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 */
final class JaasConfigAttribute extends OpenMBean.OpenAttribute<byte[], ArrayType<byte[]>> {
    private static final String NAME = "jaasConfig";

    /**
     * Instantiates a new Jaas config attribute.
     *
     * @throws OpenDataException the open data exception
     */
    JaasConfigAttribute() throws OpenDataException {
        super(NAME, ArrayType.getPrimitiveArrayType(byte[].class));
    }

    @Override
    public byte[] getValue() throws IOException {
        final BundleContext context = getBundleContextByObject(this);
        final ServiceReference<LoginConfigurationManager> managerRef =
                context.getServiceReference(LoginConfigurationManager.class);
        if (managerRef == null) return null;
        final ServiceHolder<LoginConfigurationManager> manager = new ServiceHolder<>(context, managerRef);
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream(1024)) {
            manager.get().dumpConfiguration(out);
            return out.toByteArray();
        } finally {
            manager.release(context);
        }
    }

    @Override
    public void setValue(final byte[] content) throws IOException {
        final BundleContext context = getBundleContextByObject(this);
        final ServiceReference<LoginConfigurationManager> managerRef =
                context.getServiceReference(LoginConfigurationManager.class);
        if (managerRef == null) throw new RuntimeException("Cannot take LoginConfigurationManager reference");
        final ServiceHolder<LoginConfigurationManager> manager = new ServiceHolder<>(context, managerRef);
        try {
            manager.get().loadConfiguration(new ByteArrayInputStream(content));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            manager.release(context);
        }
    }

    @Override
    protected String getDescription() {
        return "SNAMP JAAS Configuration";
    }
}