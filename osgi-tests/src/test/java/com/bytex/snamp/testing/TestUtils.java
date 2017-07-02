package com.bytex.snamp.testing;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Represents internal utility methods for SNAMP Test Framework.
 * @author Evgeny Kirichenko
 */
final class TestUtils {
    private TestUtils(){
    }

    static String join(final Object[] objects, final char delimiter)
    {
        final StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < objects.length; i++)
            stringBuilder.append(objects[i]).
                    append(i == objects.length - 1 ? "" : delimiter);
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
}
