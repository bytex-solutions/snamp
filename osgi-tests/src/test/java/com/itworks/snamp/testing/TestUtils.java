package com.itworks.snamp.testing;

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
}
