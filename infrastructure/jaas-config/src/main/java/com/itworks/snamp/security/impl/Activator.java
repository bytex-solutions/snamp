package com.itworks.snamp.security.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itworks.snamp.core.AbstractServiceLibrary;
import com.itworks.snamp.security.LoginConfigurationManager;
import com.itworks.snamp.security.auth.login.spi.JsonConfigurationSpi;
import org.apache.karaf.jaas.config.JaasRealm;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class Activator extends AbstractServiceLibrary {
    private static final String BOOT_CONFIG_PROPERTY = "com.itworks.snamp.login.config.boot";
    private static final String DEFAULT_CONFIG_FILE = "jaas.json";

    private static final class LoginConfigurationManagerServiceManager extends ProvidedService<LoginConfigurationManager, LoginConfigurationManagerImpl>{
        private final Gson formatter;

        private LoginConfigurationManagerServiceManager(final Gson formatter){
            super(LoginConfigurationManager.class);
            this.formatter = formatter;
        }

        @Override
        protected LoginConfigurationManagerImpl activateService(final Map<String, Object> identity, final RequiredService<?>... dependencies) throws IOException{
            final LoginConfigurationManagerImpl result =  new LoginConfigurationManagerImpl(formatter);
            //check for boot configuration
            final File bootConfig = new File(System.getProperty(BOOT_CONFIG_PROPERTY, DEFAULT_CONFIG_FILE));
            if(bootConfig.exists())
                try(final InputStream config = new FileInputStream(bootConfig)){
                    result.loadConfiguration(config);
                }
            return result;
        }
    }

    private static final class RealmsManager extends ServiceSubRegistryManager<JaasRealm, JaasRealmImpl>{
        private final Gson formatter;

        private RealmsManager(final Gson formatter) {
            super(JaasRealm.class, JaasRealmImpl.FACTORY_PID);
            this.formatter = Objects.requireNonNull(formatter);
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
        protected void cleanupService(final JaasRealmImpl service) {

        }
    }

    private Activator(final Gson formatter) {
        super(new LoginConfigurationManagerServiceManager(formatter),
                new RealmsManager(formatter));
    }

    public Activator() {
        this(JsonConfigurationSpi.init(new GsonBuilder()).create());
    }

    /**
     * Starts the service library.
     *
     * @param bundleLevelDependencies A collection of library-level dependencies to be required for this library.
     * @throws Exception Unable to start service library.
     */
    @Override
    protected void start(final Collection<RequiredService<?>> bundleLevelDependencies) throws Exception {
        bundleLevelDependencies.add(new SimpleDependency<>(ConfigurationAdmin.class));
    }

    /**
     * Activates this service library.
     *
     * @param activationProperties A collection of library activation properties to fill.
     * @param dependencies         A collection of resolved library-level dependencies.
     * @throws Exception Unable to activate this library.
     */
    @Override
    protected void activate(final ActivationPropertyPublisher activationProperties, final RequiredService<?>... dependencies) throws Exception {

    }

    /**
     * Deactivates this library.
     *
     * @param activationProperties A collection of library activation properties to read.
     * @throws Exception Unable to deactivate this library.
     */
    @Override
    protected void deactivate(final ActivationPropertyReader activationProperties) throws Exception {

    }
}
