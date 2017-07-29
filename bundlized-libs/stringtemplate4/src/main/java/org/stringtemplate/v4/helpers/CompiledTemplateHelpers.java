package org.stringtemplate.v4.helpers;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.compiler.CompiledST;

import java.util.Collections;
import java.util.Map;

/**
 * Represents helpers for instantiating and reusing compiled templates.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class CompiledTemplateHelpers {
    private CompiledTemplateHelpers(){
        throw new InstantiationError();
    }

    public static CompiledST compileTemplate(final STGroup group, final String template, final String templateName){
        final CompiledST result = group.compile(group.getFileName(),
                templateName,
                null,
                template,
                null);
        result.defineImplicitlyDefinedTemplates(result.nativeGroup);
        result.hasFormalArgs = false;
        return result;
    }

    public static ST createRenderer(final CompiledST prototype, final Map<String, ?> parameters) {
        final ST template = prototype.nativeGroup.createStringTemplate(prototype);
        parameters.forEach(template::add);
        return template;
    }

    public static ST createRenderer(final CompiledST prototype){
        return createRenderer(prototype, Collections.emptyMap());
    }
}
