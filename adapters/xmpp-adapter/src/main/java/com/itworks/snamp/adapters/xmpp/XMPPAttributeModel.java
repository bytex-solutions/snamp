package com.itworks.snamp.adapters.xmpp;

import com.itworks.snamp.Consumer;
import com.itworks.snamp.adapters.AbstractAttributesModel;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import java.util.Objects;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class XMPPAttributeModel extends AbstractAttributesModel<XMPPAttributeAccessor> implements AttributeReader {
    private static final class Reader implements Consumer<XMPPAttributeAccessor, JMException>{
        private final AttributeValueFormat format;
        private String output;

        private Reader(final AttributeValueFormat format){
            this.format = format;
            output = null;
        }

        @Override
        public void accept(final XMPPAttributeAccessor value) throws JMException {
            output = value.getValue(format);
        }

        @Override
        public String toString() {
            return output == null || output.isEmpty() ? super.toString() : output;
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

    @Override
    protected XMPPAttributeAccessor createAccessor(final MBeanAttributeInfo metadata) throws Exception {
        return new ReadOnlyAttribute(metadata);
    }

    @Override
    public String getAttribute(final String resourceName,
                               final String attributeID,
                               final AttributeValueFormat format) throws JMException{
        final Reader reader = new Reader(format);
        processAttribute(resourceName, attributeID, reader);
        return reader.toString();
    }
}
