package com.itworks.jcommands.impl;

import com.itworks.jcommands.ChannelProcessor;
import com.itworks.snamp.internal.semantics.ThreadSafe;
import org.stringtemplate.v4.ST;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.bind.annotation.*;
import java.util.Map;

/**
 * Represents XML-serializable template of the command-line tool.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@XmlRootElement(name = "template", namespace = XmlConstants.NAMESPACE)
@XmlType(namespace = XmlConstants.NAMESPACE, name = "XmlCommandLineTemplate")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class XmlCommandLineTemplate implements ChannelProcessor<Object, ScriptException> {

    private String template;
    private XmlParserDefinition outputParser;
    private transient ScriptEngineManager scriptManager;
    private static final char TEMPLATE_DELIMITER_START_CHAR = '{';
    private static final char TEMPLATE_DELIMITER_STOP_CHAR = '}';

    /**
     * Initializes a new empty command-line tool profile.
     */
    public XmlCommandLineTemplate(){
        template = "";
        outputParser = new XmlParserDefinition();
        scriptManager = null;
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
        this.template = value != null ? value : "";
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
     * @return A new instance of the command template.
     */
    public ST createCommandTemplate(){
        return new ST(this.template, TEMPLATE_DELIMITER_START_CHAR, TEMPLATE_DELIMITER_STOP_CHAR);
    }

    /**
     * Creates a textual command to be executed through the channel.
     *
     * @param channelParameters The channel initialization parameters. Cannot be {@literal null}.
     * @return The command to execute.
     * @throws java.lang.IllegalStateException The command template is not specified.
     */
    @Override
    @ThreadSafe(false)
    public final String renderCommand(final Map<String, ?> channelParameters) throws IllegalStateException {
        final ST template = createCommandTemplate();
        //fill template attributes from channel parameters
        for (final Map.Entry<String, ?> pair : channelParameters.entrySet())
            template.add(pair.getKey(), pair.getValue());
        return template.render();
    }

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
        return getCommandOutputParser().parse(result, scriptManager != null ? scriptManager : new ScriptEngineManager());
    }
}
