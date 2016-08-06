package com.bytex.snamp.internal;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.cluster.GridMember;
import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.concurrent.impl.ThreadPoolRepositoryImpl;
import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.impl.PersistentConfigurationManager;
import com.bytex.snamp.core.AbstractServiceLibrary;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.security.LoginConfigurationManager;
import com.bytex.snamp.security.auth.login.json.spi.JsonConfigurationSpi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hazelcast.core.HazelcastInstance;
import org.apache.karaf.jaas.config.JaasRealm;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;
import java.util.Objects;

/**
 * Represents activator of internal SNAMP services.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @since 1.2.0
 * @version 1.2.0
 */
public final class InternalServicesActivator extends AbstractServiceLibrary {
    private static final String BOOT_CONFIG_PROPERTY = "com.bytex.snamp.login.config.boot";
    private static final String DEFAULT_CONFIG_FILE = "jaas.json";

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

    private static final class LoginConfigurationManagerServiceManager extends ProvidedService<LoginConfigurationManager, LoginConfigurationManagerImpl> {
        private final Gson formatter;

        private LoginConfigurationManagerServiceManager(final Gson formatter) {
            super(LoginConfigurationManager.class);
            this.formatter = formatter;
        }

        @Override
        protected LoginConfigurationManagerImpl activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) throws IOException {
            final LoginConfigurationManagerImpl result = new LoginConfigurationManagerImpl(formatter);
            //check for boot configuration
            final File bootConfig = new File(System.getProperty(BOOT_CONFIG_PROPERTY, DEFAULT_CONFIG_FILE));
            if (bootConfig.exists())
                try (final Reader config = new FileReader(bootConfig)) {
                    result.loadConfiguration(config);
                }
            identity.put(LoginConfigurationManager.CONFIGURATION_MIME_TYPE, "application/json");
            return result;
        }
    }

    private static final class RealmsManager extends ServiceSubRegistryManager<JaasRealm, JaasRealmImpl>{
        private final Gson formatter;

        private RealmsManager(final Gson formatter) {
            super(JaasRealm.class);
            this.formatter = Objects.requireNonNull(formatter);
        }

        @Override
        protected String getFactoryPID(final RequiredService<?>[] dependencies) {
            return JaasRealmImpl.FACTORY_PID;
        }

        private JaasRealmImpl createService(final Dictionary<String, ?> configuration){
            return new JaasRealmImpl(formatter, configuration);
        }

        @Override
        protected JaasRealmImpl update(final JaasRealmImpl service, final Dictionary<String, ?> configuration, final RequiredService<?>... dependencies) {
            return createService(configuration);
        }

        @Override
        protected JaasRealmImpl createService(final Map<String, Object> identity, final Dictionary<String, ?> configuration, final RequiredService<?>... dependencies) {
            return createService(configuration);
        }

        @Override
        protected void cleanupService(final JaasRealmImpl service, final Dictionary<String, ?> identity) {

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
            super(ThreadPoolRepository.class, new SimpleDependency<>(ConfigurationAdmin.class));
        }

        @SuppressWarnings("unchecked")
        @Override
        protected ThreadPoolRepositoryImpl activateService(final Map<String, Object> identity, RequiredService<?>... dependencies) throws Exception {
            identity.put(Constants.SERVICE_PID, ThreadPoolRepositoryImpl.PID);
            return new ThreadPoolRepositoryImpl(getDependency(RequiredServiceAccessor.class, ConfigurationAdmin.class, dependencies));
        }

        @Override
        protected void cleanupService(final ThreadPoolRepositoryImpl repository, boolean stopBundle) {
            if (stopBundle) repository.close();
        }
    }

    private InternalServicesActivator(final Gson formatter) {
        super(new LoginConfigurationManagerServiceManager(formatter),
                new RealmsManager(formatter),
                new ClusterMemberProvider(),
                new ThreadPoolRepositoryProvider(),
                new ConfigurationServiceManager());
    }

    @SpecialUse
    public InternalServicesActivator(){
        this(JsonConfigurationSpi.init(new GsonBuilder()).create());
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
