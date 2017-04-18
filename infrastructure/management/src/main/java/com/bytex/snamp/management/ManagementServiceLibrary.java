package com.bytex.snamp.management;

import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.ExposedServiceHandler;
import com.bytex.snamp.core.SnampManager;
import com.bytex.snamp.jmx.FrameworkMBean;
import com.bytex.snamp.jmx.OpenMBeanServiceProvider;
import com.bytex.snamp.management.http.ManagementServlet;
import com.bytex.snamp.management.jmx.SnampClusterNodeMBean;
import com.bytex.snamp.management.jmx.SnampCoreMBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

import javax.annotation.Nonnull;
import javax.management.JMException;
import javax.servlet.Servlet;
import java.util.Dictionary;
import java.util.Map;
import java.util.Objects;

/**
 * Represents activator for SNAMP Management Library.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public final class ManagementServiceLibrary extends AbstractServiceLibrary {
    private static final String USE_PLATFORM_MBEAN_FRAMEWORK_PROPERTY = "com.bytex.snamp.management.usePlatformMBean";
    private static final ActivationProperty<Boolean> USE_PLATFORM_MBEAN_ACTIVATION_PROPERTY = defineActivationProperty(Boolean.class, false);

    private static final class SnampManagerProvider extends ProvidedService<SnampManager, DefaultSnampManager>{

        private SnampManagerProvider() {
            super(SnampManager.class);
        }

        @Override
        @Nonnull
        protected DefaultSnampManager activateService(final Map<String, Object> identity) {
            return new DefaultSnampManager();
        }
    }

    private static final class LogReaderServiceDependency extends RequiredServiceAccessor<LogReaderService>{
        private final LogListener listener;

        private LogReaderServiceDependency(final LogListener listener) {
            super(LogReaderService.class);
            this.listener = listener;
        }

        @Override
        protected boolean match(final ServiceReference<?> reference) {
            return true;
        }

        @Override
        protected void bind(final LogReaderService serviceInstance, final Dictionary<String, ?> properties) {
            super.bind(serviceInstance, properties);
            serviceInstance.addLogListener(listener);
        }

        @Override
        protected void unbind() {
            getService().removeLogListener(listener);
            super.unbind();
        }
    }

    private static final class SnampClusterNodeMBeanServiceProvider extends OpenMBeanServiceProvider<SnampClusterNodeMBean> {
        private SnampClusterNodeMBeanServiceProvider(){
            super(SnampClusterNodeMBean.OBJECT_NAME);
        }

        @Override
        protected boolean usePlatformMBeanServer(){
            return getActivationPropertyValue(USE_PLATFORM_MBEAN_ACTIVATION_PROPERTY);
        }

        /**
         * Creates a new instance of MBean.
         *
         * @return A new instance of MBean.
         */
        @Override
        protected SnampClusterNodeMBean createMBean() {
            return new SnampClusterNodeMBean();
        }
    }

    private static final class SnampCoreMBeanServiceProvider extends OpenMBeanServiceProvider<SnampCoreMBean> {

        /**
         * Initializes a new holder for the MBean.
         * @throws IllegalArgumentException contract is {@literal null}.
         */
        private SnampCoreMBeanServiceProvider() {
            super(SnampCoreMBean.OBJECT_NAME);
        }

        @Override
        protected boolean usePlatformMBeanServer(){
            return getActivationPropertyValue(USE_PLATFORM_MBEAN_ACTIVATION_PROPERTY);
        }

        /**
         * Creates a new instance of MBean.
         *
         * @return A new instance of MBean.
         */
        @Override
        protected SnampCoreMBean createMBean() throws JMException{
            return new SnampCoreMBean();
        }
    }

    private static final class LogEntryRouter extends ExposedServiceHandler<FrameworkMBean, LogEntry, ExceptionPlaceholder> implements LogListener{

        private LogEntryRouter() throws InvalidSyntaxException {
            super(FrameworkMBean.class, String.format("(%s=%s)",
                    SnampCoreMBean.OBJECT_NAME_IDENTITY_PROPERTY,
                    SnampCoreMBean.OBJECT_NAME));
        }

        @Override
        protected boolean handleService(final FrameworkMBean mbean, final LogEntry entry) {
            mbean.queryObject(LogListener.class).ifPresent(listener -> listener.logged(entry));
            return true;
        }

        /**
         * Listener method called for each LogEntry object created.
         * As with all event listeners, this method should return to its caller as
         * soon as possible.
         *
         * @param entry A {@code LogEntry} object containing log information.
         * @see org.osgi.service.log.LogEntry
         */
        @Override
        public void logged(final LogEntry entry) {
            handleService(entry);
        }
    }

    private static final class ManagementServletProvider extends ProvidedService<Servlet, ManagementServlet>{
        private ManagementServletProvider(){
            super(Servlet.class);
        }

        @Nonnull
        @Override
        protected ManagementServlet activateService(final Map<String, Object> identity) {
            identity.put("alias", ManagementServlet.CONTEXT);
            return new ManagementServlet();
        }

        @Override
        protected void cleanupService(final ManagementServlet servlet, final boolean stopBundle) {
            servlet.destroy();
        }
    }

    private final LogListener listener;

    public ManagementServiceLibrary() throws InvalidSyntaxException {
        super(new SnampManagerProvider(),
                new SnampClusterNodeMBeanServiceProvider(),
                new SnampCoreMBeanServiceProvider(),
                new ManagementServletProvider());
        this.listener = new LogEntryRouter();
    }

    /**
     * Starts the bundle and instantiate runtime state of the bundle.
     *
     * @param context                 The execution context of the bundle being started.
     * @param bundleLevelDependencies A collection of bundle-level dependencies to fill.
     */
    @Override
    protected void start(final BundleContext context, final DependencyManager bundleLevelDependencies) {
        bundleLevelDependencies.add(new LogReaderServiceDependency(listener));
    }

    @Override
    protected void activate(final BundleContext context, final ActivationPropertyPublisher activationProperties, final DependencyManager dependencies) throws Exception {
        activationProperties.publish(USE_PLATFORM_MBEAN_ACTIVATION_PROPERTY, Objects.equals(getFrameworkProperty(USE_PLATFORM_MBEAN_FRAMEWORK_PROPERTY), "true"));
        super.activate(context, activationProperties, dependencies);
    }
}
