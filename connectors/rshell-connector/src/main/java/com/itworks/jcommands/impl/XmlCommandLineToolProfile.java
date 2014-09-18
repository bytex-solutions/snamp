package com.itworks.jcommands.impl;

import com.itworks.jcommands.ChannelProcessor;
import com.itworks.snamp.internal.semantics.ThreadSafe;
import org.stringtemplate.v4.ST;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Represents XML-serializable profile of the command-line tool.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@XmlRootElement(name = "profile", namespace = XmlConstants.NAMESPACE)
@XmlType(namespace = XmlConstants.NAMESPACE, name = "XmlCommandLineToolProfile")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class XmlCommandLineToolProfile implements ChannelProcessor<Object, Exception> {

    private String template;
    private XmlCommandLineToolOutputParser outputParser;
    private static final char TEMPLATE_DELIMITER_START_CHAR = '{';
    private static final char TEMPLATE_DELIMITER_STOP_CHAR = '}';

    /**
     * Initializes a new empty command-line tool profile.
     */
    public XmlCommandLineToolProfile(){
        template = "";
        outputParser = new XmlCommandLineToolOutputParser();
    }

    /**
     * Gets settings of the command-line tool output parser.
     * @return The settings of the command-line tool output parser.
     */
    @XmlElement(name = "output", namespace = XmlConstants.NAMESPACE)
    public final XmlCommandLineToolOutputParser getCommandOutputParser(){
        return outputParser;
    }

    /**
     * Sets settings of the command-line tool output parser.
     * @param value The settings of the parser.
     */
    public final void setCommandOutputParser(final XmlCommandLineToolOutputParser value){
        this.outputParser = value != null ? value : new XmlCommandLineToolOutputParser();
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
    public final String renderCommand(final Map<String, String> channelParameters) throws IllegalStateException {
        final ST template = createCommandTemplate();
        //fill template attributes from channel parameters
        for (final Map.Entry<String, String> pair : channelParameters.entrySet())
            template.add(pair.getKey(), pair.getValue());
        return template.render();
    }

    /**
     * Processes the command execution result.
     *
     * @param result The result to parse.
     * @param error  The error message.
     * @return Processing result.
     * @throws java.lang.Exception Some non I/O processing exception.
     */
    @Override
    @ThreadSafe(false)
    public Object process(final String result, final Exception error) throws Exception {
        if(error != null) throw error;
        return null;
    }

    public void saveTo(final OutputStream output){
        JAXB.marshal(this, output);
    }

    public void saveTo(final File output){
        JAXB.marshal(this, output);
    }

    /**
     * Returns XML representation of this profile.
     * @return XML representation of this profile.
     */
    @Override
    public String toString() {
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024)) {
            saveTo(outputStream);
            return new String(outputStream.toByteArray());
        } catch (final IOException e) {
            return e.toString();
        }
    }
}
