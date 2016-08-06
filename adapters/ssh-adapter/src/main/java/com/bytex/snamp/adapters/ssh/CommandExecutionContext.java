package com.bytex.snamp.adapters.ssh;

import com.bytex.snamp.Aggregator;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;

/**
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
interface CommandExecutionContext extends Aggregator {
    Class<AdapterController> CONTROLLER = AdapterController.class;
    Class<ExecutorService> EXECUTOR = ExecutorService.class;
    Class<InputStream> INPUT_STREAM = InputStream.class;
}
