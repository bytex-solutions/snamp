package com.snamp.hosting;

import com.snamp.PlatformService;

import java.io.IOException;
import java.util.Date;

/**
 * Represents a storage for {@link AgentConfiguration} instance.
 * @author Roman Sakno
 */
public interface AgentConfigurationStorage extends PlatformService {
    /**
     * Represents name of the storage tag that holds the last version of agent configuration.
     */
    static final String TAG_LAST = "LAST";

    /**
     * Represents persisted agent configuration.
     */
    public static interface StoredAgentConfiguration{
        /**
         * Gets tag name.
         * @return
         */
        public String tag();

        /**
         *
         * @return
         */
        public Date lastModified();
        public Date firstModified();

        /**
         * Restores the configuration from the persistence.
         * @return
         */
        public AgentConfiguration restore() throws IOException;
    }

    /**
     * Saves the specified agent configuration into the current storage.
     * @param config The configuration to save.
     * @param tag Tag name for the saved version.
     * @return Persistence information about configuration.
     */
    public StoredAgentConfiguration save(final AgentConfiguration config, final String tag) throws IOException;

    /**
     * Determines whether the current storage supports configuration versions (tagging).
     * @return {@literal true}, if the current storage supports configuration versions (tagging); otherwise, {@literal false}.
     */
    public boolean supportsTagging();

    /**
     * Returns persistence information about agent configuration.
     * @param tag
     * @return
     */
    public StoredAgentConfiguration getStoredAgentConfiguration(final String tag) throws IOException;
}
