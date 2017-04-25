package org.stringtemplate.v4.adaptors;

import org.junit.Assert;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import java.util.Arrays;

/**
 * @author Roman Sakno
 */
public final class AdaptorsTest extends Assert {
    private final STGroup group;

    public AdaptorsTest(){
        group = new STGroup('<', '>');
        ListAdaptor.register(group);
    }

    @Test
    public void listAdaptor(){
        final ST template = new ST(group, "<item.1>");
        template.add("item", Arrays.asList(1, "Frank Underwood"));
        assertEquals("Frank Underwood", template.render());
    }
}
