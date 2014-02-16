package com.snamp.adapters;

import static com.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.AttributeConfiguration;
import static com.snamp.configuration.AgentConfiguration.ManagementTargetConfiguration.EventConfiguration;

import static com.snamp.connectors.NotificationSupport.NotificationListener;
import com.snamp.configuration.EmbeddedAgentConfiguration;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import javax.management.AttributeChangeNotification;
import java.math.BigInteger;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@PluginImplementation
public final class TestEmbeddedAdapter extends EmbeddedAdapter {
    public static final String NAME = "test-embedded";

    public TestEmbeddedAdapter(){
        super(NAME);
    }

    public final BigInteger getBigIntProperty(){
        return getAttribute("", "bigint", BigInteger.class, BigInteger.ZERO);
    }

    public final void setBigIntProperty(final BigInteger value){
        setAttribute("", "bigint", value);
    }

    public final Object addPropertyChangedListener(final NotificationListener listener){
        return subscribe("", "attributeChanged", listener);
    }

    public final void removePropertyChangedListener(final Object listenerId){
        assert unsubscribe(listenerId);
    }

    static final void fillAttributes(final Map<String, AttributeConfiguration> attributes) {
        EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration attribute = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedAttributeConfiguration("bigint");
        attribute.getAdditionalElements().put("objectName", TestManagementBean.BEAN_NAME);
        attributes.put("bigint", attribute);
    }

    static final void fillEvents(final Map<String, EventConfiguration> events){
        EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedEventConfiguration event = new EmbeddedAgentConfiguration.EmbeddedManagementTargetConfiguration.EmbeddedEventConfiguration();
        event.setCategory(AttributeChangeNotification.ATTRIBUTE_CHANGE);
        event.getAdditionalElements().put("severity", "notice");
        event.getAdditionalElements().put("objectName", TestManagementBean.BEAN_NAME);
        events.put("attributeChanged", event);
    }
}
