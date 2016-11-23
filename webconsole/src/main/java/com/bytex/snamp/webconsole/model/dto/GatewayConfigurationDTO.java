package com.bytex.snamp.webconsole.model.dto;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

/**
 * GatewayConfigurationDTO
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public class GatewayConfigurationDTO extends AbstractDTOEntity {

    private String type;

    GatewayConfigurationDTO() {};

    GatewayConfigurationDTO(final Map<String, String> parameters) {
        super(parameters);
    }

    /**
     * Gets type of the entity.
     * @return Type of the entity.
     */
    @JsonProperty
    public String getType() {
        return this.type;
    }

    /**
     * Sets type of the entity.
     * @param value Type of the entity.
     */
    public void setType(final String value) {
        this.type = value;
    }
}
