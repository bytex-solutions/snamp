package com.bytex.snamp.adapters.nrdp;

import ch.shamu.jsendnrdp.domain.NagiosCheckResult;
import ch.shamu.jsendnrdp.domain.State;
import com.bytex.snamp.adapters.modeling.AttributeAccessor;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.google.common.collect.ImmutableSet;

import javax.management.AttributeNotFoundException;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static com.bytex.snamp.adapters.ResourceAdapter.FeatureBindingInfo;
import static com.bytex.snamp.adapters.nrdp.NRDPAdapterConfigurationDescriptor.getServiceName;
import static com.bytex.snamp.adapters.nrdp.NRDPAdapterConfigurationDescriptor.getUnitOfMeasurement;

/**
 * Provides transformation between attribute of the connected resource and NRDP protocol.
 */
final class NRDPAttributeAccessor extends AttributeAccessor implements FeatureBindingInfo<MBeanAttributeInfo>, Function<String, NagiosCheckResult> {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

    NRDPAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    private NagiosCheckResult getCheckResult(final String host) {
        State state;
        String message;
        final String service = getServiceName(getMetadata().getDescriptor(),
                AttributeDescriptor.getName(getMetadata()));
        try {
            final Object attributeValue = getValue();
            if (attributeValue instanceof Number)
                state = isInRange((Number) attributeValue, DECIMAL_FORMAT) ?
                        State.OK : State.CRITICAL;
            else state = State.OK;
            message = Objects.toString(attributeValue, "0") +
                    getUnitOfMeasurement(getMetadata().getDescriptor());
        } catch (final AttributeNotFoundException | ParseException e) {
            message = e.getMessage();
            state = State.WARNING;
        } catch (final JMException e) {
            message = e.getMessage();
            state = State.CRITICAL;
        }
        return new NagiosCheckResult(host, service, state, message);
    }

    @Override
    public NagiosCheckResult apply(final String host) {
        return getCheckResult(host);
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
