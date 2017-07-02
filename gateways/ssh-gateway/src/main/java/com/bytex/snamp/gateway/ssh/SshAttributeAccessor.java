package com.bytex.snamp.gateway.ssh;

import com.bytex.snamp.connector.attributes.AttributeDescriptor;
import com.bytex.snamp.gateway.modeling.AttributeAccessor;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;

import javax.management.Descriptor;
import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Objects;

/**
 * Provides transformation between attribute of the connected resource and SSH protocol.
 */
abstract class SshAttributeAccessor extends AttributeAccessor implements SshAttributeMapping {
    static final String GET_COMMAND_PATTERN = "get -n %s -r %s";
    static final String SET_COMMAND_PATTERN = "set -n %s -r %s -v %s";

    final ObjectMapper formatter;

    SshAttributeAccessor(final MBeanAttributeInfo metadata, final ObjectMapper formatter) {
        super(metadata);
        this.formatter = Objects.requireNonNull(formatter)
                .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    @Override
    public final String getOriginalName() {
        return AttributeDescriptor.getName(getMetadata());
    }

    private void printValueAsJson(final Writer output) throws IOException, JMException {
        formatter.writeValue(output, getValue());
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
            setValue(formatter.readValue(input, getType().getJavaType()));
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
