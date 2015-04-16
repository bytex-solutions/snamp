package com.itworks.snamp.adapters.xmpp;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itworks.snamp.StringAppender;
import com.itworks.snamp.internal.annotations.SpecialUse;
import com.itworks.snamp.jmx.json.Formatters;

import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents notification payload in the form of the XMPP packet extension.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@XmlRootElement(name = XMPPNotificationPayload.NAME, namespace = XMPPNotificationPayload.NAMESPACE)
@XmlAccessorType(XmlAccessType.PROPERTY)
public final class XMPPNotificationPayload {
    private static Gson FORMATTER = Formatters.enableAll(new GsonBuilder()).create();
    static final String NAME = "notification";
    public static final String NAMESPACE = "http://www.itworks.com/snamp/schemas/xmpp/notification";
    private static final Marshaller MARSHALLER;

    static {
        try {
            final JAXBContext context = JAXBContext.newInstance(XMPPNotificationPayload.class);
            MARSHALLER = context.createMarshaller();
            MARSHALLER.setProperty(Marshaller.JAXB_FRAGMENT, true);
        } catch (final JAXBException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @XmlRootElement(name = "property", namespace = NAMESPACE)
    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class KeyValuePair {
        private String key = "";

        private String value = "";

        @SpecialUse
        public KeyValuePair(){

        }

        public KeyValuePair(final String key, final String value){
            this.key = key;
            this.value = value;
        }

        public KeyValuePair(final Map.Entry<String, String> entry){
            this(entry.getKey(), entry.getValue());
        }

        @XmlAttribute(name = "name", namespace = NAMESPACE)
        @SpecialUse
        public String getKey(){
            return key;
        }

        @SpecialUse
        public void setKey(final String value){
            key = value;
        }

        @XmlValue
        @SpecialUse
        public String getValue(){
            return value;
        }

        @SpecialUse
        public void setValue(final String value){
            this.value = value;
        }
    }

    private String source = "";
    private String message = "";
    private long timeStamp = 0L;
    private long sequenceNumber = 0L;
    private String notificationType = "";
    private String userData = "";
    private final Map<String, String> properties = new HashMap<>(10);

    @SpecialUse
    public XMPPNotificationPayload(){

    }

    public XMPPNotificationPayload(final Notification n){
        source = Objects.toString(n.getSource());
        message = n.getMessage();
        timeStamp = n.getTimeStamp();
        sequenceNumber = n.getSequenceNumber();
        notificationType = n.getType();
        if(n.getUserData() != null)
            userData = FORMATTER.toJson(n.getUserData());
    }

    public XMPPNotificationPayload(final Notification n, final MBeanNotificationInfo metadata){
        this(n);
        for(final String fieldName: metadata.getDescriptor().getFieldNames()){
            final Object fieldValue = metadata.getDescriptor().getFieldValue(fieldName);
            if(fieldValue != null)
                properties.put(fieldName, fieldValue.toString());
        }
    }

    @XmlElement(name = "property", namespace = NAMESPACE)
    @SpecialUse
    public KeyValuePair[] getProperties(){
        final KeyValuePair[] result = new KeyValuePair[properties.size()];
        int index = 0;
        for(final Map.Entry<String, String> entry: properties.entrySet())
            result[index++] = new KeyValuePair(entry);
        return result;
    }

    @SpecialUse
    public void setProperties(final KeyValuePair[] properties){
        for(final KeyValuePair pair: properties)
            this.properties.put(pair.getKey(), pair.getValue());
    }

    @XmlElement(name = "source", namespace = NAMESPACE)
    @SpecialUse
    public String getSource() {
        return source;
    }

    @SpecialUse
    public void setSource(final String value) {
        this.source = value;
    }

    @XmlElement(name = "message", namespace = NAMESPACE)
    @SpecialUse
    public String getMessage() {
        return message;
    }

    @SpecialUse
    public void setMessage(final String value) {
        this.message = value;
    }

    @XmlElement(name = "timeStamp", namespace = NAMESPACE)
    @SpecialUse
    public long getTimeStamp() {
        return timeStamp;
    }

    @SpecialUse
    public void setTimeStamp(final long value) {
        this.timeStamp = value;
    }

    @XmlElement(name = "sequenceNumber", namespace = NAMESPACE)
    @SpecialUse
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    @SpecialUse
    public void setSequenceNumber(final long value) {
        this.sequenceNumber = value;
    }

    @XmlAttribute(name = "type", namespace = NAMESPACE)
    @SpecialUse
    public String getNotificationType() {
        return notificationType;
    }

    @SpecialUse
    public void setNotificationType(final String value) {
        this.notificationType = value;
    }

    @XmlElement(name = "userData", namespace = NAMESPACE)
    @SpecialUse
    public String getUserData() {
        return userData;
    }

    @SpecialUse
    public void setUserData(final String value) {
        this.userData = value;
    }

    public String toXML() throws JAXBException {
        try (final StringAppender writer = new StringAppender(1024)) {
            MARSHALLER.marshal(this, writer);
            return writer.toString();
        }
    }

    @Override
    public String toString() {
        try {
            return toXML();
        } catch (final JAXBException e) {
            return e.getMessage();
        }
    }

    static String toString(final Notification n){
        return FORMATTER.toJson(n);
    }
}
