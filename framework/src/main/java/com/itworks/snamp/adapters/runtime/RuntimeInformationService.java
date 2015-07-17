package com.itworks.snamp.adapters.runtime;

import com.itworks.snamp.core.SupportService;

import java.util.Collection;
import java.util.Set;

/**
 * Represents internal information about binding of features.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public interface RuntimeInformationService extends SupportService {
    /**
     * Gets a collection of bound features.
     * @param bindingType Type of the feature binding.
     * @param <B> Type of the feature binding.
     * @return A collection of features; or empty collection if binding is not supported.
     */
    <B extends FeatureBinding> Collection<? extends B> getBindingInfo(final String adapterInstanceName,
                                                            final Class<B> bindingType);

    /**
     * Gets a set of instantiated adapters.
     * @return A set of instantiated adapters.
     */
    Set<String> getAdapterInstances();
}
