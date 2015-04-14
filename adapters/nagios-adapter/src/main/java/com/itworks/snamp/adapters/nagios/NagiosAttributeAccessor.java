package com.itworks.snamp.adapters.nagios;

import com.itworks.snamp.adapters.AttributeAccessor;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class NagiosAttributeAccessor extends AttributeAccessor {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat();

    NagiosAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    NagiosPluginOutput toNagiosOutput() {
        final NagiosPluginOutput result = new NagiosPluginOutput();
        result.setMetadata(getMetadata());
        try {
            final Object attributeValue = getValue();
            result.setValue(attributeValue);
            if (attributeValue instanceof Number) {
                if (isInRange((Number) attributeValue, DECIMAL_FORMAT))
                    if (result.checkCritThreshold((Number) attributeValue))
                        if (result.checkWarnThreshold((Number) attributeValue))
                            result.setStatus(NagiosPluginOutput.Status.OK);
                        else result.setStatus(NagiosPluginOutput.Status.WARNING);
                    else result.setStatus(NagiosPluginOutput.Status.CRITICAL);
                else result.setStatus(NagiosPluginOutput.Status.CRITICAL);
            } else result.setStatus(NagiosPluginOutput.Status.OK);
        } catch (final MBeanException | ReflectionException e) {
            result.setMessage(e.getMessage());
            result.setStatus(NagiosPluginOutput.Status.CRITICAL);
        } catch (final AttributeNotFoundException | ParseException e) {
            result.setMessage(e.getMessage());
            result.setStatus(NagiosPluginOutput.Status.WARNING);
        }
        return result;
    }

    @Override
    public boolean canWrite() {
        return false;
    }
}
