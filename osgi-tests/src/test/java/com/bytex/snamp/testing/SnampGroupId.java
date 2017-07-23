package com.bytex.snamp.testing;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Represents group of SNAMP artifacts.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
enum SnampGroupId {
    FEATURES("io", "snamp", "features");

    private final String[] relativePath;

    SnampGroupId(final String... path){
        relativePath = path;
    }

    /**
     * Gets path to the Maven local repository.
     * @return The path to the Maven local repository.
     */
    private static Path getMavenLocalRepository() {
        final String localRepository = System.getProperty("mavenLocalRepository", "");
        assert !localRepository.isEmpty();
        return Paths.get(new File(localRepository).toURI());
    }

    String getAbsolutePath() {
        return Paths.get(getMavenLocalRepository().toFile().getAbsolutePath(), relativePath).toUri().toString();
    }
    
    @Override
    public String toString() {
        return TestUtils.join(relativePath, '.');
    }
}
