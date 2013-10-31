package com.snamp.hosting;

import com.snamp.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.Date;

import static com.snamp.ConcurrentResourceHolder.Writer;
import static com.snamp.ConcurrentResourceHolder.ConsistentReader;
import static com.snamp.ConcurrentResourceHolder.Reader;

import static com.snamp.hosting.AgentConfigurationStorage.StoredAgentConfiguration;

/**
 * Represents current configuration storage.
 * @author roman
 */
final class CurrentConfigurationStorage {
    /**
     * Represents memory-based configuration storage,
     */
    private final static class MemoryConfigurationStorage implements AgentConfigurationStorage{

        private StoredAgentConfiguration storedConfig;

        public MemoryConfigurationStorage(){
            storedConfig = null;
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

    private static final ConcurrentResourceHolder<AgentConfigurationStorage> currentStorage;

    static {
        currentStorage = new ConcurrentResourceHolder<AgentConfigurationStorage>(new MemoryConfigurationStorage());
    }

    /**
     * Maps the current configuration storage to the specified file.
     * @param configurationFileName The path to the configuration file.
     * @param format Configuration file format.
     */
    public static final void switchToFile(final String configurationFileName, final ConfigurationFormat format){
        currentStorage.changeResource(new FileConfigurationStorage(configurationFileName, format));
    }

    public static final void switchToMemory(){
        currentStorage.changeResource(new MemoryConfigurationStorage());
    }

    public static final StoredAgentConfiguration save(final AgentConfiguration config, final String tag) throws IOException{
        return currentStorage.write(new Writer<AgentConfigurationStorage, Void, StoredAgentConfiguration, IOException>() {
            @Override
            public StoredAgentConfiguration write(final AgentConfigurationStorage resource, final Void value) throws IOException {
                return resource.save(config, tag);
            }
        }, null);
    }

    public static final boolean supportsTagging(){
        return currentStorage.read(new ConsistentReader<AgentConfigurationStorage, Boolean>() {
            @Override
            public Boolean read(final AgentConfigurationStorage resource) {
                return resource.supportsTagging();
            }
        });
    }

    public static final StoredAgentConfiguration getStoredAgentConfiguration(final String tag) throws IOException{
        return currentStorage.read(new Reader<AgentConfigurationStorage, StoredAgentConfiguration, IOException>() {
            @Override
            public StoredAgentConfiguration read(final AgentConfigurationStorage resource) throws IOException {
                return resource.getStoredAgentConfiguration(tag);
            }
        });
    }
}
