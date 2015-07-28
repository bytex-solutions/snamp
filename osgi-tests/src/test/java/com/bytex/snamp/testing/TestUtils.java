package com.bytex.snamp.testing;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Represents internal utility methods for SNAMP Test Framework.
 * @author Evgeny Kirichenko
 */
final class TestUtils {
    private TestUtils(){
    }

    static String join(final Object[] objects, final char delimeter)
    {
        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < objects.length; i++)
            stringBuilder.append(objects[i]).
                    append(i == objects.length - 1 ? "" : delimeter);
        return stringBuilder.toString();

    }

    static <A extends Annotation> Collection<A> getAnnotations(Class<?> clazz, final Class<A> annotationType){
        final Collection<A> result = new LinkedList<>();
        do {
            final A annotation = clazz.getAnnotation(annotationType);
            if(annotation != null) result.add(annotation);
            clazz = clazz.getSuperclass();
        }
        while (clazz != null);
        return result;
    }

    /**
     * Gets path to the Maven local repository.
     * @return The path to the Maven local repository.
     */
    static Path getMavenLocalRepository() throws MalformedURLException {
        final String localRepository = System.getProperty("mavenLocalRepository", "");
        return localRepository.isEmpty() ? null : Paths.get(new File(localRepository).toURI());
    }
}
