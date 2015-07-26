package com.bytex.snamp.adapters.profiles;

import com.bytex.snamp.Aggregator;

import java.util.Map;

/**
 * Represents profile of the resource adapter.
 * <p>
 *     The map represented by this profile is read-only.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface ResourceAdapterProfile extends Map<String, String>, Aggregator, Cloneable {
    /**
     * The name of the configuration parameter that holds the name of the profile.
     */
    String PROFILE_NAME = "profile";

    /**
     * The name of the default profile.
     */
    String DEFAULT_PROFILE_NAME = "DEFAULT";

    /**
     * Clones this profile.
     * @return A new cloned instance of this profile.
     */
    ResourceAdapterProfile clone();
}
