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
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.http.HttpService;

import javax.management.JMException;
import javax.servlet.Servlet;
import javax.xml.bind.JAXBException;
import java.io.IOException;
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
            super(ConfigurationManager.class, simpleDependencies(ConfigurationAdmin.class));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected PersistentConfigurationManager activateService(final Map<String, Object> identity) {
            return new PersistentConfigurationManager(dependencies.getDependency(ConfigurationAdmin.class));
        }
    }

    private static final class ClusterMemberProvider extends ProvidedService<ClusterMember, GridMember>{
        private ClusterMemberProvider(){
            super(ClusterMember.class, simpleDependencies(HazelcastInstance.class));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected GridMember activateService(final Map<String, Object> identity) throws ReflectiveOperationException, JAXBException, IOException, JMException {
            final HazelcastInstance hazelcast = dependencies.getDependency(HazelcastInstance.class);
            final GridMember member = new GridMember(hazelcast);
            member.start();
            return member;
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

    private static final class SecurityServletProvider extends ProvidedService<Servlet, SecurityServlet>{
        private SecurityServletProvider(){
            super(Servlet.class, simpleDependencies(HttpService.class));
        }

        @Override
        protected SecurityServlet activateService(final Map<String, Object> identity) {
            identity.put("alias", SecurityServlet.CONTEXT);
            return new SecurityServlet();
        }

        @Override
        protected void cleanupService(final SecurityServlet servlet, final boolean stopBundle) {
            servlet.destroy();
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public InternalServicesActivator(){
        super(new ClusterMemberProvider(),
                new ThreadPoolRepositoryProvider(),
                new ConfigurationServiceManager(),
                new SecurityServletProvider());
    }

    /**
     * Starts the bundle and instantiate runtime state of the bundle.
     *
     * @param context                 The execution context of the bundle being started.
     * @param bundleLevelDependencies A collection of bundle-level dependencies to fill.
     * @throws Exception An exception occurred during starting.
     */
    @Override
    protected void start(final BundleContext context, final DependencyManager bundleLevelDependencies) throws Exception {

    }
}
