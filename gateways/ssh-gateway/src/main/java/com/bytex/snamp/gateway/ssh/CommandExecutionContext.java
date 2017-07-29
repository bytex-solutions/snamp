package com.bytex.snamp.gateway.ssh;

import com.bytex.snamp.Aggregator;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
interface CommandExecutionContext extends Aggregator {
    Class<GatewayController> CONTROLLER = GatewayController.class;
    Class<ExecutorService> EXECUTOR = ExecutorService.class;
    Class<InputStream> INPUT_STREAM = InputStream.class;
}
