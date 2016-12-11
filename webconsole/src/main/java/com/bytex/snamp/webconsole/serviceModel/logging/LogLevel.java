package com.bytex.snamp.webconsole.serviceModel.logging;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonDeserialize(using = LogLevelDeserializer.class)
@JsonSerialize(using = LogLevelSerializer.class)
public enum LogLevel {
    DEBUG(LogService.LOG_DEBUG),
    INFO(LogService.LOG_INFO),
    WARNING(LogService.LOG_WARNING),
    ERROR(LogService.LOG_ERROR);

    private final int level;

    LogLevel(final int level){
        this.level = level;
    }

    final boolean shouldBeLogged(final LogEntry entry){
        return entry.getLevel() >= level;
    }

    static LogLevel of(final LogEntry entry){
        for(final LogLevel level: values())
            if(level.level == entry.getLevel()) return level;
        return DEBUG;
    }

    @Override
    public final String toString() {
        return name().toLowerCase();
    }
}
