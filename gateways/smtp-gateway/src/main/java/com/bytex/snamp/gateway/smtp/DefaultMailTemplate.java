package com.bytex.snamp.gateway.smtp;

import com.bytex.snamp.io.IOUtils;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.compiler.CompiledST;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import static com.bytex.snamp.internal.Utils.callUnchecked;
import static org.stringtemplate.v4.helpers.CompiledTemplateHelpers.compileTemplate;
import static org.stringtemplate.v4.helpers.CompiledTemplateHelpers.createRenderer;

/**
 * Represents a set of default templates.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
enum DefaultMailTemplate implements Callable<CompiledST> {
    NOTIFICATION("NotificationMessageTemplate.txt"),
    HEALTH_STATUS("HealthStatusTemplate.txt"),
    NEW_RESOURCE("NewResourceTemplate.txt"),
    REMOVED_RESOURCE("RemovedResourceTemplate.txt"),
    SCALE_OUT("ScaleOutTemplate.txt"),
    SCALE_IN("ScaleInTemplate.txt"),
    MAX_CLUSTER_SIZE_REACHED("MaxClusterSizeReachedTemplate.txt");
    
    private final String templateName;

    DefaultMailTemplate(final String templateName) {
        this.templateName = templateName;
    }

    @Override
    public CompiledST call() throws IOException {
        final String template;
        try (final InputStream stream = getClass().getResourceAsStream(templateName)) {
            template = IOUtils.toString(stream);
        }
        return compile(template, name());
    }

    static CompiledST compile(final String template, final String templateName){
        final STGroup templateGroup = new STGroup('{', '}');
        return compileTemplate(templateGroup, template, templateName);
    }

    static ST createTemplateRenderer(final CompiledST compiledTemplate) {
        return createRenderer(callUnchecked(compiledTemplate::clone));
    }
}
