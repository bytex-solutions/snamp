package com.itworks.snamp.adapters.ssh;

import com.google.common.reflect.TypeToken;
import com.itworks.snamp.adapters.AbstractResourceAdapter;

import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SshHelpers {
    static final TypeToken<Map<String, Object>> STRING_MAP_TYPE = new TypeToken<Map<String, Object>>() {};
    static final String ADAPTER_NAME = "ssh";


    private SshHelpers(){

    }

    static Logger getLogger(){
        return AbstractResourceAdapter.getLogger(ADAPTER_NAME);
    }
}
