package com.bytex.snamp.web.serviceModel;

import com.bytex.snamp.json.JsonUtils;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Provides access to the singleton instance of {@link ObjectMapper}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class ObjectMapperSingleton extends ObjectMapper {
    public static final ObjectMapper INSTANCE = new ObjectMapperSingleton();

    private ObjectMapperSingleton(){
        registerModule(new JsonUtils());
    }
}
