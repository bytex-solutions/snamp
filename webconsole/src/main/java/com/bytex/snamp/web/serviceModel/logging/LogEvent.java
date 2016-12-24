package com.bytex.snamp.web.serviceModel.logging;

import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.bytex.snamp.web.serviceModel.WebEvent;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.osgi.service.log.LogEntry;

import java.time.Instant;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@JsonTypeName("log")
public final class LogEvent extends WebEvent {
    private static final long serialVersionUID = -9157337771308084521L;
    private final String message;
    private final LogLevel level;
    private final Instant timeStamp;

    LogEvent(final WebConsoleService source, final LogEntry entry) {
        super(source);
        message = entry.getMessage();
        level = LogLevel.of(entry);
        timeStamp = Instant.ofEpochMilli(entry.getTime());
    }

    @JsonProperty("message")
    public String getMessage(){
        return message;
    }

    @JsonProperty("level")
    public LogLevel getLevel(){
        return level;
    }

    @JsonProperty("timeStamp")
    public Instant getTimeStamp(){
        return timeStamp;
    }
}
