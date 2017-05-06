package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.Stateful;
import com.bytex.snamp.configuration.ScriptletConfiguration;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

/**
 * Represents serializable scriptlet configuration.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
class SerializableScriptletConfiguration implements ScriptletConfiguration, Externalizable, Modifiable, Stateful {
    private static final long serialVersionUID = 7658424337366645929L;
    private String language;
    private String script;
    private boolean isURL;
    private transient boolean modified;
    private final ParametersMap parameters;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SerializableScriptletConfiguration() {
        language = script = "";
        parameters = new ParametersMap();
    }

    final void markAsModified(){
        modified = true;
    }

    @Override
    public final ParametersMap getParameters() {
        return parameters;
    }

    @Override
    public final String getLanguage() {
        return language;
    }

    @Override
    public final void setLanguage(final String value) {
        language = Objects.requireNonNull(value);
        markAsModified();
    }

    @Override
    public final String getScript() {
        return script;
    }

    @Override
    public final void setScript(final String value) {
        script = Objects.requireNonNull(value);
        markAsModified();
    }

    @Override
    public final boolean isURL() {
        return isURL;
    }

    @Override
    public final void setURL(final boolean value) {
        isURL = value;
        markAsModified();
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(language);
        out.writeUTF(script);
        out.writeBoolean(isURL);
        parameters.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        language = in.readUTF();
        script = in.readUTF();
        isURL = in.readBoolean();
        parameters.readExternal(in);
    }

    final void load(final ScriptletConfiguration other){
        setLanguage(other.getLanguage());
        setScript(other.getScript());
        setURL(other.isURL());
        parameters.clear();
        parameters.putAll(other.getParameters());
    }

    @Override
    public final boolean isModified() {
        return modified || parameters.isModified();
    }

    @Override
    public final void reset() {
        modified = false;
        parameters.reset();
    }

    @Override
    public int hashCode() {
        return Objects.hash(language, script, isURL, parameters);
    }

    private boolean equals(final ScriptletConfiguration other) {
        return other.getScript().equals(script) &&
                other.getLanguage().equals(language) &&
                other.isURL() == isURL &&
                other.getParameters().equals(parameters);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ScriptletConfiguration && equals((ScriptletConfiguration) other);
    }
}
