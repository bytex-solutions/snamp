package com.bytex.snamp.web.serviceModel.logging;

import com.bytex.snamp.ArrayUtils;
import com.bytex.snamp.connector.notifications.Severity;
import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.bytex.snamp.web.serviceModel.WebEvent;
import com.google.common.collect.ImmutableMap;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogService;

import java.time.Instant;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@JsonTypeName("log")
public final class LogEvent extends WebEvent {
    private static final long serialVersionUID = -9157337771308084521L;
    private final String message;
    private final Severity severity;
    private final Instant timeStamp;
    private final String[] stackTrace;
    private final ImmutableMap additionalInfo;

    LogEvent(final WebConsoleService source, final LogEntry entry) {
        super(source);
        message = entry.getMessage();
        switch (entry.getLevel()) {
            case LogService.LOG_DEBUG:
                severity = Severity.DEBUG;
                break;
            case LogService.LOG_ERROR:
                severity = Severity.ERROR;
                break;
            case LogService.LOG_INFO:
                severity = Severity.INFO;
                break;
            case LogService.LOG_WARNING:
                severity = Severity.WARNING;
                break;
            default:
                severity = Severity.UNKNOWN;
        }
        timeStamp = Instant.ofEpochMilli(entry.getTime());
        stackTrace = ArrayUtils.emptyArray(String[].class);
        additionalInfo = ImmutableMap.of();
    }

    LogEvent(final WebConsoleService source, final PaxLoggingEvent event) {
        super(source);
        message = event.getMessage();
        timeStamp = Instant.ofEpochMilli(event.getTimeStamp());
        severity = Severity.resolve(event.getLevel().getSyslogEquivalent());
        stackTrace = nullToEmpty(event.getThrowableStrRep());
        additionalInfo = nullToEmpty(event.getProperties());
    }

    private static ImmutableMap nullToEmpty(final Map value){
        return value == null ? ImmutableMap.of() : ImmutableMap.copyOf(value);
    }

    private static String[] nullToEmpty(final String[] array){
        return array == null ? ArrayUtils.emptyArray(String[].class) : array;
    }

    @JsonProperty("message")
    public String getMessage(){
        return message;
    }

    @JsonProperty("level")
    @JsonSerialize(using = SeveritySerializer.class)
    public Severity getLevel(){
        return severity;
    }

    @JsonProperty("timeStamp")
    @JsonSerialize(using = InstantSerializer.class)
    public Instant getTimeStamp(){
        return timeStamp;
    }

    @JsonProperty("stackTrace")
    public String[] getStackTrace(){
        return stackTrace;
    }

    @JsonProperty("details")
    public Map getDetails(){
        return additionalInfo;
    }
}
