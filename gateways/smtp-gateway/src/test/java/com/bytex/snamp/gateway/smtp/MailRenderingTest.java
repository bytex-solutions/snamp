package com.bytex.snamp.gateway.smtp;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.compiler.CompiledST;

import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class MailRenderingTest extends Assert {
    @Test
    public void mapRenderingTest() {
        final Map<String, ?> map = ImmutableMap.of("key1", "value1", "key2", "value2");
        final CompiledST compiledTemplate = DefaultMailTemplate.compile("{teams; separator=\"\\n\"}", "dummy");
        final ST template = DefaultMailTemplate.createTemplateRenderer(compiledTemplate);
        template.add("teams", map.entrySet());
        String result = template.render();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
