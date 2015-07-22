package com.itworks.snamp.adapters.ssh;

import com.google.common.collect.ImmutableSet;
import com.itworks.snamp.adapters.modeling.AttributeAccessor;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;

import javax.management.Descriptor;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Set;

import static com.itworks.snamp.adapters.ResourceAdapter.FeatureBindingInfo;

/**
 * Provides transformation between attribute of the connected resource and SSH protocol.
 */
abstract class SshAttributeAccessor extends AttributeAccessor implements SshAttributeMapping {
    static final String GET_COMMAND_PATTERN = "get -n %s -r %s";
    static final String SET_COMMAND_PATTERN = "set -n %s -r %s -v %s";

    SshAttributeAccessor(final MBeanAttributeInfo metadata) {
        super(metadata);
    }

    @Override
    public final String getOriginalName() {
        return AttributeDescriptor.getAttributeName(getMetadata());
    }

    private void printValueAsJson(final Writer output) throws IOException, JMException {
        SshHelpers.FORMATTER.toJson(getValue(), output);
        output.flush();
    }

    @Override
    public final void printValue(final Writer output, final AttributeValueFormat format) throws JMException, IOException {
        switch (format) {
            case JSON:
                printValueAsJson(output);
                return;
            default:
                printValueAsText(output);
        }
    }

    final String getReadCommand(final String resourceName){
        return String.format(GET_COMMAND_PATTERN, getName(), resourceName);
    }

    final String getWriteCommand(final String resourceName){
        return String.format(SET_COMMAND_PATTERN, getName(), resourceName, "<json-value>");
    }

    protected abstract void printValueAsText(final Writer output) throws JMException, IOException;

    /**
     * Prints attribute value.
     *
     * @param input An input stream that contains attribute
     * @throws JMException
     */
    @Override
    public final void setValue(final Reader input) throws JMException, IOException {
        if (getType() != null && canWrite())
            setValue(SshHelpers.FORMATTER.fromJson(input, getType().getJavaType()));
        else throw new UnsupportedOperationException(String.format("Attribute %s is read-only", getName()));
    }

    @Override
    public final void printOptions(final Writer output) throws IOException {
        final Descriptor descr = getMetadata().getDescriptor();
        for (final String fieldName : descr.getFieldNames())
            output.append(String.format("%s = %s", fieldName, descr.getFieldValue(fieldName)));
        output.flush();
    }
}
