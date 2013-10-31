package com.snamp.hosting;

import com.snamp.hosting.management.AgentManager;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Represents application startup.
 * @author roman
 */
final class Startup implements HostingContext {
    /**
     * Represents file-based configuration storage.
     */
    private final static class FileConfigurationStorage implements AgentConfigurationStorage{

        private final ConfigurationFormat format;
        private final Path configurationFile;

        public FileConfigurationStorage(final String fileName, final ConfigurationFormat format){
            this.configurationFile = Paths.get(fileName);
            this.format = format;
        }

        /**
         * Saves the specified agent configuration into the current storage.
         *
         * @param config The configuration to save.
         * @param tag    Tag name for the saved version.
         * @return Persistence information about configuration.
         */
        @Override
        public StoredAgentConfiguration save(final AgentConfiguration config, final String tag) throws IOException {
            try(final OutputStream os = Files.newOutputStream(configurationFile)){
                config.save(os);
            }
            return getStoredAgentConfiguration(tag);
        }

        /**
         * Determines whether the current storage supports configuration versions (tagging).
         *
         * @return {@literal true}, if the current storage supports configuration versions (tagging); otherwise, {@literal false}.
         */
        @Override
        public final boolean supportsTagging() {
            return false;
        }

        /**
         * Returns persistence information about agent configuration.
         *
         * @param tag
         * @return
         */
        @Override
        public StoredAgentConfiguration getStoredAgentConfiguration(final String tag) throws IOException {
            final BasicFileAttributes fileAttributes = Files.readAttributes(configurationFile, BasicFileAttributes.class);
            return new StoredAgentConfiguration() {
                @Override
                public final String tag() {
                    return TAG_LAST;
                }

                @Override
                public final Date lastModified() {
                    return new Date(fileAttributes.lastModifiedTime().toMillis());
                }

                @Override
                public final Date firstModified() {
                    return new Date(fileAttributes.creationTime().toMillis());
                }

                @Override
                public AgentConfiguration restore() throws IOException {
                    final AgentConfiguration result = format.newAgentConfiguration();
                    try(final InputStream is = Files.newInputStream(configurationFile)){
                        result.load(is);
                    }
                    return result;
                }
            };
        }
    }

    private static final Map<String, AgentConfiguration.ManagementTargetConfiguration> nullTargetsMap = null;
    private final Agent agnt;
    private final AgentConfigurationStorage configurationStorage;

    public Startup(final String configFile, final String configFormat) throws IOException {
        configurationStorage = new FileConfigurationStorage(configFile, ConfigurationFormat.parse(configFormat));
        try(final InputStream stream = new FileInputStream(configFile)){
            this.agnt = Agent.start(configurationStorage.getStoredAgentConfiguration(AgentConfigurationStorage.TAG_LAST).restore());
        }
    }

    public static void main(String[] args) throws Exception {
        //prepare startup arguments
        switch (args.length){
            case 1: args = new String[]{args[0], ""}; break;
            case 2: break;
            default:
                System.out.println("Usage:");
                System.out.println("\tjava snamp config-format config-file");
                System.out.println("\tExample: java snamp yaml mon.yaml");
                return;
        }
        Agent.start(ConfigurationFormat.load(args[0], args[1]));
        //represents REPL server startup.
        try(final AgentManager manager = HostingServices.getAgentManager(true)){
            final Startup repl = new Startup(args[0], args[1]);
            //executes the manager
            manager.start(repl);
            manager.waitForTermination();
        }
    }

    /**
     * Retrieves the service instance.
     *
     * @param serviceType Type of the requested service.
     * @param <T>         Type of the required service.
     * @return An instance of the requested service; or {@literal null} if service is not available.
     */
    @Override
    public final  <T> T getService(final Class<T> serviceType) {
        if(AGENT_SERVICE == serviceType) return (T)agnt;
        else if(CONFIG_STORAGE == serviceType) return (T)configurationStorage;
        else return null;
    }
}
