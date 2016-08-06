package com.bytex.snamp.adapters.xmpp;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.ExceptionPlaceholder;
import com.bytex.snamp.adapters.modeling.ModelOfAttributes;
import com.bytex.snamp.connectors.attributes.AttributeDescriptor;
import com.bytex.snamp.jmx.TabularDataUtils;
import com.bytex.snamp.jmx.WellKnownType;
import org.jivesoftware.smack.packet.ExtensionElement;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.nio.*;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
final class XMPPModelOfAttributes extends ModelOfAttributes<XMPPAttributeAccessor> implements AttributeReader, AttributeWriter {
    private static final class Reader implements Acceptor<XMPPAttributeAccessor, JMException> {
        private final AttributeValueFormat format;
        private String output;
        private final Collection<ExtensionElement> extras;

        private Reader(final AttributeValueFormat format,
                       final Collection<ExtensionElement> extras){
            this.format = format;
            output = null;
            this.extras = extras;
        }

        @Override
        public void accept(final XMPPAttributeAccessor value) throws JMException {
            output = value.getValue(format);
            value.createExtensions(extras);
        }

        @Override
        public String toString() {
            return output == null || output.isEmpty() ? super.toString() : output;
        }
    }

    private static final class OptionsPrinter implements Consumer<XMPPAttributeAccessor>, Acceptor<XMPPAttributeAccessor, ExceptionPlaceholder> {
        private final boolean withNames;
        private final boolean details;
        private final StringBuilder result;

        private OptionsPrinter(final boolean withNames, final boolean details) {
            result = new StringBuilder(64);
            this.withNames = withNames;
            this.details = details;
        }

        @Override
        public void accept(final XMPPAttributeAccessor accessor) {
            if (withNames)
                result.append(String.format("ID: %s NAME: %s CAN_READ: %s CAN_WRITE %s",
                            accessor.getName(),
                            accessor.getOriginalName(),
                            accessor.canRead(),
                            accessor.canWrite()));
            else
                result.append(accessor.getName());
            result.append(System.lineSeparator());
            if (details) accessor.printOptions(result);
        }

        @Override
        public String toString() {
            return result.toString();
        }
    }

    private static final class Writer implements Acceptor<XMPPAttributeAccessor, JMException> {
        private final String value;

        private Writer(final String val){
            this.value = val;
        }

        @Override
        public void accept(final XMPPAttributeAccessor accessor) throws JMException {
            accessor.setValue(value);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private static final class ReadOnlyAttribute extends XMPPAttributeAccessor {
        private ReadOnlyAttribute(final MBeanAttributeInfo metadata){
            super(metadata);
        }

        @Override
        protected String getValueAsText() throws JMException{
            return Objects.toString(getValue(), "NULL");
        }


        @Override
        public boolean canWrite() {
            return false;
        }
    }

    private static final class DefaultAttribute extends XMPPAttributeAccessor{

        private DefaultAttribute(final MBeanAttributeInfo metadata) {
            super(metadata);
        }

        @Override
        protected String getValueAsText() throws JMException {
            return Objects.toString(getValue(), "");
        }
    }

    private static abstract class AbstractBufferAttribute<B extends Buffer> extends XMPPAttributeAccessor {
        static final char WHITESPACE = ' ';
        private final Class<B> bufferType;

        private AbstractBufferAttribute(final MBeanAttributeInfo metadata,
                                               final Class<B> bufferType){
            super(metadata);
            this.bufferType = bufferType;
        }

        protected abstract void getValueAsText(final B buffer, final StringBuilder writer);

        @Override
        protected final String getValueAsText() throws JMException {
            final StringBuilder builder = new StringBuilder(128);
            getValueAsText(getValue(bufferType), builder);
            return builder.toString();
        }
    }

    private static final class ByteBufferAttribute extends AbstractBufferAttribute<ByteBuffer> {
        private ByteBufferAttribute(final MBeanAttributeInfo metadata){
            super(metadata, ByteBuffer.class);
        }

        @Override
        protected void getValueAsText(final ByteBuffer buffer, final StringBuilder output) {
            while (buffer.hasRemaining())
                output.append(buffer.get()).append(WHITESPACE);
        }
    }

    private static final class CharBufferAttribute extends AbstractBufferAttribute<CharBuffer> {
        private CharBufferAttribute(final MBeanAttributeInfo metadata){
            super(metadata, CharBuffer.class);
        }

        @Override
        protected void getValueAsText(final CharBuffer buffer, final StringBuilder output) {
            while (buffer.hasRemaining())
                output.append(buffer.get()).append(WHITESPACE);
        }
    }

    private static final class ShortBufferAttribute extends AbstractBufferAttribute<ShortBuffer> {
        private ShortBufferAttribute(final MBeanAttributeInfo metadata){
            super(metadata, ShortBuffer.class);
        }

        @Override
        protected void getValueAsText(final ShortBuffer buffer, final StringBuilder output) {
            while (buffer.hasRemaining())
                output.append(buffer.get()).append(WHITESPACE);
        }
    }

    private static final class IntBufferAttribute extends AbstractBufferAttribute<IntBuffer> {
        private IntBufferAttribute(final MBeanAttributeInfo metadata){
            super(metadata, IntBuffer.class);
        }

        @Override
        protected void getValueAsText(final IntBuffer buffer, final StringBuilder output) {
            while (buffer.hasRemaining())
                output.append(buffer.get()).append(WHITESPACE);
        }
    }

    private static final class LongBufferAttribute extends AbstractBufferAttribute<LongBuffer> {
        private LongBufferAttribute(final MBeanAttributeInfo metadata){
            super(metadata, LongBuffer.class);
        }

        @Override
        protected void getValueAsText(final LongBuffer buffer, final StringBuilder output) {
            while (buffer.hasRemaining())
                output.append(buffer.get()).append(WHITESPACE);
        }
    }

    private static final class FloatBufferAttribute extends AbstractBufferAttribute<FloatBuffer> {
        private FloatBufferAttribute(final MBeanAttributeInfo metadata){
            super(metadata, FloatBuffer.class);
        }

        @Override
        protected void getValueAsText(final FloatBuffer buffer, final StringBuilder output) {
            while (buffer.hasRemaining())
                output.append(buffer.get()).append(WHITESPACE);
        }
    }

    private static final class DoubleBufferAttribute extends AbstractBufferAttribute<DoubleBuffer> {
        private DoubleBufferAttribute(final MBeanAttributeInfo metadata){
            super(metadata, DoubleBuffer.class);
        }

        @Override
        protected void getValueAsText(final DoubleBuffer buffer, final StringBuilder output) {
            while (buffer.hasRemaining())
                output.append(buffer.get()).append(WHITESPACE);
        }
    }

    private static final class CompositeDataAttribute extends XMPPAttributeAccessor {
        private CompositeDataAttribute(final MBeanAttributeInfo metadata) {
            super(metadata);
        }

        @Override
        protected String getValueAsText() throws JMException {
            final CompositeData value = getValue(CompositeData.class);
            final StringBuilder result = new StringBuilder(64);
            for (final String key : value.getCompositeType().keySet())
                result
                        .append(String.format("%s = %s", key, FORMATTER.toJson(value.get(key))))
                        .append(System.lineSeparator());
            return result.toString();
        }
    }

    private static final class TabularDataAttribute extends XMPPAttributeAccessor {
        private TabularDataAttribute(final MBeanAttributeInfo metadata){
            super(metadata);
        }

        private static String joinString(final Stream<String> values,
                                         final String format,
                                         final String separator) {
            return String.join(separator, (CharSequence[]) values.map(input -> String.format(format, input)).toArray(String[]::new));
        }

        @Override
        protected String getValueAsText() throws JMException {
            final StringBuilder result = new StringBuilder(64);
            final TabularData data = getValue(TabularData.class);
            final String COLUMN_SEPARATOR = "\t";
            final String ITEM_FORMAT = "%-10s";
            //print column first
            result.append(joinString(data.getTabularType().getRowType().keySet().stream(), ITEM_FORMAT, COLUMN_SEPARATOR));
            //print rows
            TabularDataUtils.forEachRow(data, row -> result.append(joinString(row.values().stream().map(FORMATTER::toJson), ITEM_FORMAT, COLUMN_SEPARATOR)));
            return result.toString();
        }
    }

    @Override
    protected XMPPAttributeAccessor createAccessor(final MBeanAttributeInfo metadata) {
        final WellKnownType attributeType = AttributeDescriptor.getType(metadata);
        if (attributeType != null)
            switch (attributeType) {
                case BYTE_BUFFER:
                    return new ByteBufferAttribute(metadata);
                case CHAR_BUFFER:
                    return new CharBufferAttribute(metadata);
                case SHORT_BUFFER:
                    return new ShortBufferAttribute(metadata);
                case INT_BUFFER:
                    return new IntBufferAttribute(metadata);
                case LONG_BUFFER:
                    return new LongBufferAttribute(metadata);
                case FLOAT_BUFFER:
                    return new FloatBufferAttribute(metadata);
                case DOUBLE_BUFFER:
                    return new DoubleBufferAttribute(metadata);
                case DICTIONARY:
                    return new CompositeDataAttribute(metadata);
                case TABLE:
                    return new TabularDataAttribute(metadata);
                default:
                    return new DefaultAttribute(metadata);
            }
        return new ReadOnlyAttribute(metadata);
    }

    @Override
    public String getAttribute(final String resourceName,
                               final String attributeID,
                               final AttributeValueFormat format,
                               final Collection<ExtensionElement> extras) throws JMException{
        final Reader reader = new Reader(format, extras);
        processAttribute(resourceName, attributeID, reader);
        return reader.toString();
    }

    @Override
    public void setAttribute(final String resourceName, final String attributeID, final String value) throws JMException {
        processAttribute(resourceName, attributeID, new Writer(value));
    }

    @Override
    public String printOptions(final String resourceName,
                               final String attributeID,
                               final boolean withNames,
                               final boolean details) {
        final OptionsPrinter printer = new OptionsPrinter(withNames, details);
        processAttribute(resourceName, attributeID, printer);
        return printer.toString();
    }
}
