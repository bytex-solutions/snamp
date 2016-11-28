package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.configuration.ConfigurationManager;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.google.common.collect.ImmutableMap;

import javax.management.Notification;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Represents builder for {@link GroovyEvent}.
 * @author Roman Sakno
 * @since 2.0
 * @version 2.0
 */
public final class GroovyEventBuilder {
    private String description;
    private String name;
    private Map<String, String> parameters;
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

    public void description(final String value){
        description = Objects.requireNonNull(value);
    }

    EventConfiguration createConfiguration() {
        final EventConfiguration configuration = ConfigurationManager.createEntityConfiguration(getClass().getClassLoader(), EventConfiguration.class);
        assert configuration != null;
        configuration.setParameters(parameters);
        if (!isNullOrEmpty(name))
            configuration.setAlternativeName(name);
        configuration.setAutomaticallyAdded(true);
        if (!isNullOrEmpty(description))
            configuration.setDescription(description);
        return configuration;
    }

    GroovyEvent build(final String name, final NotificationDescriptor descriptor){
        return new GroovyEvent(name, notificationType, isNullOrEmpty(description) ? "Groovy notification" : description, descriptor);
    }

    GroovyEvent build(){
        return build(name, new NotificationDescriptor(parameters));
    }
}
