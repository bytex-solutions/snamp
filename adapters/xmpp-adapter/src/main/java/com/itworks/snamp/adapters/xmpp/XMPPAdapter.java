package com.itworks.snamp.adapters.xmpp;

import com.google.common.collect.Iterables;
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
import javax.management.MBeanNotificationInfo;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class XMPPAdapter extends AbstractResourceAdapter {
    static {
        final SmackInitializer initializer = new Java7SmackInitializer();
        initializer.initialize();
    }

    private AbstractXMPPConnection connection;
    private final Bot chatBot;

    XMPPAdapter(final String instanceName) {
        super(instanceName);
        connection = null;
        chatBot = new Bot(getLogger());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo, S> FeatureAccessor<M, S> addFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, S>) chatBot.getAttributes().addAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M, S>) chatBot.getNotifications().enableNotifications(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected Iterable<? extends FeatureAccessor<?, ?>> removeAllFeatures(final String resourceName) {
        return Iterables.concat(chatBot.getAttributes().clear(resourceName),
                chatBot.getNotifications().clear(resourceName));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M, ?> removeFeature(final String resourceName, final M feature) {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M, ?>) chatBot.getAttributes().removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M, ?>) chatBot.getNotifications().disableNotifications(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected void start(final Map<String, String> parameters) throws AbsentXMPPConfigurationParameterException, IOException, XMPPException, SmackException, GeneralSecurityException {
        connection = XMPPAdapterConfiguration.createConnection(parameters);
        connection.connect();
        connection.login();
        final ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addChatListener(chatBot);
    }

    @Override
    protected void stop() throws Exception {
        chatBot.close();
        if (connection.isConnected())
            connection.disconnect(new Presence(Presence.Type.unavailable));
        connection = null;
    }

}
