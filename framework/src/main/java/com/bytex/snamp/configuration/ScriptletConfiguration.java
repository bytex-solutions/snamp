package com.bytex.snamp.configuration;

import com.bytex.snamp.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Represents scriptlet.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ScriptletConfiguration {
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

    default String resolveScriptBody() throws IOException {
        final String result;
        if (isURL()) {
            final URL url = new URL(getScript());
            final URLConnection connection = url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            try (final InputStream is = connection.getInputStream()) {
                return IOUtils.toString(is);
            }
        } else
            result = getScript();
        return result;
    }
}