package com.itworks.snamp.adapters.xmpp;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;
import com.itworks.snamp.Consumer;
import com.itworks.snamp.SafeConsumer;
import com.itworks.snamp.StringAppender;
import com.itworks.snamp.adapters.modeling.ModelOfAttributes;
import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import com.itworks.snamp.jmx.TabularDataUtils;
import com.itworks.snamp.jmx.WellKnownType;
import com.itworks.snamp.jmx.json.JsonSerializerFunction;
import org.jivesoftware.smack.packet.ExtensionElement;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import java.nio.*;
import java.util.Collection;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class XMPPAttributeModelOfAttributes extends ModelOfAttributes<XMPPAttributeAccessor> implements AttributeReader, AttributeWriter {
    private static final class Reader implements Consumer<XMPPAttributeAccessor, JMException>{
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

    private static final class OptionsPrinter extends StringAppender implements SafeConsumer<XMPPAttributeAccessor> {
        private static final long serialVersionUID = 5711252453739087077L;
        private final boolean withNames;
        private final boolean details;

        private OptionsPrinter(final boolean withNames, final boolean details) {
            super(64);
            this.withNames = withNames;
            this.details = details;
        }

        @Override
        public void accept(final XMPPAttributeAccessor accessor) {
            if (withNames)
                appendln("ID: %s NAME: %s CAN_READ: %s CAN_WRITE %s",
                        accessor.getName(),
                        accessor.getOriginalName(),
                        accessor.canRead(),
                        accessor.canWrite());
            else appendln(accessor.getName());
            if (details) accessor.printOptions(this);
        }
    }

    private static final class Writer implements Consumer<XMPPAttributeAccessor, JMException>{
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
        protected static final char WHITESPACE = ' ';
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
            final StringAppender result = new StringAppender(64);
            for (final String key : value.getCompositeType().keySet())
                result.appendln("%s = %s", key, FORMATTER.toJson(value.get(key)));
            return result.toString();
        }
    }

    private static final class TabularDataAttribute extends XMPPAttributeAccessor {
        private TabularDataAttribute(final MBeanAttributeInfo metadata){
            super(metadata);
        }

        private static String joinString(final Collection<?> values,
                                         final String format,
                                         final String separator) {
            return Joiner.on(separator).join(Collections2.transform(values, new Function<Object, String>() {
                @Override
                public final String apply(final Object input) {
                    return String.format(format, input);
                }
            }));
        }

        @Override
        protected String getValueAsText() throws JMException {
            final StringBuilder result = new StringBuilder(64);
            final TabularData data = getValue(TabularData.class);
            final String COLUMN_SEPARATOR = "\t";
            final String ITEM_FORMAT = "%-10s";
            //print column first
            result.append(joinString(data.getTabularType().getRowType().keySet(), ITEM_FORMAT, COLUMN_SEPARATOR));
            //print rows
            TabularDataUtils.forEachRow(data, new SafeConsumer<CompositeData>() {
                @SuppressWarnings("unchecked")
                @Override
                public void accept(final CompositeData row) {
                    final Collection<?> values = Collections2.transform(row.values(), new JsonSerializerFunction(FORMATTER));
                    result.append(joinString(values, ITEM_FORMAT, COLUMN_SEPARATOR));
                }
            });
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
