package com.bytex.snamp.configuration;

import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

/**
 * Represents scriptlet.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ScriptletConfiguration {
    ScriptletConfiguration EMPTY = new ScriptletConfiguration() {
        @Override
        public String getLanguage() {
            return "";
        }

        @Override
        public void setLanguage(final String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getScript() {
            return "";
        }

        @Override
        public void setScript(final String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isURL() {
            return false;
        }

        @Override
        public void setURL(final boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ImmutableMap<String, String> getParameters() {
            return ImmutableMap.of();
        }
    };
    
    String GROOVY_LANGUAGE = "Groovy";
    String JS_LANGUAGE = "JavaScript";

    /**
     * Gets scripting language.
     * @return Scripting language name.
     */
    String getLanguage();

    /**
     * Sets scripting language.
     * @param value Scripting language name.
     */
    void setLanguage(final String value);

    /**
     * Gets script body.
     * @return Gets script body.
     */
    String getScript();

    /**
     * Sets script body.
     * @param value Script body.
     */
    void setScript(final String value);

    /**
     * Determines whether the script body is just a URL-based location of script file.
     * @return {@literal true} if {@link #getScript()} returns URL to the script body.
     */
    boolean isURL();

    /**
     * Defines that the script body is just a URL-based reference to the location of script file
     * @param value {@literal true} if {@link #getScript()} returns URL to the script body.
     */
    void setURL(final boolean value);

    /**
     * Gets scriptlet context parameters.
     * @return Context parameters.
     */
    Map<String, String> getParameters();

    default String resolveScriptBody() throws IOException {
        final String result;
        if (isURL()) {
            try (final InputStream is = new URL(getScript()).openStream()) {
                return IOUtils.toString(is);
            }
        } else
            result = getScript();
        return result;
    }

    static void fillByDefault(final ScriptletConfiguration scriptlet){
        scriptlet.setScript("");
        scriptlet.setURL(false);
        scriptlet.setLanguage("");
        scriptlet.getParameters().clear();
    }
}