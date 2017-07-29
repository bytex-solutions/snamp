package com.bytex.snamp.connector.attributes.checkers;

import com.bytex.snamp.configuration.ScriptletConfiguration;
import com.bytex.snamp.configuration.scriptlet.ScriptletConfigurationSupport;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

import javax.annotation.Nonnull;
import javax.management.Attribute;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Represents simple attribute checker based on
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class ColoredAttributeChecker implements AttributeChecker, ScriptletConfigurationSupport {
    public static final String LANGUAGE_NAME = "ColoredAttributeChecker";
    private ColoredAttributePredicate green;
    private ColoredAttributePredicate yellow;

    public ColoredAttributeChecker(){
        green = new ConstantAttributePredicate(true);
        yellow = new ConstantAttributePredicate(true);
    }

    public static ColoredAttributeChecker parse(final String scriptBody) throws IOException {
        return parse(scriptBody, new ObjectMapper());
    }

    public static ColoredAttributeChecker parse(final String scriptBody, final ObjectMapper mapper) throws IOException {
        return mapper.readValue(scriptBody, ColoredAttributeChecker.class);
    }

    @Override
    public void configureScriptlet(final ScriptletConfiguration scriptlet) {
        final ObjectMapper mapper = new ObjectMapper();
        scriptlet.setLanguage(LANGUAGE_NAME);
        scriptlet.setURL(false);
        final String json;
        try {
            json = mapper.writeValueAsString(this);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
        scriptlet.setScript(json);
    }

    @JsonProperty("green")
    public ColoredAttributePredicate getGreenPredicate(){
        return green;
    }

    public void setGreenPredicate(@Nonnull final ColoredAttributePredicate value){
        green = value;
    }

    @JsonProperty("yellow")
    public ColoredAttributePredicate getYellowPredicate(){
        return yellow;
    }

    public void setYellowPredicate(@Nonnull final ColoredAttributePredicate value){
        yellow = value;
    }

    @Override
    public AttributeCheckStatus getStatus(final Attribute attribute) {
        if (green.test(attribute))
            return AttributeCheckStatus.OK;
        else if (yellow.test(attribute))
            return AttributeCheckStatus.SUSPICIOUS;
        else
            return AttributeCheckStatus.MALFUNCTION;
    }
}
