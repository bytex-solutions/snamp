package com.bytex.snamp.web.serviceModel.logging;

import com.bytex.snamp.web.serviceModel.WebConsoleService;
import org.ops4j.pax.logging.spi.PaxAppender;

/**
 * Represents interface for log appender.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public interface WebConsoleLogService extends WebConsoleService, PaxAppender {
}
