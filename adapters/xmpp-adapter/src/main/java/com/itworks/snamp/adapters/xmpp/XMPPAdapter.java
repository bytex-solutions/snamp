package com.itworks.snamp.adapters.xmpp;

import com.itworks.snamp.adapters.AbstractResourceAdapter;
import com.itworks.snamp.adapters.FeatureAccessor;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.initializer.SmackInitializer;
import org.jivesoftware.smack.java7.Java7SmackInitializer;
import org.jivesoftware.smack.packet.Presence;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class XMPPAdapter extends AbstractResourceAdapter {
    static {
        final SmackInitializer initializer = new Java7SmackInitializer();
        initializer.initialize();
    }

    static final String NAME = "xmpp";

    private AbstractXMPPConnection connection;
    private final ChatController controller;

    XMPPAdapter(final String instanceName) {
        super(instanceName);
        connection = null;
        controller = new ChatController(getLogger());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo, S> FeatureAccessor<M, S> addFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, S>)controller.getAttributes().addAttribute(resourceName, (MBeanAttributeInfo)feature);
        else return null;
    }

    @Override
    protected Iterable<? extends FeatureAccessor<?, ?>> removeAllFeatures(final String resourceName) {
        return controller.getAttributes().clear(resourceName);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M, ?> removeFeature(final String resourceName, final M feature) {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, ?>)controller.getAttributes().removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else return null;
    }

    @Override
    protected void start(final Map<String, String> parameters) throws AbsentXMPPConfigurationParameterException, IOException, XMPPException, SmackException, GeneralSecurityException {
        connection = XMPPAdapterConfiguration.createConnection(parameters);
        connection.connect();
        connection.login();
        final ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addChatListener(controller);
    }

    @Override
    protected void stop() throws Exception {
        connection.disconnect(new Presence(Presence.Type.unavailable));
        connection = null;
        System.gc();
    }

    @Override
    public Logger getLogger() {
        return getLogger(NAME);
    }

}
