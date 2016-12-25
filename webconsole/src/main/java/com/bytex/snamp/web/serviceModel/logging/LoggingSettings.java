package com.bytex.snamp.web.serviceModel.logging;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.Objects;

/**
 * Represents logging settings.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("loggingSettings")
public final class LoggingSettings {
    private LogLevel level;

    public LoggingSettings(){
        level = LogLevel.ERROR;
    }

    @JsonProperty("logLevel")
    public LogLevel getLogLevel(){
        return level;
    }

    public void setLogLevel(final LogLevel value){
        level = Objects.requireNonNull(value);
    }
}
