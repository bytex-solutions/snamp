package com.itworks.jcommands.impl;

import com.google.common.collect.ImmutableMap;
import com.itworks.jcommands.CommandExecutionChannel;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.*;
import java.io.*;
import java.util.Map;

/**
 * Represents profile for the command-line tool.
 * <p>
 *     By default, the profile contains two predefined actions
 *     associated with command-line tool:
 *     <ul>
 *         <li>Reader - uses for obtaining some information about underlying system using the tool.</li>
 *         <li>Modifier - uses for modifying of underlying system using the tool.</li>
 *     </ul>
 * </p>
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@XmlRootElement(name = "profile", namespace = XmlConstants.NAMESPACE)
@XmlType(name = "XmlCommandLineToolProfile", namespace = XmlConstants.NAMESPACE)
@XmlAccessorType(XmlAccessType.PROPERTY)
public class XmlCommandLineToolProfile {
    private XmlCommandLineTemplate readerTemplate;
    private XmlCommandLineTemplate modifierTemplate;

    /**
     * Initializes a new instance of the profile.
     */
    public XmlCommandLineToolProfile() {
        readerTemplate = new XmlCommandLineTemplate();
        modifierTemplate = null;
    }

    @XmlElement(name = "reader")
    public final XmlCommandLineTemplate getReaderTemplate() {
        return readerTemplate;
    }

    public final Object readFromChannel(final CommandExecutionChannel channel,
                                        final Map<String, ?> commandFormattingParams) throws IOException, ScriptException {
        return channel.exec(getReaderTemplate(), commandFormattingParams);
    }

    public final boolean writeToChannel(final CommandExecutionChannel channel,
                                        final Object value) throws ScriptException, IOException {
        if (modifierTemplate == null) return false;
        final Object success = channel.exec(modifierTemplate, ImmutableMap.of("value", value));
        return success instanceof Boolean && (Boolean) success;
    }

    public final void setReaderTemplate(final XmlCommandLineTemplate value) {
        readerTemplate = value != null ? value : new XmlCommandLineTemplate();
    }

    @XmlElement(name = "modifier")
    public final XmlCommandLineTemplate getModifierTemplate() {
        return modifierTemplate;
    }

    public final void setModifierTemplate(final XmlCommandLineTemplate value) {
        modifierTemplate = value;
    }

    public final void saveTo(final File destination){
        JAXB.marshal(this, destination);
    }

    public final void saveTo(final OutputStream destination){
        JAXB.marshal(this, destination);
    }

    public final void saveTo(final Writer destination){
        JAXB.marshal(this, destination);
    }

    protected static <T extends XmlCommandLineToolProfile> T loadFrom(final File source, final Class<T> profileType){
        return JAXB.unmarshal(source, profileType);
    }

    protected static <T extends XmlCommandLineToolProfile> T loadFrom(final InputStream source, final Class<T> profileType){
        return JAXB.unmarshal(source, profileType);
    }

    protected static <T extends XmlCommandLineToolProfile> T loadFrom(final Reader source, final Class<T> profileType){
        return JAXB.unmarshal(source, profileType);
    }

    public static XmlCommandLineToolProfile loadFrom(final File source){
        return loadFrom(source, XmlCommandLineToolProfile.class);
    }

    public static XmlCommandLineToolProfile loadFrom(final InputStream source){
        return loadFrom(source, XmlCommandLineToolProfile.class);
    }

    public static XmlCommandLineToolProfile loadFrom(final Reader source){
        return loadFrom(source, XmlCommandLineToolProfile.class);
    }

    public void setScriptManager(final ScriptEngineManager scriptManager) {
        if(readerTemplate != null)
            readerTemplate.setScriptManager(scriptManager);
        if(modifierTemplate != null)
            modifierTemplate.setScriptManager(scriptManager);
    }
}
