package com.snamp.hosting;

import com.snamp.configuration.AgentConfiguration;
import com.snamp.configuration.AgentConfigurationStorage;
import com.snamp.configuration.ConfigurationFormat;
import com.snamp.core.AbstractPlatformService;
import com.snamp.hosting.management.AgentManager;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Represents application startup.
 * @author Roman Sakno
 */
final class Bootstrapper implements HostingContext {
    private static final String CONFIG_STORAGE_LOG_NAME = "com.snamp.configuration.storage";

    /**
     * Represents memory-based configuration storage,
     */
    private final static class MemoryConfigurationStorage extends AbstractPlatformService implements AgentConfigurationStorage{

        private StoredAgentConfiguration storedConfig;

        public MemoryConfigurationStorage(){
            super(CONFIG_STORAGE_LOG_NAME);
        }

        /**
         * Saves the specified agent configuration into the current storage.
         *
         * @param config The configuration to save.
         * @param tag    Tag name for the saved version.
         * @return Persistence information about configuration.
         */
        @Override
        public final StoredAgentConfiguration save(final AgentConfiguration config, final String tag) throws IOException{
            try(final ByteArrayOutputStream os = new ByteArrayOutputStream()){
                config.save(os);
                final Date timeStamp = new Date();
                final byte[] content = os.toByteArray();
                final AgentConfiguration prototype = config.clone();
                return storedConfig = new StoredAgentConfiguration() {
                    @Override
                    public final String tag() {
                        return TAG_LAST;
                    }

                    @Override
                    public final Date lastModified() {
                        return timeStamp;
                    }

                    @Override
                    public final Date firstModified() {
                        return timeStamp;
                    }

                    @Override
                    public AgentConfiguration restore() throws IOException {
                        final AgentConfiguration newInstance = prototype.clone();
                        try(final InputStream is = new ByteArrayInputStream(content)){
                            newInstance.load(is);
                        }
                        return newInstance;
                    }
                };
            }
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
        public final StoredAgentConfiguration getStoredAgentConfiguration(final String tag) {
            return storedConfig;
        }
    }

    /**
     * Represents file-based configuration storage.
     */
    private final static class FileConfigurationStorage extends AbstractPlatformService implements AgentConfigurationStorage{

        private final ConfigurationFormat format;
        private final Path configurationFile;

        public FileConfigurationStorage(final String fileName, final ConfigurationFormat format){
            super(CONFIG_STORAGE_LOG_NAME);
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

    private final Agent agnt;
    private final AgentConfigurationStorage configurationStorage;

    public Bootstrapper(final String configFile, final String configFormat) throws IOException {
        configurationStorage = new FileConfigurationStorage(configFile, ConfigurationFormat.parse(configFormat));
        this.agnt = Agent.start(configurationStorage.getStoredAgentConfiguration(AgentConfigurationStorage.TAG_LAST).restore());
    }

    private static void printHelp(final PrintStream output, final PrintStream err){
        try(final InputStream help = Bootstrapper.class.getResourceAsStream("cmd_help.txt")){
            int b;
            while ((b = help.read()) >= 0)
                output.write(b);
        }
        catch (final IOException e) {
            err.println(e.getLocalizedMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        //prepare startup arguments
        switch (args.length){
            case 1: args = new String[]{args[0], ""}; break;
            case 2: break;
            default:
                printHelp(System.out, System.err);
                return;
        }
        final Bootstrapper snampEntryPoint = new Bootstrapper(args[0], args[1]);
        try(final Agent ag = snampEntryPoint.queryObject(AGENT); final AgentManager manager = ag.getAgentManager(true)){
            manager.start(snampEntryPoint);
        }
    }

    /**
     * Retrieves the service instance.
     *
     * @param objectType Type of the requested service.
     * @param <T>         Type of the required service.
     * @return An instance of the requested service; or {@literal null} if service is not available.
     */
    @Override
    public final <T> T queryObject(final Class<T> objectType) {
        if(objectType == null) return null;
        if(objectType.isInstance(agnt)) return objectType.cast(agnt);
        else if(objectType.isInstance(configurationStorage)) return objectType.cast(configurationStorage);
        else return null;
    }
}
