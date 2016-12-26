package com.bytex.snamp.management;

import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.MethodStub;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.ExposedServiceHandler;
import com.bytex.snamp.core.SnampManager;
import com.bytex.snamp.jmx.FrameworkMBean;
import com.bytex.snamp.jmx.OpenMBeanServiceProvider;
import com.bytex.snamp.management.http.ManagementServlet;
import com.bytex.snamp.management.jmx.SnampClusterNodeMBean;
import com.bytex.snamp.management.jmx.SnampCoreMBean;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

import javax.management.JMException;
import java.util.*;

import static com.bytex.snamp.internal.Utils.acceptWithContextClassLoader;

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
    private static final ActivationProperty<HttpService> HTTP_SERVICE_ACTIVATION_PROPERTY = defineActivationProperty(HttpService.class);

    private static final class SnampManagerProvider extends ProvidedService<SnampManager, SnampManagerImpl>{

        private SnampManagerProvider() {
            super(SnampManager.class);
        }

        @Override
        protected SnampManagerImpl activateService(final Map<String, Object> identity) {
            return new SnampManagerImpl();
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
            final LogListener listener = mbean.queryObject(LogListener.class);
            if (listener != null)
                listener.logged(entry);
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

    private final LogListener listener;

    public ManagementServiceLibrary() throws InvalidSyntaxException {
        super(new SnampManagerProvider(),
                new SnampClusterNodeMBeanServiceProvider(),
                new SnampCoreMBeanServiceProvider());
        this.listener = new LogEntryRouter();
    }

    /**
     * Starts the service library.
     *
     * @param bundleLevelDependencies A collection of library-level dependencies to be required for this library.
     */
    @Override
    protected void start(final Collection<RequiredService<?>> bundleLevelDependencies) {
        bundleLevelDependencies.add(new LogReaderServiceDependency(listener));
        bundleLevelDependencies.add(new SimpleDependency<>(HttpService.class));
    }

    /**
     * Activates this service library.
     *
     * @param activationProperties A collection of library activation properties to fill.
     */
    @Override
    @MethodStub
    protected void activate(final ActivationPropertyPublisher activationProperties) throws Exception {
        activationProperties.publish(USE_PLATFORM_MBEAN_ACTIVATION_PROPERTY, Objects.equals(getFrameworkProperty(USE_PLATFORM_MBEAN_FRAMEWORK_PROPERTY), "true"));
        final HttpService httpService = getDependencies().getDependency(HttpService.class);
        acceptWithContextClassLoader(getClass().getClassLoader(),
                httpService,
                (publisher) -> publisher.registerServlet(ManagementServlet.CONTEXT, new ManagementServlet(), new Hashtable<>(), null));
        activationProperties.publish(HTTP_SERVICE_ACTIVATION_PROPERTY, httpService);
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     */
    @Override
    @MethodStub
    protected void deactivate(final ActivationPropertyReader activationProperties) {
        final HttpService httpService = activationProperties.getProperty(HTTP_SERVICE_ACTIVATION_PROPERTY);
        httpService.unregister(ManagementServlet.CONTEXT);
    }
}
