package com.bytex.snamp.testing;

import org.ops4j.pax.exam.karaf.options.KarafFeaturesOption;

import java.net.MalformedURLException;
import java.nio.file.Paths;

import static com.bytex.snamp.testing.TestUtils.getMavenLocalRepository;

/**
 * Represents SNAMP-related feature definition.
 * @author Evgeny Kirichenko
 */
final class SnampFeatureOption extends KarafFeaturesOption {
    private static final String POSTFIX_FILE_NAME = "features.xml";

    private static String getPrefixRepository() throws MalformedURLException {
        return Paths.get(getMavenLocalRepository().toFile().getAbsolutePath(), "com", "bytex", "snamp")
                .toUri().toString();
    }

    SnampFeatureOption(final SnampFeature feature) throws MalformedURLException {
        super(feature.getAbsoluteRepositoryPath(getPrefixRepository(), POSTFIX_FILE_NAME), feature.featureNames);
    }
}
