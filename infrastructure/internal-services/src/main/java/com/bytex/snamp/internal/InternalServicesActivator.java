package com.bytex.snamp.internal;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.cluster.GridMember;
import com.bytex.snamp.cluster.NonGridMember;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.concurrent.impl.ThreadPoolRepositoryImpl;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.impl.PersistentConfigurationManager;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.security.web.impl.SecurityServlet;
import com.hazelcast.core.HazelcastInstance;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;

import javax.annotation.Nonnull;
import javax.servlet.Servlet;

/**
 * Represents activator of internal SNAMP services.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.2.0
 * @version 2.1.0
 */
public final class InternalServicesActivator extends AbstractServiceLibrary {
    private static final class ConfigurationServiceManager extends ProvidedService<PersistentConfigurationManager>{

        private ConfigurationServiceManager() {
            super(ConfigurationManager.class, requiredBy(PersistentConfigurationManager.class).require(ConfigurationAdmin.class));
        }

        @Override
        @Nonnull
        protected PersistentConfigurationManager activateService(final ServiceIdentityBuilder identity) {
            return new PersistentConfigurationManager(dependencies.getService(ConfigurationAdmin.class));
        }
    }

    private static final class GridMemberProvider extends ProvidedService<GridMember>{
        private GridMemberProvider(){
            super(ClusterMember.class, requiredBy(GridMember.class).require(HazelcastInstance.class));
        }

        @Override
        @Nonnull
        protected GridMember activateService(final ServiceIdentityBuilder identity) throws Exception {
            //GridMember is very likely to be returned as the default implementation of ClusterMember
            identity.setRank(Integer.MAX_VALUE);
            final HazelcastInstance hazelcast = dependencies.getService(HazelcastInstance.class);
            final GridMember member = new GridMember(hazelcast);
            member.start();
            return member;
        }

        @Override
        protected void cleanupService(final GridMember node, final boolean stopBundle) throws Exception {
            node.close();
        }
    }

    private static final class LocalMemberProvider extends ProvidedService<NonGridMember> {
        private LocalMemberProvider() {
            super(ClusterMember.class);
        }

        @Nonnull
        @Override
        protected NonGridMember activateService(final ServiceIdentityBuilder identity) throws Exception {
            //ServerNode is unlikely to be returned as the default implementation.
            //This is because it can be replaced by GridMember after activation of HazelcastInstance
            identity.setRank(Integer.MIN_VALUE);
            final NonGridMember member = new NonGridMember();
            member.start();
            return member;
        }

        @Override
        protected void cleanupService(final NonGridMember serviceInstance, final boolean stopBundle) throws Exception {
            serviceInstance.close();
        }
    }

    private static final class ThreadPoolRepositoryProvider extends ProvidedService<ThreadPoolRepositoryImpl> {

        private ThreadPoolRepositoryProvider() {
            super(ThreadPoolRepository.class, ManagedService.class);
        }

        @Override
        @Nonnull
        protected ThreadPoolRepositoryImpl activateService(final ServiceIdentityBuilder identity) {
            identity.setServicePID(ThreadPoolRepositoryImpl.PID);
            return new ThreadPoolRepositoryImpl();
        }

        @Override
        protected void cleanupService(final ThreadPoolRepositoryImpl repository, boolean stopBundle) throws Exception {
            if (stopBundle) repository.close();
        }
    }

    private static final class SecurityServletProvider extends ProvidedService<SecurityServlet>{
        private SecurityServletProvider(){
            super(Servlet.class);
        }

        @Override
        @Nonnull
        protected SecurityServlet activateService(final ServiceIdentityBuilder identity) {
            identity.setServletContext(SecurityServlet.CONTEXT);
            final ClusterMember clusterMember = ClusterMember.get(Utils.getBundleContextOfObject(this));
            return new SecurityServlet(clusterMember);
        }

        @Override
        protected void cleanupService(final SecurityServlet servlet, final boolean stopBundle) {
            servlet.destroy();
        }
    }

    @SpecialUse(SpecialUse.Case.OSGi)
    public InternalServicesActivator(){
        super(new GridMemberProvider(),
                new LocalMemberProvider(),
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
