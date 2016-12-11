package com.bytex.snamp.internal;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.cluster.GridMember;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.concurrent.impl.ThreadPoolRepositoryImpl;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.impl.PersistentConfigurationManager;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.security.web.impl.SecurityServlet;
import com.hazelcast.core.HazelcastInstance;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.http.HttpService;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import static com.bytex.snamp.internal.Utils.acceptWithContextClassLoader;

/**
 * Represents activator of internal SNAMP services.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.2.0
 * @version 2.0.0
 */
public final class InternalServicesActivator extends AbstractServiceLibrary {
    private static final ActivationProperty<HttpService> HTTP_SERVICE_ACTIVATION_PROPERTY = defineActivationProperty(HttpService.class);

    private static final class ConfigurationServiceManager extends ProvidedService<ConfigurationManager, PersistentConfigurationManager>{

        private ConfigurationServiceManager() {
            super(ConfigurationManager.class, simpleDependencies(ConfigurationAdmin.class));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected PersistentConfigurationManager activateService(final Map<String, Object> identity) {
            return new PersistentConfigurationManager(getDependencies().getDependency(ConfigurationAdmin.class));
        }
    }

    private static final class ClusterMemberProvider extends ProvidedService<ClusterMember, GridMember>{
        private ClusterMemberProvider(){
            super(ClusterMember.class, simpleDependencies(HazelcastInstance.class));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected GridMember activateService(final Map<String, Object> identity) {
            final HazelcastInstance hazelcast = getDependencies().getDependency(HazelcastInstance.class);
            return new GridMember(hazelcast);
        }

        @Override
        protected void cleanupService(final GridMember node, final boolean stopBundle) throws InterruptedException {
            node.close();
        }
    }

    private static final class ThreadPoolRepositoryProvider extends ProvidedService<ThreadPoolRepository, ThreadPoolRepositoryImpl> {

        private ThreadPoolRepositoryProvider() {
            super(ThreadPoolRepository.class, simpleDependencies(), ManagedService.class);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected ThreadPoolRepositoryImpl activateService(final Map<String, Object> identity) {
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
        bundleLevelDependencies.add(new SimpleDependency<>(HttpService.class));
    }

    @Override
    protected void activate(final ActivationPropertyPublisher activationProperties) throws Exception {
        @SuppressWarnings("unchecked")
        final HttpService httpService = getDependencies().getDependency(HttpService.class);
        acceptWithContextClassLoader(getClass().getClassLoader(),
                httpService,
                (publisher) ->
                        publisher.registerServlet(SecurityServlet.CONTEXT, new SecurityServlet(getLogger()), new Hashtable<>(), null));
        activationProperties.publish(HTTP_SERVICE_ACTIVATION_PROPERTY, httpService);
    }

    @Override
    protected void deactivate(final ActivationPropertyReader activationProperties) {
        activationProperties.getProperty(HTTP_SERVICE_ACTIVATION_PROPERTY).unregister(SecurityServlet.CONTEXT);
    }
}
