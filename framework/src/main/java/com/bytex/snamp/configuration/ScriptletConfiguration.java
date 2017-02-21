package com.bytex.snamp.configuration;

/**
 * Represents scriptlet.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface ScriptletConfiguration {
    /**
     * Gets scripting language.
     * @return Scripting language name.
     */
    String getLanguage();

    /**
     * Sets scripting language.
     * @param language Scripting language name.
     */
    void setLanguage(final String language);

    /**
     * Gets script body.
     * @return Gets script body.
     */
    String getScript();

    /**
     * Sets script body.
     * @param script Script body.
     */
    void setScript(final String script);

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
}
