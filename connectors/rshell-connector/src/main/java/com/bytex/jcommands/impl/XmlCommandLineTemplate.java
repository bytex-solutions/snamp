package com.bytex.jcommands.impl;

import com.bytex.jcommands.ChannelProcessor;
import org.antlr.runtime.tree.Tree;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.adaptors.CompositeDataAdaptor;
import org.stringtemplate.v4.compiler.CompiledST;
import org.stringtemplate.v4.compiler.STLexer;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.bytex.snamp.internal.Utils.callUnchecked;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.stringtemplate.v4.helpers.CompiledTemplateHelpers.compileTemplate;
import static org.stringtemplate.v4.helpers.CompiledTemplateHelpers.createRenderer;

/**
 * Represents XML-serializable template of the command-line tool.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
@XmlRootElement(name = "template", namespace = XmlConstants.NAMESPACE)
@XmlType(namespace = XmlConstants.NAMESPACE, name = "XmlCommandLineTemplate")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class XmlCommandLineTemplate implements Serializable, ChannelProcessor<Map<String, ?>, Object, ScriptException> {
    private static final long serialVersionUID = -7260435161943556221L;

    private transient CompiledST precompiledTemplate;
    private XmlParserDefinition outputParser;
    private transient ScriptEngineManager scriptManager;

    /**
     * Initializes a new empty command-line tool profile.
     */
    public XmlCommandLineTemplate(){
        outputParser = new XmlParserDefinition();
        scriptManager = null;
        precompiledTemplate = null;
    }

    /**
     * Gets settings of the command-line tool output parser.
     * @return The settings of the command-line tool output parser.
     */
    @XmlElement(name = "output", namespace = XmlConstants.NAMESPACE)
    public final XmlParserDefinition getCommandOutputParser(){
        return outputParser;
    }

    /**
     * Sets settings of the command-line tool output parser.
     * @param value The settings of the parser.
     */
    public final void setCommandOutputParser(final XmlParserDefinition value){
        this.outputParser = value != null ? value : new XmlParserDefinition();
    }

    /**
     * Sets command-line string template.
     * @param value The template of the command-line string.
     */
    @XmlElement(name = "input", namespace = XmlConstants.NAMESPACE)
    public final void setCommandTemplate(final String value) {
        precompiledTemplate = isNullOrEmpty(value) ? null : createCommandTemplate(value);
    }

    /**
     * Gets command-line template associated with this profile.
     * @return The command-line template.
     */
    public final String getCommandTemplate(){
        return precompiledTemplate != null ? precompiledTemplate.template : "";
    }

    /**
     * Creates a new instance of the command template.
     * @param template The template to compile.
     * @return A new instance of the command template.
     */
    public static CompiledST createCommandTemplate(final String template) {
        final STGroup templateGroup = new STGroup('{', '}');
        CommonAdaptors.register(templateGroup);
        CompositeDataAdaptor.register(templateGroup);
        return compileTemplate(templateGroup, template, "CommandLineTemplate");
    }

    private static void findTemplateParameters(final Tree tree, final List<String> output) {
        switch (tree.getType()) {
            case STLexer.ID:
                output.add(tree.getText());
                return;
            default:
                for(int i = 0; i < tree.getChildCount(); i++)
                    findTemplateParameters(tree.getChild(i), output);
        }
    }

    /**
     * Extracts template parameters.
     * @return Immutable list of template parameters.
     */
    public final List<String> extractTemplateParameters(){
        if(precompiledTemplate == null)
            return Collections.emptyList();
        final List<String> result = new ArrayList<>();
        findTemplateParameters(precompiledTemplate.ast, result);
        return result;
    }

    private static ST createCommandTemplate(CompiledST compiledST) {
        compiledST = callUnchecked(compiledST::clone);
        return compiledST.nativeGroup.createStringTemplate(compiledST);
    }

    /**
     * Creates a textual command to be executed through the channel.
     *
     * @param channelParameters The channel initialization parameters. Cannot be {@literal null}.
     * @param input Additional template formatting parameters.
     * @return The command to apply.
     */
    @Override
    public final String renderCommand(final Map<String, ?> channelParameters,
                                                   final Map<String, ?> input) {
        if (precompiledTemplate == null)
            throw new IllegalStateException("Template is not configured");
        final ST template = createRenderer(callUnchecked(precompiledTemplate::clone), channelParameters);
        //fill template attributes from custom input
        if (input != null)
            //attribute name cannot be null or contain '.'
            input.forEach((key, value) -> {
                if (key.indexOf('.') < 0)
                    template.add(key, value);
            });
        return template.render();
    }

    /**
     * Creates a textual command to be executed through the channel.
     *
     * @param channelParameters The channel initialization parameters. Cannot be {@literal null}.
     * @return The command to apply.
     * @throws java.lang.IllegalStateException The command template is not specified.
     */
    public final String renderCommand(final Map<String, ?> channelParameters) throws IllegalStateException {
        return renderCommand(channelParameters, Collections.emptyMap());
    }

    @XmlTransient
    public void setScriptManager(final ScriptEngineManager value){
        scriptManager = value;
    }

    /**
     * Processes the command execution result.
     *
     * @param result The result to parse.
     * @param error  The error message.
     * @return Processing result.
     * @throws javax.script.ScriptException Some non I/O processing exception.
     */
    @Override
    public Object process(final String result, final Exception error) throws ScriptException {
        if (error != null) throw new ScriptException(error);
        return getCommandOutputParser().parse(result,
                scriptManager != null ?
                        scriptManager :
                        new ScriptEngineManager());
    }
}
