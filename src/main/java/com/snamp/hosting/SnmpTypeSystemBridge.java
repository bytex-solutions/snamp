package com.snamp.hosting;

import com.snamp.ExtensionsManager;
import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.*;
import org.snmp4j.smi.*;

import javax.management.openmbean.TabularData;
import java.math.*;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.logging.*;

/**
 * Represents a bridge between management connector attribute types and SNMP-compliant types.
 * @author rvsakno
 *
 */
final class SnmpTypeSystemBridge implements AutoCloseable {
    private static Logger log = Logger.getLogger("snamp.log");
    private final ManagementConnector connector;

    /**
     * Initializes a new type system bridge.
     * @param connector The connector to wrap.
     * @exception IllegalArgumentException connector is null.
     */
	public SnmpTypeSystemBridge(final ManagementConnector connector){
	    if(connector == null) throw new IllegalArgumentException("connector is null.");
        this.connector = connector;
	}

    /**
     * Releases all resources associated with the wrapped connector.
     * @throws Exception
     */
    @Override
    public final void close() throws Exception{
        connector.close();
    }
	
	private static abstract class ScalarValueProvider<T extends Variable> extends MOScalar<T>{
		private final T defaultValue;
		private final ManagementConnector connector;
        private final TimeSpan timeouts;
        /**
         * Represents the type of the attribute.
         */
        protected final AttributeTypeInfo attributeTypeInfo;

        private static MOAccess getAccessRestrictions(final AttributeMetadata metadata){
            switch ((metadata.canWrite() ? 1 : 0) << 1 | (metadata.canRead() ? 1 : 0)){
                //case 0: case 1:
                default: return MOAccessImpl.ACCESS_READ_ONLY;
                case 2: return MOAccessImpl.ACCESS_WRITE_ONLY;
                case 3: return MOAccessImpl.ACCESS_READ_WRITE;
            }
        }

        private ScalarValueProvider(final String oid, final ManagementConnector connector, final AttributeMetadata attributeInfo, final T defval, final TimeSpan timeouts){
            super(new OID(oid), getAccessRestrictions(attributeInfo), defval);
            if(connector == null) throw new IllegalArgumentException("connector is null.");
            this.connector = connector;
            defaultValue = defval;
            this.timeouts = timeouts;
            this.attributeTypeInfo = attributeInfo.getAttributeType();
        }

        /**
         * Initializes a new SNMP scala value provider.
         * @param oid Unique identifier of the new management object.
         * @param connector The underlying management connector that provides access to the attribute.
         * @param defval The default value of the management object.
         * @param timeouts Read/write timeout.
         * @exception IllegalArgumentException connector is null.
         */
		protected ScalarValueProvider(final String oid, final ManagementConnector connector, final T defval, final TimeSpan timeouts)
		{
            this(oid, connector, connector.getAttributeInfo(oid), defval, timeouts);
		}

        /**
         * Converts the attribute value into the SNMP-compliant value.
         * @param value The value to convert.
         * @return
         */
		protected abstract T convert(final Object value);

        /**
         * Converts the SNMP-compliant value to the management connector native value.
         * @param value The value to convert.
         * @return
         */
        protected abstract Object convert(final T value);

        /**
         * Returns the SNMP-compliant value of the attribute.
         * @return
         */
		@Override
		public final T getValue() {
			Object result = null;
            try{
                result = connector.getAttribute(super.getID().toString(), timeouts, defaultValue);
            }
            catch (final TimeoutException timeout){
                log.log(Level.WARNING, timeout.getLocalizedMessage(), timeout);
                result = defaultValue;

            }
			return result == null ? defaultValue : convert(result);
		}

        /**
         * Changes the SNMP management object.
         * @param value
         * @return
         */
        @Override
        public final int setValue(final T value) {
            int result = 0;
            try {
                result = connector.setAttribute(super.getID().toString(), timeouts, convert(value)) ? 1 : 0;
            } catch (final TimeoutException timeout) {
                log.log(Level.WARNING, timeout.getLocalizedMessage(), timeout);
                result = 0;
            }
            return result;
        }
    }
	
	/**
	 * SNMP-переменная для целочисленного JMX-атрибута.
	 * @author rvsakno
	 *
	 */
	private static final class IntegerValueProvider extends ScalarValueProvider<Integer32>{
		public static final int defaultValue = -1;

		public IntegerValueProvider(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
			super(oid, connector, new Integer32(defaultValue), timeouts);
		}

		@Override
		protected Integer32 convert(final Object value){
			return new Integer32(attributeTypeInfo.convertTo(value, Integer.class));
		}

        /**
         * Converts the SNMP-compliant value to the management connector native value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected Object convert(final Integer32 value) {
            if(attributeTypeInfo.canConvertFrom(Long.class)) return value.toLong();
            else if(attributeTypeInfo.canConvertFrom(Integer.class)) return value.toInt();
            else if(attributeTypeInfo.canConvertFrom(String.class)) return value.toString();
            else return null;
        }
    }
	
	private static final class BooleanValueProvider extends ScalarValueProvider<Integer32>{
        public static final int defaultValue = -1;

		public BooleanValueProvider(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
			super(oid, connector, new Integer32(defaultValue), timeouts);
		}

        /**
         * Converts the attribute value into the SNMP-compliant value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected Integer32 convert(final Object value) {
            return new Integer32(attributeTypeInfo.convertTo(value, Integer.class));
        }

        /**
         * Converts the SNMP-compliant value to the management connector native value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected Boolean convert(final Integer32 value) {
            return value.toLong() != 0;
        }
    }
	
	private static final class StringValueProvider extends ScalarValueProvider<OctetString>{
        public static final String defaultValue = "";
		
		public StringValueProvider(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
			super(oid, connector, new OctetString(defaultValue), timeouts);
		}

        /**
         * Converts the attribute value into the SNMP-compliant value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected OctetString convert(final Object value) {
            return new OctetString(attributeTypeInfo.convertTo(value, String.class));
        }

        /**
         * Converts the SNMP-compliant value to the management connector native value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected String convert(final OctetString value) {
            return value.toString();
        }
    }

    private static final class LongValueProvider extends ScalarValueProvider<Counter64>{
        public static final long defaultValue = -1;

        public LongValueProvider(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
            super(oid, connector, new Counter64(defaultValue), timeouts);
        }

        /**
         * Converts the attribute value into the SNMP-compliant value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected Counter64 convert(final Object value) {
            return new Counter64(attributeTypeInfo.convertTo(value, Long.class));
        }

        /**
         * Converts the SNMP-compliant value to the management connector native value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected Long convert(final Counter64 value) {
            if(attributeTypeInfo.canConvertFrom(Long.class)) return value.toLong();
            else return defaultValue;
        }
    }

    private static final class UnixTimeProvider extends ScalarValueProvider<TimeTicks>{
        public static final long defaultValue = -1;

        public UnixTimeProvider(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
            super(oid, connector, new TimeTicks(defaultValue), timeouts);
        }

        /**
         * Converts the attribute value into the SNMP-compliant value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected TimeTicks convert(final Object value) {
            return new TimeTicks(attributeTypeInfo.convertTo(value, Long.class));
        }

        /**
         * Converts the SNMP-compliant value to the management connector native value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected Object convert(final TimeTicks value) {
            if(attributeTypeInfo.canConvertFrom(Long.class)) return value.toLong();
            else if(attributeTypeInfo.canConvertFrom(Date.class)) return new Date(value.toLong());
            else return new Date();
        }
    }

    private static final class BigNumberProvider extends ScalarValueProvider<OctetString>{
        public static final Number defaultValue = 0;

        public BigNumberProvider(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
            super(oid, connector, new OctetString(defaultValue.toString()), timeouts);
        }

        /**
         * Converts the attribute value into the SNMP-compliant value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected OctetString convert(final Object value) {
            return new OctetString(attributeTypeInfo.convertTo(value, String.class));
        }

        /**
         * Converts the SNMP-compliant value to the management connector native value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected Object convert(final OctetString value) {
            if(attributeTypeInfo.canConvertFrom(String.class)) return value.toString();
            else return defaultValue;
        }
    }

	private static final class FloatValueProvider extends ScalarValueProvider<OctetString>{
        public static final Float defaultValue = -1.0F;

		public FloatValueProvider(final String oid, final ManagementConnector connector, final TimeSpan timeouts){
			super(oid, connector, new OctetString(defaultValue.toString()), timeouts);
		}

        /**
         * Converts the attribute value into the SNMP-compliant value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected OctetString convert(final Object value) {
            return new OctetString(attributeTypeInfo.convertTo(value, String.class));
        }

        /**
         * Converts the SNMP-compliant value to the management connector native value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected Object convert(final OctetString value) {
            if(attributeTypeInfo.canConvertFrom(String.class)) return value.toString();
            else return defaultValue;
        }
    }

    private ScalarValueProvider<?> createScalarManagedObject(final String oid, final AttributePrimitiveType attributeType, final TimeSpan timeouts){
        switch (attributeType){
            case BOOL: return new BooleanValueProvider(oid, connector, timeouts);
            case INT8:
            case INT16:
            case INT32: return new IntegerValueProvider(oid, connector, timeouts);
            case FLOAT:
            case DOUBLE: return new FloatValueProvider(oid, connector, timeouts);
            case INT64: return new LongValueProvider(oid, connector, timeouts);
            case INTEGER:
            case DECIMAL: return new BigNumberProvider(oid, connector, timeouts);
            case UNIX_TIME: return new UnixTimeProvider(oid, connector, timeouts);
            case TEXT:
            default: return new StringValueProvider(oid, connector, timeouts);
        }
    }

    private ManagedObject createManagedObject(final String oid, final AttributeTypeInfo attributeType, final TimeSpan timeouts){
        if(attributeType instanceof AttributePrimitiveType) return createScalarManagedObject(oid, (AttributePrimitiveType) attributeType, timeouts);
        else return null;
    }

    /**
     * Creates a new SNMP management object based on the management connector attribute.
     * @param oid
     * @param timeouts
     * @return
     */
	public final ManagedObject connectAttribute(final String oid,
                                                final String attributeName,
                                                final Map<String, String> options,
                                                final TimeSpan timeouts){
        final AttributeMetadata attributeMetadata = connector.connectAttribute(oid, attributeName, options);
        return attributeMetadata !=null ? createManagedObject(oid, attributeMetadata.getAttributeType(), timeouts) : null;
    }
}
