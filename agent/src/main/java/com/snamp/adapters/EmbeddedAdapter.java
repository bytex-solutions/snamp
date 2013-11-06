package com.snamp.adapters;

import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import static com.snamp.hosting.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author roman
 */
public class EmbeddedAdapter extends AbstractAdapter {
    private final AbstractAttributesRegistry attributes;

    /**
     * Initializes a new instance of the adapter.
     *
     * @param adapterName
     */
    public EmbeddedAdapter(final String adapterName) {
        super(adapterName);
        attributes = new AbstractAttributesRegistry() {
            @Override
            protected ConnectedAttributes createBinding(final ManagementConnector connector) {
                return new ConnectedAttributes(connector) {
                    @Override
                    public String makeAttributeId(final String prefix, final String postfix) {
                        return String.format("%s/%s", prefix, postfix);
                    }
                };
            }
        };
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
        this.attributes.putAll(connector, namespace, attributes);
    }

    protected final <T> T getAttribute(final String namespace, final String id, final Class<T> attributeType, final T defaultValue){
        return this.attributes.getAttribute(namespace, id, attributeType, defaultValue, TimeSpan.INFINITE);
    }

    protected final boolean setAttribute(final String namespace, final String id, final Object value){
        return this.attributes.setAttribute(namespace, id, value, TimeSpan.INFINITE);
    }



    @Override
    public void close() {
        attributes.clear();
    }

    private static final boolean isVoid(final Class<?> t){
        return t == null || Objects.equals(void.class, t) || Objects.equals(Void.class, t);
    }
}
