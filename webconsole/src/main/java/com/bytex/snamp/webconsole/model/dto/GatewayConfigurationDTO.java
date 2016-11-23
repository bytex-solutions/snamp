package com.bytex.snamp.webconsole.model.dto;

import java.util.Map;

/**
 * GatewayConfigurationDTO
 *
 * @author Evgeniy Kirichenko
 * @version 2.0
 * @since 2.0
 */
public class GatewayConfigurationDTO extends AbstractDTOEntity {

    GatewayConfigurationDTO() {};

    GatewayConfigurationDTO(final Map<String, String> parameters) {
        super(parameters);
    }
}
