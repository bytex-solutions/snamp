package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.google.common.collect.ImmutableMap;

import javax.management.Notification;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents builder for {@link GroovyEvent}.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public final class GroovyEventBuilder extends GroovyFeatureBuilder<EventConfiguration> {
    private String name;
    private Class<? extends Notification> notificationType;

    GroovyEventBuilder(){
        parameters = ImmutableMap.of();
        notificationType = Notification.class;
    }

    String name(){
        return name;
    }

    public void type(final Class<? extends Notification> value){
        notificationType = Objects.requireNonNull(value);
    }

    public void name(final String value){
        name = Objects.requireNonNull(value);
    }

    @Override
    EventConfiguration createConfiguration() {
        final EventConfiguration configuration = createConfiguration(EventConfiguration.class);
        if (!isNullOrEmpty(name))
            configuration.setAlternativeName(name);
        return configuration;
    }

    GroovyEvent build(final String name, final NotificationDescriptor descriptor){
        return new GroovyEvent(name, notificationType, isNullOrEmpty(description) ? "Groovy notification" : description, descriptor);
    }

    GroovyEvent build(){
        return build(name, new NotificationDescriptor(parameters));
    }
}
