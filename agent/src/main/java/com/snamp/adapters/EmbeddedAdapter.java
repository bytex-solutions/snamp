package com.snamp.adapters;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;

import java.beans.*;
import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * @author roman
 */
public class EmbeddedAdapter extends AdapterBase {

    private static final class ExposedAttributes extends HashMap<String, AttributeMetadata>{
        private final ManagementConnector connector;

        public ExposedAttributes(final ManagementConnector connector){
            this.connector = connector;
        }

        public final <T> T getValue(final String namespace, final String attributeId, final Class<T> returnType, final T defaultValue) throws TimeoutException {
            final Object value = connector.getAttribute(makeAttributeID(namespace, attributeId), TimeSpan.INFINITE, defaultValue);
            return get(attributeId).getAttributeType().convertTo(value, returnType);
        }

        public final boolean setValue(final String namespace, final String attributeId, final Object value) throws TimeoutException {
            return connector.setAttribute(makeAttributeID(namespace, attributeId), TimeSpan.INFINITE, value);
        }

        public static final String makeAttributeID(final String namespace, final String id){
            return String.format("%s/%s", namespace, id);
        }
    }

    private final Map<String, ExposedAttributes> attributes;

    /**
     * Initializes a new instance of the adapter.
     *
     * @param adapterName
     */
    public EmbeddedAdapter(final String adapterName) {
        super(adapterName);
        attributes = new HashMap<>(10);
    }

    /**
     * Exposes the connector to the world.
     *
     * @param parameters The adapter startup parameters.
     * @return {@literal true}, if adapter is started successfully; otherwise, {@literal false}.
     */
    @Override
    public final boolean start(final Map<String, String> parameters) throws IOException {
        return true;
    }

    /**
     * Stops the connector hosting.
     *
     * @param saveAttributes {@literal true} to save previously exposed attributes for reuse; otherwise,
     *                       clear internal list of exposed attributes.
     * @return {@literal true}, if adapter is previously started; otherwise, {@literal false}.
     */
    @Override
    public final boolean stop(final boolean saveAttributes) {
        return true;
    }



    /**
     * Exposes management attributes.
     *
     * @param connector  Management connector that provides access to the specified attributes.
     * @param namespace  The attributes namespace.
     * @param attributes The dictionary of attributes.
     */
    @Override
    public final void exposeAttributes(final ManagementConnector connector, final String namespace, final Map<String, AttributeConfiguration> attributes) {
        final ExposedAttributes exposedAttrs;
        this.attributes.put(namespace, exposedAttrs = new ExposedAttributes(connector));
        for(final String attributeId: attributes.keySet()){
            final AttributeConfiguration attributeConfig = attributes.get(attributeId);
            final AttributeMetadata md = connector.connectAttribute(ExposedAttributes.makeAttributeID(namespace, attributeId), attributeConfig.getAttributeName(), attributeConfig.getAdditionalElements());

            if(md != null)
                exposedAttrs.put(attributeId, md);
        }
    }

    protected final <T> T getAttribute(final String namespace, final String id, final Class<T> attributeType, final T defaultValue){
        if(attributes.containsKey(namespace)){
            final ExposedAttributes attrs = attributes.get(namespace);
            if(attrs.containsKey(id))
                try {
                    return attrs.getValue(namespace, id, attributeType, defaultValue);
                }
                catch (final TimeoutException e) {
                    return defaultValue;
                }
        }
        return defaultValue;
    }

    protected final boolean setAttribute(final String namespace, final String id, final Object value){
        if(attributes.containsKey(namespace)){
            final ExposedAttributes attrs = attributes.get(namespace);
            if(attrs.containsKey(id))
                try {
                    return attrs.setValue(namespace, id, value);
                } catch (final TimeoutException e) {
                    return false;
                }
        }
        return false;
    }



    @Override
    public void close() {
        attributes.clear();
    }

    private static final boolean isVoid(final Class<?> t){
        return t == null || Objects.equals(void.class, t) || Objects.equals(Void.class, t);
    }
}
