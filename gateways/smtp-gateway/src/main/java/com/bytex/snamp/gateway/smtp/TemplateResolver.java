package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.io.IOUtils;
import org.stringtemplate.v4.compiler.CompiledST;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class TemplateResolver implements Callable<CompiledST> {
    private final String templateLocation;
    private final String templateName;

    TemplateResolver(final String templateName, final String templateLocation){
        this.templateLocation = templateLocation;
        this.templateName = templateName;
    }

    @Override
    public CompiledST call() throws IOException {
        final String template = IOUtils.contentAsString(new URL(templateLocation));
        return DefaultMailTemplate.compile(template, templateName);
    }
}
