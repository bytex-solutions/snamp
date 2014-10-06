package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.adapters.AbstractResourceAdapter;

import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SshHelpers {
    static final String ADAPTER_NAME = "ssh";

    private SshHelpers(){

    }

    public static Logger getLogger(){
        return AbstractResourceAdapter.getLogger(ADAPTER_NAME);
    }
}
