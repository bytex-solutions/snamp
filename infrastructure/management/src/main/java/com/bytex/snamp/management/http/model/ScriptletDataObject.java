package com.bytex.snamp.management.http.model;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ScriptletConfiguration;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class ScriptletDataObject implements ScriptletConfiguration, Exportable<ScriptletConfiguration> {
    private String language;
    private String script;
    private boolean isURL;
    private final Map<String, String> parameters;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public ScriptletDataObject() {
        language = script = "";
        parameters = new HashMap<>();
    }

    public ScriptletDataObject(final ScriptletConfiguration configuration){
        language = configuration.getLanguage();
        script = configuration.getScript();
        isURL = configuration.isURL();
        parameters = new HashMap<>(configuration.getParameters());
    }

    /**
     * Gets scriptlet context parameters.
     *
     * @return Context parameters.
     */
    @Override
    @JsonProperty
    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(@Nonnull final Map<String, String> value){
        parameters.clear();
        parameters.putAll(value);
    }

    @Override
    public void exportTo(@Nonnull final ScriptletConfiguration output) {
        output.setScript(script);
        output.setLanguage(language);
        output.setURL(isURL);
        output.getParameters().clear();
        output.getParameters().putAll(parameters);
    }

    /**
     * Gets scripting language.
     *
     * @return Scripting language name.
     */
    @Override
    @JsonProperty("language")
    public String getLanguage() {
        return language;
    }

    /**
     * Sets scripting language.
     *
     * @param value Scripting language name.
     */
    @Override
    public void setLanguage(final String value) {
        language = Objects.requireNonNull(value);
    }

    /**
     * Gets script body.
     *
     * @return Gets script body.
     */
    @Override
    @JsonProperty("script")
    public String getScript() {
        return script;
    }

    /**
     * Sets script body.
     *
     * @param value Script body.
     */
    @Override
    public void setScript(final String value) {
        script = Objects.requireNonNull(value);
    }

    /**
     * Determines whether the script body is just a URL-based location of script file.
     *
     * @return {@literal true} if {@link #getScript()} returns URL to the script body.
     */
    @Override
    @JsonProperty("url")
    public boolean isURL() {
        return isURL;
    }

    /**
     * Defines that the script body is just a URL-based reference to the location of script file
     *
     * @param value {@literal true} if {@link #getScript()} returns URL to the script body.
     */
    @Override
    public void setURL(final boolean value) {
        isURL = value;
    }
}
