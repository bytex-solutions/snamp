package com.itworks.jcommands.impl;

import javax.xml.bind.annotation.XmlTransient;
import java.util.EnumSet;
import java.util.List;

/**
 * Represents parsing rule. This class cannot be inherited directly from your code.
 * @author Roman Sakno
 * @since 1.0
 * @version 1.0
 */
@XmlTransient
public abstract class ParsingRule {
    private EnumSet<XmlParsingResultType> related;

    ParsingRule() {
        this(EnumSet.allOf(XmlParsingResultType.class));
    }

    ParsingRule(final XmlParsingResultType first,
                final XmlParsingResultType... other){
        this(EnumSet.of(first, other));
    }

    ParsingRule(final EnumSet<XmlParsingResultType> related){
        this.related = related;
    }

    /**
     * Determines whether this parsing rule can be used to describe parsing
     * of the specified type.
     * @param type The type to check.
     * @return {@literal true}, if this parsing rule can be used with the specified
     * return type; otherwise, {@literal false}.
     */
    public final boolean compatibleWith(final XmlParsingResultType type){
        return related.contains(type);
    }

    static <R extends ParsingRule> R findRule(final List parsingTemplate, final Class<R> ruleType){
        for(final Object template: parsingTemplate)
            if(ruleType.isInstance(template))
                return ruleType.cast(template);
        return null;
    }
}
