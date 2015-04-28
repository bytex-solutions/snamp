package com.itworks.snamp.jmx.xml;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Represents JAXB adapter for {@link javax.management.ObjectName} type.
 * This class cannot be inherited.
 */
final class ObjectNameAdapter extends XmlAdapter<String, ObjectName> {
    @Override
    public ObjectName unmarshal(final String value) throws MalformedObjectNameException {
        return new ObjectName(value);
    }

    @Override
    public String marshal(final ObjectName value) {
        return value.getCanonicalName();
    }
}
