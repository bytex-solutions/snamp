package com.bytex.jcommands.impl;

import com.bytex.jcommands.ChannelProcessor;
import com.bytex.snamp.ThreadSafe;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

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
    private static final STGroup TEMPLATE_GROUP;
    private static final long serialVersionUID = -7260435161943556221L;

    static {
        TEMPLATE_GROUP = new STGroup('{', '}');
        CommonExtender.register(TEMPLATE_GROUP);
        CompositeDataExtender.register(TEMPLATE_GROUP);
    }

    private String template;
    private transient ST precompiledTemplate;
    private XmlParserDefinition outputParser;
    private transient ScriptEngineManager scriptManager;

    /**
     * Initializes a new empty command-line tool profile.
     */
    public XmlCommandLineTemplate(){
        template = "";
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
        if (value == null || value.isEmpty()) {
            template = "";
            precompiledTemplate = null;
        } else precompiledTemplate = createCommandTemplate(template = value);
    }

    /**
     * Gets command-line template associated with this profile.
     * @return The command-line template.
     */
    public final String getCommandTemplate(){
        return template;
    }

    /**
     * Creates a new instance of the command template.
     * @param template The template to compile.
     * @return A new instance of the command template.
     */
    public static ST createCommandTemplate(final String template) {
        return new ST(TEMPLATE_GROUP, template);
    }

    /**
     * Creates a textual command to be executed through the channel.
     *
     * @param channelParameters The channel initialization parameters. Cannot be {@literal null}.
     * @param input Additional template formatting parameters.
     * @return The command to apply.
     */
    @Override
    @ThreadSafe(true)
    public final String renderCommand(final Map<String, ?> channelParameters,
                                                   final Map<String, ?> input) {
        final ST template = precompiledTemplate != null ?
                new ST(precompiledTemplate) :
                createCommandTemplate(this.template);
        //fill template attributes from channel parameters
        for (final Map.Entry<String, ?> pair : channelParameters.entrySet())
            template.add(pair.getKey(), pair.getValue());
        //fill template attributes from custom input
        if (input != null)
            //attribute name cannot be null or contain '.'
            input.entrySet().stream()
                    .filter(pair -> pair.getKey().indexOf('.') < 0)
                    .forEach(pair -> template.add(pair.getKey(), pair.getValue()));
        return template.render();
    }

    /**
     * Creates a textual command to be executed through the channel.
     *
     * @param channelParameters The channel initialization parameters. Cannot be {@literal null}.
     * @return The command to apply.
     * @throws java.lang.IllegalStateException The command template is not specified.
     */
    @ThreadSafe()
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
    @ThreadSafe(false)
    public Object process(final String result, final Exception error) throws ScriptException {
        if (error != null) throw new ScriptException(error);
        return getCommandOutputParser().parse(result,
                scriptManager != null ?
                        scriptManager :
                        new ScriptEngineManager());
    }
}
