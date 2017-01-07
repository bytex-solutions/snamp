package com.bytex.snamp.web.serviceModel.logging;

import com.bytex.snamp.connector.notifications.Severity;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.ops4j.pax.logging.spi.PaxLevel;

import java.util.Objects;

/**
 * Represents logging settings.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("loggingSettings")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public final class LoggingSettings {
    private Severity level;

    public LoggingSettings(){
        level = Severity.ERROR;
    }

    @JsonProperty("logLevel")
    @JsonSerialize(using = SeveritySerializer.class)
    @JsonDeserialize(using = SeverityDeserializer.class)
    public Severity getLogLevel(){
        return level;
    }

    public void setLogLevel(final Severity value){
        level = Objects.requireNonNull(value);
    }

    boolean shouldBeLogged(final PaxLevel level) {
        return level.getSyslogEquivalent() <= this.level.getLevel();
    }
}
