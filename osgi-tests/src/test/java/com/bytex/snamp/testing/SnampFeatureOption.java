package com.bytex.snamp.testing;

import org.ops4j.pax.exam.karaf.options.KarafFeaturesOption;

import java.net.MalformedURLException;

/**
 * Represents SNAMP-related feature definition.
 * @author Evgeny Kirichenko
 */
final class SnampFeatureOption extends KarafFeaturesOption {

    SnampFeatureOption(final SnampFeature feature) throws MalformedURLException {
        super(feature.getFeatureFile(), feature.featureNames);
    }
}
