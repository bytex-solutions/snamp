package com.bytex.snamp.configuration.impl;

import java.util.Map;

/**
 * Represents abstract class for all serializable configuration entities.
 */
abstract class AbstractEntityConfiguration extends ParametersMap implements SerializableEntityConfiguration {
    private static final long serialVersionUID = -8455277079119895844L;

    @Override
    public abstract void load(final Map<String, String> parameters);
}
