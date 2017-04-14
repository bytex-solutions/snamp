package com.bytex.snamp.gateway.xmpp;

import com.bytex.snamp.gateway.AbstractGateway;
import com.bytex.snamp.gateway.modeling.AttributeSet;
import com.bytex.snamp.gateway.modeling.FeatureAccessor;
import com.bytex.snamp.gateway.modeling.NotificationSet;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.initializer.SmackInitializer;
import org.jivesoftware.smack.java7.Java7SmackInitializer;
import org.jivesoftware.smack.packet.Presence;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanNotificationInfo;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class XMPPGateway extends AbstractGateway {
    static {
        final SmackInitializer initializer = new Java7SmackInitializer();
        initializer.initialize();
    }

    private AbstractXMPPConnection connection;
    private final Bot chatBot;

    XMPPGateway(final String instanceName) {
        super(instanceName);
        connection = null;
        chatBot = new Bot();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> addFeature(final String resourceName, final M feature) throws Exception {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>) chatBot.getAttributes().addAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>) chatBot.getNotifications().enableNotifications(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected Stream<? extends FeatureAccessor<?>> removeAllFeatures(final String resourceName) throws InterruptedException {
        return Stream.concat(
                chatBot.getAttributes().clear(resourceName).stream(),
                chatBot.getNotifications().clear(resourceName).stream()
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <M extends MBeanFeatureInfo> FeatureAccessor<M> removeFeature(final String resourceName, final M feature) throws InterruptedException {
        if(feature instanceof MBeanAttributeInfo)
            return (FeatureAccessor<M>) chatBot.getAttributes().removeAttribute(resourceName, (MBeanAttributeInfo)feature);
        else if(feature instanceof MBeanNotificationInfo)
            return (FeatureAccessor<M>) chatBot.getNotifications().disableNotifications(resourceName, (MBeanNotificationInfo)feature);
        else return null;
    }

    @Override
    protected void start(final Map<String, String> parameters) throws Exception {
        connection = XMPPGatewayConfigurationProvider.createConnection(parameters);
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

    private static Multimap<String, ? extends FeatureBindingInfo<MBeanAttributeInfo>> getAttributes(final AttributeSet<XMPPAttributeAccessor> attributes){
        final Multimap<String, ReadOnlyFeatureBindingInfo<MBeanAttributeInfo>> result = HashMultimap.create();
        attributes.forEachAttribute((resourceName, accessor) -> {
            final ImmutableMap.Builder<String, String> parameters = ImmutableMap.builder();
            if (accessor.canRead())
                parameters.put("read-command", accessor.getReadCommand(resourceName));
            if (accessor.canWrite())
                parameters.put("write-command", accessor.getWriteCommand(resourceName));
            return result.put(resourceName, new ReadOnlyFeatureBindingInfo<>(accessor, parameters.build()));
        });
        return result;
    }

    private static Multimap<String, ? extends FeatureBindingInfo<MBeanNotificationInfo>> getNotifications(final NotificationSet<XMPPNotificationAccessor> notifs) {
        final Multimap<String, ReadOnlyFeatureBindingInfo<MBeanNotificationInfo>> result = HashMultimap.create();
        notifs.forEachNotification((resourceName, accessor) -> result.put(resourceName, new ReadOnlyFeatureBindingInfo<>(accessor, "listen-command", accessor.getListenCommand())));
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends MBeanFeatureInfo> Multimap<String, ? extends FeatureBindingInfo<M>> getBindings(final Class<M> featureType) {
        if(featureType.isAssignableFrom(MBeanAttributeInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getAttributes(chatBot.getAttributes());
        else if(featureType.isAssignableFrom(MBeanNotificationInfo.class))
            return (Multimap<String, ? extends FeatureBindingInfo<M>>)getNotifications(chatBot.getNotifications());
        return super.getBindings(featureType);
    }
}
