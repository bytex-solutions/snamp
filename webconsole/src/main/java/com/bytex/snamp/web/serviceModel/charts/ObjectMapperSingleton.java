package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.json.JsonUtils;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class ObjectMapperSingleton extends ObjectMapper {
    static final ObjectMapper INSTANCE = new ObjectMapperSingleton();

    private ObjectMapperSingleton(){
        registerModule(new JsonUtils());
    }
}
