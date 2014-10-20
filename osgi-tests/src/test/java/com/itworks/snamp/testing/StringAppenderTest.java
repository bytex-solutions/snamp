package com.itworks.snamp.testing;

import com.itworks.snamp.StringAppender;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class StringAppenderTest extends AbstractUnitTest<StringAppender> {
    public StringAppenderTest(){
        super(StringAppender.class);
    }

    @Test
    public void simpleTest(){
        final String result = StringAppender.init()
                .appendln("Hello, %s", "world!")
                .join(Arrays.asList(1, 2, 3), StringAppender.toStringTransformer(), ",")
                .toString();
        assertEquals("Hello, world!" + System.lineSeparator() + "1,2,3", result);
    }
}
