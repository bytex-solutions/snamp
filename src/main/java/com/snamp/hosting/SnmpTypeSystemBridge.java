package com.snamp.hosting;

import com.snamp.ExtensionsManager;
import com.snamp.TimeSpan;
import com.snamp.connectors.*;
import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.*;
import org.snmp4j.smi.*;

import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        protected final String attributeClassName;

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
            this.attributeClassName = attributeInfo.getAttributeClassName();
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
            this(oid, connector, connector.getAttributeInfo(oid),defval, timeouts);
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
			return new Integer32(Integer.parseInt(value.toString()));
		}

        /**
         * Converts the SNMP-compliant value to the management connector native value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected Number convert(final Integer32 value) {
            switch (attributeClassName){
                case "int":
                case "java.lang.Integer": return value.toInt();
                case "long":
                case "java.lang.Long": return value.toLong();
                case "short":
                case "java.lang.Short": return (short)value.toInt();
                case "byte":
                case "java.lang.Byte": return (byte)value.toInt();
                default: return defaultValue;
            }
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
            return new Integer32(Boolean.TRUE.equals(value) ? 1 : 0);
        }

        /**
         * Converts the SNMP-compliant value to the management connector native value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected Boolean convert(final Integer32 value) {
            return value.toLong() > 0;
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
            return new OctetString(value != null ? value.toString() : defaultValue);
        }

        /**
         * Converts the SNMP-compliant value to the management connector native value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected String convert(final OctetString value) {
            switch (attributeClassName){
                case "java.lang.String":
                case "string": return value.toString();
                default: return defaultValue;
            }
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
            return new OctetString((value != null ? value : defaultValue).toString());
        }

        /**
         * Converts the SNMP-compliant value to the management connector native value.
         *
         * @param value The value to convert.
         * @return
         */
        @Override
        protected Number convert(final OctetString value) {
            switch (attributeClassName){
                case "float":
                case "java.lang.Float": return Float.valueOf(value.toString());
                case "double":
                case "java.lang.Double":return Double.valueOf(value.toString());
                default: return defaultValue;
            }
        }
    }

    private ManagedObject createManagedObject(final String oid, final String attributeType, final TimeSpan timeouts){
        //for all integers
        switch (attributeType){
            case "int":
            case "java.lang.Integer":
            case "byte":
            case "java.lang.Byte":
            case "short":
            case "java.lang.Short":
            case "long":
            case "java.lang.Long":
                return new IntegerValueProvider(oid, connector, timeouts);
            case "boolean":
            case "java.lang.Boolean":
                return new BooleanValueProvider(oid, connector, timeouts);
            case "float":
            case "java.lang.Float":
            case "double":
            case "java.lang.double":
                return new FloatValueProvider(oid, connector, timeouts);
            default: return new StringValueProvider(oid, connector, timeouts);
        }
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
        return attributeMetadata !=null ? createManagedObject(oid, attributeMetadata.getAttributeClassName(), timeouts) : null;
    }
}
