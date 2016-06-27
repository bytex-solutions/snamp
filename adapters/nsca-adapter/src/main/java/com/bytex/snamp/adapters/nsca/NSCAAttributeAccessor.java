package com.bytex.snamp.adapters.nsca;

import com.bytex.snamp.adapters.modeling.AttributeAccessor;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.googlecode.jsendnsca.core.MessagePayload;

import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.Set;

import static com.bytex.snamp.adapters.ResourceAdapter.FeatureBindingInfo;
import static com.bytex.snamp.adapters.nsca.configuration.NSCAAdapterConfigurationDescriptor.getServiceName;
import static com.bytex.snamp.adapters.nsca.configuration.NSCAAdapterConfigurationDescriptor.getUnitOfMeasurement;

/**
 * Provides transformation between attribute of the resource and NSCA protocol.
 */
final class NSCAAttributeAccessor extends AttributeAccessor implements FeatureBindingInfo<MBeanAttributeInfo>, Function<String, MessagePayload> {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

    NSCAAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    private MessagePayload getMessage(final String host) {
        final MessagePayload payload = new MessagePayload();
        payload.setHostname(host);
        payload.setServiceName(getServiceName(getMetadata().getDescriptor(),
                AttributeDescriptor.getName(getMetadata())));
        try {
            final Object attributeValue = getValue();
            payload.setMessage(Objects.toString(attributeValue, "0") +
                    getUnitOfMeasurement(getMetadata().getDescriptor()));
            if (attributeValue instanceof Number)
                payload.setLevel(isInRange((Number) attributeValue, DECIMAL_FORMAT) ?
                        MessagePayload.LEVEL_OK : MessagePayload.LEVEL_CRITICAL);
            else payload.setLevel(MessagePayload.LEVEL_OK);
        } catch (final AttributeNotFoundException | ParseException e) {
            payload.setMessage(e.getMessage());
            payload.setLevel(MessagePayload.LEVEL_WARNING);
        } catch (final JMException e) {
            payload.setMessage(e.getMessage());
            payload.setLevel(MessagePayload.LEVEL_CRITICAL);
        }
        return payload;
    }

    @Override
    public MessagePayload apply(final String host) {
        return getMessage(host);
    }

    @Override
    public boolean canWrite() {
        return false;
    }

    @Override
    public Object getProperty(final String propertyName) {
        return null;
    }

    @Override
    public Set<String> getProperties() {
        return ImmutableSet.of();
    }

    @Override
    public boolean setProperty(final String propertyName, final Object value) {
        return false;
    }
}
