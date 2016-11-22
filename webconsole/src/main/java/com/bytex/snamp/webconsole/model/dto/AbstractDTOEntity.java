package com.bytex.snamp.webconsole.model.dto;

import com.bytex.snamp.configuration.FeatureConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * AbstractDTOEntity
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public abstract class AbstractDTOEntity implements FeatureConfiguration {
    private Map<String, String> parameters = new HashMap<>();

    AbstractDTOEntity() {}

    AbstractDTOEntity(final Map<String, String> map) {
        this.parameters = map;
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters == null ? Collections.EMPTY_MAP : parameters;
    }
}