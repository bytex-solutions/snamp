package com.bytex.snamp.webconsole.model.dto;

import com.bytex.snamp.configuration.FeatureConfiguration;
import org.codehaus.jackson.map.annotate.JsonSerialize;

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
@JsonSerialize(include=JsonSerialize.Inclusion.NON_EMPTY)
public abstract class AbstractDTOEntity implements FeatureConfiguration {
    private Map<String, String> parameters = new HashMap<>();

    AbstractDTOEntity() {}

    AbstractDTOEntity(final Map<String, String> map) {
        this.parameters = new HashMap<>(map);
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters == null ? Collections.EMPTY_MAP : parameters;
    }
}