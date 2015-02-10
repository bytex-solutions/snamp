package com.itworks.snamp.management.impl;

import com.itworks.snamp.ServiceReferenceHolder;
import com.itworks.snamp.management.jmx.OpenMBean;
import com.itworks.snamp.security.LoginConfigurationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import static com.itworks.snamp.internal.Utils.getBundleContextByObject;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 * @date 10.02.2015
 */
final class JaasConfigAttribute extends OpenMBean.OpenAttribute<byte[], ArrayType<byte[]>> {
    private static final String NAME = "jaasConfig";

    JaasConfigAttribute() throws OpenDataException {
        super(NAME, ArrayType.getPrimitiveArrayType(byte[].class));
    }

    @Override
    public byte[] getValue() throws IOException {
        final BundleContext context = getBundleContextByObject(this);
        final ServiceReference<LoginConfigurationManager> managerRef =
                context.getServiceReference(LoginConfigurationManager.class);
        if (managerRef == null) return null;
        final ServiceReferenceHolder<LoginConfigurationManager> manager = new ServiceReferenceHolder<>(context, managerRef);
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream(1024)) {
            manager.get().dumpConfiguration(out);
            return out.toByteArray();
        } catch (final Exception e) {
            return null;
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
        final ServiceReferenceHolder<LoginConfigurationManager> manager = new ServiceReferenceHolder<>(context, managerRef);
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