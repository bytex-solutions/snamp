package com.snamp.restmanager;

import com.snamp.hosting.management.AgentManagerBase;

/**
 * User: agrishin85
 * Date: 11/3/13
 * Time: 7:11 PM
 * Rest Manager plugin
 */
final class RestAdapterManager extends AgentManagerBase {

    public static final String MANAGER_NAME = "rest";

    public RestAdapterManager(){
           super(MANAGER_NAME);
    }

    @Override
    public void close() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
