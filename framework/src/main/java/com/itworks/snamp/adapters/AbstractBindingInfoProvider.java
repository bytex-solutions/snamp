package com.itworks.snamp.adapters;

import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.adapters.binding.FeatureBindingInfo;

import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractBindingInfoProvider extends AbstractAggregator {
    abstract String getInstanceName();
    abstract <B extends FeatureBindingInfo> Collection<? extends B> getBindings(final Class<B> bindingType);
}
