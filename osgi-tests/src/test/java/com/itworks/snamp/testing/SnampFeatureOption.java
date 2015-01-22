package com.itworks.snamp.testing;

import org.ops4j.pax.exam.karaf.options.KarafFeaturesOption;

/**
 * Represents SNAMP-related feature definition.
 * @author Evgeny Kirichenko
 */
final class SnampFeatureOption extends KarafFeaturesOption {
    private static final String PREFIX_REPOSITORY = "file:/home/roman/.m2/repository/com/itworks/snamp";
    private static final String POSTFIX_FILE_NAME = "features.xml";

    SnampFeatureOption(final SnampFeature feature) {
        super(feature.getAbsoluteRepositoryPath(PREFIX_REPOSITORY, POSTFIX_FILE_NAME), feature.featureName);
    }
}
