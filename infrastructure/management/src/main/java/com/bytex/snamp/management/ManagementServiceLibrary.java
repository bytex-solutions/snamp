package com.bytex.snamp.management;

import com.bytex.snamp.MethodStub;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.ExposedServiceHandler;
import com.bytex.snamp.core.cluster.GridMember;
import com.bytex.snamp.management.jmx.SnampClusterNodeMBean;
import com.bytex.snamp.management.jmx.SnampCoreMBean;
import com.bytex.snamp.management.jmx.SnampManagerImpl;
import com.hazelcast.core.HazelcastInstance;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;

import javax.management.JMException;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;
import java.util.Objects;

/**
 * Represents activator for SNAMP Management Library.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class ManagementServiceLibrary extends AbstractServiceLibrary {
    private static final String USE_PLATFORM_MBEAN_FRAMEWORK_PROPERTY = "com.bytex.snamp.management.usePlatformMBean";
    private static final ActivationProperty<Boolean> usePlatformMBeanProperty = defineActivationProperty(Boolean.class, false);

    private static final class ClusterNodeProvider extends ProvidedService<ClusterMember, GridMember>{
        private ClusterNodeProvider(){
            super(ClusterMember.class, new SimpleDependency<>(HazelcastInstance.class));
        }

        @Override
        protected GridMember activateService(final Map<String, Object> identity,
                                              final RequiredService<?>... dependencies) {
            final HazelcastInstance hazelcast =
                    getDependency(RequiredServiceAccessor.class, HazelcastInstance.class, dependencies);
            return new GridMember(hazelcast);
        }

        @Override
        protected void cleanupService(final GridMember node, final boolean stopBundle) throws InterruptedException {
            node.close();
        }
    }

    private static final class SnampManagerProvider extends ProvidedService<SnampManager, SnampManagerImpl>{

        private SnampManagerProvider() {
            super(SnampManager.class);
        }

        /**
         * Creates a new instance of the service.
         *
         * @param identity     A dictionary of properties that uniquely identifies service instance.
         * @param dependencies A collection of dependencies.
         * @return A new instance of the service.
         */
        @Override
        protected SnampManagerImpl activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) {
            return new SnampManagerImpl();
        }
    }

    private static final class LogReaderServiceDependency extends RequiredServiceAccessor<LogReaderService>{
        private final LogListener listener;

        protected LogReaderServiceDependency(final LogListener listener) {
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

    private static final class SnampClusterNodeMBeanProvider extends OpenMBeanProvider<SnampClusterNodeMBean>{
        private SnampClusterNodeMBeanProvider(){
            super(SnampClusterNodeMBean.OBJECT_NAME);
        }

        @Override
        protected boolean usePlatformMBeanServer(){
            return getActivationPropertyValue(usePlatformMBeanProperty);
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

    private static final class SnampCoreMBeanProvider extends OpenMBeanProvider<SnampCoreMBean>{

        /**
         * Initializes a new holder for the MBean.
         * @throws IllegalArgumentException contract is {@literal null}.
         */
        private SnampCoreMBeanProvider() {
            super(SnampCoreMBean.OBJECT_NAME);
        }

        @Override
        protected boolean usePlatformMBeanServer(){
            return getActivationPropertyValue(usePlatformMBeanProperty);
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

    private static final class LogEntryRouter extends ExposedServiceHandler<FrameworkMBean, LogEntry> implements LogListener{

        private LogEntryRouter() throws InvalidSyntaxException {
            super(FrameworkMBean.class, String.format("(%s=%s)",
                    SnampCoreMBean.OBJECT_NAME_IDENTITY_PROPERTY,
                    SnampCoreMBean.OBJECT_NAME));
        }

        @Override
        protected void handleService(final FrameworkMBean mbean, final LogEntry entry) {
            final LogListener listener = mbean.queryObject(LogListener.class);
            if (listener != null)
                listener.logged(entry);
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
                new SnampClusterNodeMBeanProvider(),
                new SnampCoreMBeanProvider(),
                new ClusterNodeProvider());
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
    }

    /**
     * Activates this service library.
     *
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies         A collection of resolved library-level dependencies.
     */
    @Override
    @MethodStub
    protected void activate(final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) {
        activationProperties.publish(usePlatformMBeanProperty, Objects.equals(getFrameworkProperty(USE_PLATFORM_MBEAN_FRAMEWORK_PROPERTY), "true"));
    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     */
    @Override
    @MethodStub
    protected void deactivate(final ActivationPropertyReader activationProperties) {

    }
}
