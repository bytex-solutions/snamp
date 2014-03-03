package com.snamp.connectors;

import com.ibm.mq.MQMessage;
import com.snamp.connectors.util.NotificationBuilder;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

import java.io.IOException;
import java.util.logging.*;

import static com.snamp.connectors.NotificationSupport.Notification;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NotificationParser {
    private static final String MESSAGE_BINDING_PARAM = "mqmessage";
    private static final String NOTIF_BUILDER_BINDING_PARAM = "notification";

    private long sequenceNumber;
    private final GroovyScriptEngine scriptEngine;
    private final String scriptFile;
    private final Logger log;

    public NotificationParser(final String scriptFile, final String... dirs) throws IOException {
        sequenceNumber = 0;
        scriptEngine = new GroovyScriptEngine(dirs);
        this.scriptFile = scriptFile;
        this.log = IbmWmqHelpers.getLogger();
    }

    public final synchronized Notification createNotification(final MQMessage message){
        final Binding binding = new Binding();
        final NotificationBuilder builder = new NotificationBuilder(true);
        builder.setSequenceNumber(sequenceNumber);
        binding.setProperty(MESSAGE_BINDING_PARAM, message);
        binding.setProperty(NOTIF_BUILDER_BINDING_PARAM, builder);
        try {
            final Object result = scriptEngine.run(scriptFile, binding);
            if(result instanceof Notification){
                sequenceNumber = builder.getSequenceNumber();
                return (Notification)result;
            }
            else return null;
        }
        catch (final Exception e) {
            log.log(Level.SEVERE, String.format("Unable to evaluate script %s", scriptFile), e);
            return null;
        }
    }
}
