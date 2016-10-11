package com.bytex.snamp.connector.composite;

import com.bytex.snamp.concurrent.Repeater;
import com.bytex.snamp.connector.AbstractFeatureRepository;

import java.util.Objects;

/**
 * Provides cluster-wide synchronization
 */
final class AttributeSynchronizationJob extends Repeater {
    private final AbstractFeatureRepository<AbstractCompositeAttribute> attributes;

    AttributeSynchronizationJob(final long period, AbstractFeatureRepository<AbstractCompositeAttribute> attributes) {
        super(period);
        this.attributes = Objects.requireNonNull(attributes);
    }

    @Override
    protected void doAction() {

    }
}
