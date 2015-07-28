package com.bytex.snamp.adapters.profiles;

import com.bytex.snamp.adapters.AbstractResourceAdapter;

import java.util.Map;

/**
 * Represents an abstract class for resource adapter which behavior controlled by profile.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public abstract class PolymorphicResourceAdapter<P extends ResourceAdapterProfile> extends AbstractResourceAdapter {
    /**
     * Initializes a new resource adapter.
     *
     * @param instanceName The name of the adapter instance.
     */
    protected PolymorphicResourceAdapter(final String instanceName) {
        super(instanceName);
    }

    /**
     * Creates a new instance of the profile using its name and configuration parameters.
     * @param profileName The name of the profile.
     * @param parameters A set of configuration parameters.
     * @return A new instance of the profile. Cannot be {@literal null}.
     */
    protected abstract P createProfile(final String profileName,
                                       final Map<String, String> parameters);

    private P createProfile(final Map<String, String> parameters){
        return createProfile(parameters.containsKey(ResourceAdapterProfile.PROFILE_NAME) ?
                            parameters.get(ResourceAdapterProfile.PROFILE_NAME) :
                            ResourceAdapterProfile.DEFAULT_PROFILE_NAME,
        parameters);
    }

    /**
     * Starts the adapter.
     *
     * @param parameters Adapter startup parameters.
     * @throws Exception Unable to start adapter.
     */
    @Override
    protected final void start(final Map<String, String> parameters) throws Exception {
        start(createProfile(parameters));
    }

    /**
     * Starts the adapter.
     * @param profile The profile of the adapter.
     * @throws Exception Unable to start adapter.
     */
    protected abstract void start(final P profile) throws Exception;

    /**
     * Updates this adapter with a new configuration parameters.
     *
     * @param current       The current configuration parameters.
     * @param newParameters A new configuration parameters.
     * @throws Exception Unable to update this adapter.
     */
    @Override
    protected final void update(final Map<String, String> current, final Map<String, String> newParameters) throws Exception {
        update(createProfile(current), createProfile(newParameters));
    }

    protected void update(@SuppressWarnings("UnusedParameters") final P current,
                          final P newProfile) throws Exception{
        restart(newProfile);
    }
}
