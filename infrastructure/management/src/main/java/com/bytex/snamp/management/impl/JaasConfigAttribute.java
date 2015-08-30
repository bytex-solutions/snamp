package com.bytex.snamp.management.impl;

import com.bytex.snamp.StringAppender;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.jmx.OpenMBean;
import com.bytex.snamp.security.LoginConfigurationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static com.bytex.snamp.internal.Utils.getBundleContextByObject;

/**
 * Description here
 *
 * @author Evgeniy Kirichenko
 */
final class JaasConfigAttribute extends OpenMBean.OpenAttribute<String, SimpleType<String>> {
    private static final String NAME = "jaasConfig";

    /**
     * Instantiates a new Jaas config attribute.
     *
     * @throws OpenDataException the open data exception
     */
    JaasConfigAttribute() throws OpenDataException {
        super(NAME, SimpleType.STRING);
    }

    @Override
    public String getValue() throws IOException {
        final BundleContext context = getBundleContextByObject(this);
        final ServiceReference<LoginConfigurationManager> managerRef =
                context.getServiceReference(LoginConfigurationManager.class);
        if (managerRef == null) return null;
        final ServiceHolder<LoginConfigurationManager> manager = new ServiceHolder<>(context, managerRef);
        try (final StringAppender out = new StringAppender(1024)) {
            manager.get().dumpConfiguration(out);
            return out.toString();
        } finally {
            manager.release(context);
        }
    }

    @Override
    public void setValue(final String content) throws IOException {
        final BundleContext context = getBundleContextByObject(this);
        final ServiceReference<LoginConfigurationManager> managerRef =
                context.getServiceReference(LoginConfigurationManager.class);
        if (managerRef == null) throw new RuntimeException("Cannot take LoginConfigurationManager reference");
        final ServiceHolder<LoginConfigurationManager> manager = new ServiceHolder<>(context, managerRef);
        try(final Reader reader = new StringReader(content)) {
            manager.get().loadConfiguration(reader);
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