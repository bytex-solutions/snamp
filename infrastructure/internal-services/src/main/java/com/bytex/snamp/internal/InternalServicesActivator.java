package com.bytex.snamp.internal;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.cluster.GridMember;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.concurrent.impl.ThreadPoolRepositoryImpl;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.impl.PersistentConfigurationManager;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.ClusterMember;
import com.hazelcast.core.HazelcastInstance;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;

import java.util.Collection;
import java.util.Map;

/**
 * Represents activator of internal SNAMP services.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.2.0
 * @version 2.0.0
 */
public final class InternalServicesActivator extends AbstractServiceLibrary {

    private static final class ConfigurationServiceManager extends ProvidedService<ConfigurationManager, PersistentConfigurationManager>{

        private ConfigurationServiceManager() {
            super(ConfigurationManager.class, new SimpleDependency<>(ConfigurationAdmin.class));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected PersistentConfigurationManager activateService(final Map<String, Object> identity, final RequiredService<?>... requiredServices) {
            return new PersistentConfigurationManager(getDependency(RequiredServiceAccessor.class, ConfigurationAdmin.class, requiredServices));
        }
    }

    private static final class ClusterMemberProvider extends ProvidedService<ClusterMember, GridMember>{
        private ClusterMemberProvider(){
            super(ClusterMember.class, new SimpleDependency<>(HazelcastInstance.class));
        }

        @SuppressWarnings("unchecked")
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

    private static final class ThreadPoolRepositoryProvider extends ProvidedService<ThreadPoolRepository, ThreadPoolRepositoryImpl> {

        private ThreadPoolRepositoryProvider() {
            super(ThreadPoolRepository.class, ArrayUtils.emptyArray(RequiredService[].class), ManagedService.class);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected ThreadPoolRepositoryImpl activateService(final Map<String, Object> identity, RequiredService<?>... dependencies) throws Exception {
            identity.put(Constants.SERVICE_PID, ThreadPoolRepositoryImpl.PID);
            return new ThreadPoolRepositoryImpl();
        }

        @Override
        protected void cleanupService(final ThreadPoolRepositoryImpl repository, boolean stopBundle) {
            if (stopBundle) repository.close();
        }
    }

    @SpecialUse
    public InternalServicesActivator(){
        super(new ClusterMemberProvider(),
                new ThreadPoolRepositoryProvider(),
                new ConfigurationServiceManager());
    }

    @Override
    protected void start(final Collection<RequiredService<?>> bundleLevelDependencies) {

    }

    @Override
    protected void activate(final ActivationPropertyPublisher activationProperties, RequiredService<?>... dependencies) {

    }

    @Override
    protected void deactivate(final ActivationPropertyReader activationProperties) {

    }
}