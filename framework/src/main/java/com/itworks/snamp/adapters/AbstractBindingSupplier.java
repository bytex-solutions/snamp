package com.itworks.snamp.adapters;

import com.itworks.snamp.AbstractAggregator;
import com.itworks.snamp.adapters.runtime.FeatureBinding;

import java.util.Collection;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
abstract class AbstractBindingSupplier extends AbstractAggregator {
    abstract String getInstanceName();
    abstract <B extends FeatureBinding> Collection<B> getBindings(final Class<B> bindingType);
}
