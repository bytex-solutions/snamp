package com.itworks.snamp.adapters.ssh;

import com.itworks.snamp.Aggregator;
import org.apache.sshd.common.Session;

import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
interface CommandExecutionContext extends Aggregator {
    static final Class<AdapterController> CONTROLLER = AdapterController.class;
    static final Class<Session> SESSION = Session.class;
    static final Class<ExecutorService> EXECUTOR = ExecutorService.class;
    static final Class<Logger> LOGGER = Logger.class;
}
