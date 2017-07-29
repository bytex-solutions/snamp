package com.bytex.snamp.connector.attributes.checkers;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.scripting.groovy.Scriptlet;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import javax.management.Attribute;
import java.util.Optional;
import java.util.function.Function;

/**
 * Represents attribute checker implemented as Groovy script.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public abstract class GroovyAttributeChecker extends Scriptlet implements AttributeChecker {
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    public static final AttributeCheckStatus OK = AttributeCheckStatus.OK;
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    public static final AttributeCheckStatus SUSPICIOUS = AttributeCheckStatus.SUSPICIOUS;
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    public static final AttributeCheckStatus MALFUNCTION = AttributeCheckStatus.MALFUNCTION;
    private final ThreadLocal<Attribute> attributeStorage = new ThreadLocal<>();

    @Override
    public final AttributeCheckStatus getStatus(final Attribute attribute) {
        attributeStorage.set(attribute);
        try {
            return DefaultGroovyMethods.asType(run(), AttributeCheckStatus.class);
        } finally {
            attributeStorage.remove();
        }
    }

    private <O> Optional<O> getAttribute(final Function<? super Attribute, ? extends O> transformation){
        return Optional.ofNullable(attributeStorage.get()).map(transformation);
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final Object getAttributeValue(){
        return getAttribute(Attribute::getValue).orElse(null);
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final String getAttributeName() {
        return getAttribute(Attribute::getName).orElse("");
    }
}
