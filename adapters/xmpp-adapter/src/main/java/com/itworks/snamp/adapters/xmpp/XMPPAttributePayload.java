package com.itworks.snamp.adapters.xmpp;

import com.itworks.snamp.StringAppender;
import com.itworks.snamp.internal.annotations.SpecialUse;

import javax.management.MBeanAttributeInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents attribute payload in the form of the XMPP packet extension.
 * This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
@XmlRootElement(name = XMPPAttributePayload.NAME, namespace = XMPPAttributePayload.NAMESPACE)
@XmlAccessorType(XmlAccessType.PROPERTY)
public final class XMPPAttributePayload {
    static final String NAME = "attribute";
    public static final String NAMESPACE = "http://www.itworks.com/snamp/schemas/xmpp/attribute";

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

    private final Map<String, String> properties = new HashMap<>(10);
    private String attributeName = "";
    private boolean canRead = false;
    private boolean canWrite = false;

    @SpecialUse
    public XMPPAttributePayload(){

    }

    public void init(final MBeanAttributeInfo metadata){
        for (final String fieldName : metadata.getDescriptor().getFieldNames()) {
            final Object fieldValue = metadata.getDescriptor().getFieldValue(fieldName);
            if (fieldValue != null)
                properties.put(fieldName, fieldValue.toString());
        }
        canRead = metadata.isReadable();
        canWrite = metadata.isWritable();
    }

    @XmlAttribute(name = "readable", namespace = NAMESPACE)
    @SpecialUse
    public boolean isReadable(){
        return canRead;
    }

    @SpecialUse
    public void setReadable(final boolean value){
        canRead = value;
    }

    @SpecialUse
    @XmlAttribute(name = "writable", namespace = NAMESPACE)
    public boolean isWritable(){
        return canWrite;
    }

    @SpecialUse
    public void setWritable(final boolean value){
        canWrite = value;
    }

    @SpecialUse
    @XmlAttribute(name = "name", namespace = NAMESPACE)
    public String getAttributeName() {
        return attributeName;
    }

    @SpecialUse
    public void setAttributeName(final String value) {
        this.attributeName = value;
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
}
