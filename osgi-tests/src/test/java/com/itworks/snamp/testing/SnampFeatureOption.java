package com.itworks.snamp.testing;

import org.ops4j.pax.exam.karaf.options.KarafFeaturesOption;

/**
 * Created by temni on 10.01.15.
 */
final class SnampFeatureOption extends KarafFeaturesOption {
    private static final String PREFIX_REPOSITORY = "file:c:/Users/temni/.m2/repository/com/itworks/snamp";
    private static final String POSTFIX_FILE_NAME = "features.xml";

    SnampFeatureOption(final SnampFeature feature) {
        super(feature.getAbsoluteRepositoryPath(PREFIX_REPOSITORY, POSTFIX_FILE_NAME), feature.featureName);
    }

    private static KarafFeaturesOption[] makeReferences(final SnampFeature... featureList) {
        final KarafFeaturesOption[] result = new KarafFeaturesOption[featureList.length];
        for (int i = 0; i < featureList.length; i++)
            result[i] = new SnampFeatureOption(featureList[i]);
        return result;
    }

    static KarafFeaturesOption[] makeBasicSet() {
        return makeReferences(SnampFeature.BASIC_SET);
    }
}
