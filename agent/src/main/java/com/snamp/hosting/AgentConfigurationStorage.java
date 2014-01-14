package com.snamp.hosting;

import com.snamp.core.PlatformService;

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
         * Gets the tag name.
         * @return The tag name of the persisted configuration.
         */
        public String tag();

        /**
         * Returns the timestamp of the last modification of the persistence.
         * @return The timestamp of the last modification of the persistence.
         */
        public Date lastModified();

        /**
         * Returns the date/when of the configuration creation.
         * @return The date/when of the configuration creation.
         */
        public Date firstModified();

        /**
         * Restores the configuration from the persistence.
         * @return The restored configuration.
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
     * @param tag The tag of the previously stored version
     * @return The persisted version of the configuration.
     */
    public StoredAgentConfiguration getStoredAgentConfiguration(final String tag) throws IOException;
}
