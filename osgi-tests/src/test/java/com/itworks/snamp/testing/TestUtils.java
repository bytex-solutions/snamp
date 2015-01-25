package com.itworks.snamp.testing;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by temni on 10.01.15.
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
    static String getMavenLocalRepository(){
        return System.getProperty("mavenLocalRepository", "");
    }
}
