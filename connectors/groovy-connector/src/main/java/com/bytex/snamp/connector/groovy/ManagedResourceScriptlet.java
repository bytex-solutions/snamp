package com.bytex.snamp.connector.groovy;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.WeakEventListenerList;
import com.bytex.snamp.configuration.AttributeConfiguration;
import com.bytex.snamp.configuration.EventConfiguration;
import com.bytex.snamp.configuration.FeatureConfiguration;
import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.connector.notifications.NotificationBuilder;
import com.bytex.snamp.connector.notifications.NotificationDescriptor;
import com.bytex.snamp.connector.operations.OperationDescriptor;
import com.bytex.snamp.internal.AbstractKeyedObjects;
import com.bytex.snamp.internal.KeyedObjects;
import com.bytex.snamp.jmx.JMExceptionUtils;
import com.bytex.snamp.scripting.groovy.Scriptlet;
import com.bytex.snamp.scripting.groovy.TypeDeclarationDSL;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;

import javax.management.*;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents an abstract class for initialization script.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
public abstract class ManagedResourceScriptlet extends Scriptlet implements ManagedResourceInfo, TypeDeclarationDSL {
    private static final String RESOURCE_NAME_PROPERTY = "resourceName";
    private static final String IS_DISCOVERY_PROPERTY = "discovery";
    private final KeyedObjects<String, GroovyAttributeBuilder> attributes;
    private final KeyedObjects<String, GroovyEventBuilder> events;
    private final KeyedObjects<String, GroovyOperationBuilder> operations;
    private final WeakEventListenerList<NotificationListener, Notification> listeners;

    protected ManagedResourceScriptlet(){
        setProperty(IS_DISCOVERY_PROPERTY, false);
        attributes = AbstractKeyedObjects.create(GroovyAttributeBuilder::name);
        events = AbstractKeyedObjects.create(GroovyEventBuilder::name);
        operations = AbstractKeyedObjects.create(GroovyOperationBuilder::name);
        listeners = new WeakEventListenerList<>((l, n) -> l.handleNotification(n, this));
    }

    final void addEventListener(final NotificationListener listener){
        listeners.add(listener);
    }

    final void removeEventListener(final NotificationListener listener){
        listeners.remove(listener);
    }

    final GroovyAttribute createAttribute(final String name, final AttributeDescriptor descriptor) throws AttributeNotFoundException {
        final GroovyAttributeBuilder builder = attributes.get(descriptor.getAlternativeName().orElse(name));
        if(builder == null)
            throw JMExceptionUtils.attributeNotFound(name);
        else
            return builder.build(name, descriptor);
    }

    final GroovyEvent createEvent(final String name, final NotificationDescriptor descriptor) throws MBeanException{
        final GroovyEventBuilder builder = events.get(descriptor.getAlternativeName().orElse(name));
        if(builder == null)
            throw new MBeanException(new IllegalArgumentException(String.format("Event %s is not supported", name)));
        else
            return builder.build(name, descriptor);
    }

    final GroovyOperation createOperation(final String name, final OperationDescriptor descriptor) throws OperationsException {
        final GroovyOperationBuilder builder = operations.get(descriptor.getAlternativeName().orElse(name));
        if(builder == null)
            throw JMExceptionUtils.operationNotFound(name);
        else
            return builder.build(name, descriptor);
    }

    /**
     * Declares a new attribute.
     * @param statement Attribute declaration.
     */
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final void attribute(@DelegatesTo(GroovyAttributeBuilder.class) final Closure<?> statement){
        final GroovyAttributeBuilder builder = invokeDslStatement(statement, GroovyAttributeBuilder::new);
        attributes.put(builder);
    }

    /**
     * Declares a new event.
     * @param statement Event declaration.
     */
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final void event(@DelegatesTo(GroovyEventBuilder.class) final Closure<?> statement){
        final GroovyEventBuilder builder = invokeDslStatement(statement, GroovyEventBuilder::new);
        events.put(builder);
    }

    /**
     * Declares a new operation.
     * @param statement Operation declaration.
     */
    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final void operation(@DelegatesTo(GroovyOperationBuilder.class) final Closure<?> statement){
        final GroovyOperationBuilder builder = invokeDslStatement(statement, GroovyOperationBuilder::new);
        operations.put(builder);
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final void emit(@DelegatesTo(NotificationBuilder.class) final Closure<?> statement) {
        final NotificationBuilder builder = invokeDslStatement(statement, NotificationBuilder::new);
        emit(builder.setSource(this).get());
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final void emit(final Notification notification){
        listeners.fire(notification);
    }

    @SpecialUse(SpecialUse.Case.SCRIPTING)
    protected final void emit(final String type, final String message){
        emit(new Notification(type, this, 0L, message));
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T extends FeatureConfiguration> Collection<T> getEntities(final Class<T> entityType) {
        if(AttributeConfiguration.class.isAssignableFrom(entityType))
            return (Collection<T>) attributes.values().stream().map(GroovyAttributeBuilder::createConfiguration).collect(Collectors.toList());
        else if(EventConfiguration.class.isAssignableFrom(entityType))
            return (Collection<T>) events.values().stream().map(GroovyEventBuilder::createConfiguration).collect(Collectors.toList());
        else return null;
    }

    final Set<String> getAttributes(){
        return attributes.keySet();
    }

    final Set<String> getEvents(){
        return events.keySet();
    }

    final Set<String> getOperations(){
        return operations.keySet();
    }

    /**
     * Releases all resources associated with this script.
     * @throws Exception Unable to release resources associated with this script.
     */
    @Override
    public void close() throws Exception {
        attributes.clear();
        events.clear();
        listeners.clear();
    }

    final void setResourceName(final String value){
        setProperty(RESOURCE_NAME_PROPERTY, value);
    }

    final void setDiscovery(final boolean value){
        setProperty(IS_DISCOVERY_PROPERTY, value);
    }
}
