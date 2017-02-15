package com.bytex.snamp.moa.watching;

import javax.management.Attribute;
import java.util.EnumMap;
import java.util.Objects;

/**
 * Represents configuration of the watch dog for attribute.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class AttributeWatcherSettings extends EnumMap<AttributeState, Condition> {
    private static final long serialVersionUID = 6016909747542182104L;
    private final String componentName;
    private final String attributeName;

    public AttributeWatcherSettings(final String targetComponent, final String targetAttribute) {
        super(AttributeState.class);
        componentName = Objects.requireNonNull(targetComponent);
        attributeName = Objects.requireNonNull(targetAttribute);
        for (final AttributeState state : AttributeState.ALL_STATES)
            put(state, Condition.FALSE);
    }

    public AttributeState detectState(final Attribute attribute) {
        for (final Entry<AttributeState, Condition> entry : entrySet())
            if (entry.getValue().test(attribute))
                return entry.getKey();
        return AttributeState.ALL_STATES.last();
    }

    public String getTargetComponent(){
        return componentName;
    }

    public String getTargetAttribute() {
        return attributeName;
    }
}
