package com.snamp.connectors.jmx;

import org.snmp4j.agent.*;
import org.snmp4j.agent.mo.*;
import org.snmp4j.smi.*;

/**
 * Фабрика, оборачивающая JMX-атрибуты в SNMP-переменные.
 * @author rvsakno
 *
 */
public final class JmxToSnmpValueConverter {

	private JmxToSnmpValueConverter(){
		
	}
	
	private static abstract class ScalarValueProvider<T extends Variable> extends MOScalar<T>{
		private final T defaultValue;
		private final JmxMonitor.AttributeProvider jmxAttribute;
		
		protected ScalarValueProvider(final String oid,  final JmxMonitor.AttributeProvider attrProvider, final T defval)
		{
			super(new OID(oid), MOAccessImpl.ACCESS_READ_ONLY, defval);
			if(attrProvider == null) throw new NullPointerException("attrProvider is null.");
			jmxAttribute = attrProvider;
			defaultValue = defval;
		}
		
		protected abstract T convert(Object value);
		
		@Override
		public final T getValue() {
			final Object result = jmxAttribute.getValue(null);
			return result == null ? defaultValue : convert(result);
		}
	}
	
	/**
	 * SNMP-переменная для целочисленного JMX-атрибута.
	 * @author rvsakno
	 *
	 */
	private static final class IntegerValueProvider extends ScalarValueProvider<Integer32>{
		
		public IntegerValueProvider(final String oid, final JmxMonitor.AttributeProvider attrProvider){
			super(oid, attrProvider, new Integer32(-1));
		}

		@Override
		protected Integer32 convert(Object value){
			return new Integer32(Integer.parseInt(value.toString()));
		}
	}
	
	private static final class BooleanValueProvider extends ScalarValueProvider<Integer32>{
		public BooleanValueProvider(final String oid, final JmxMonitor.AttributeProvider attrProvider){
			super(oid, attrProvider, new Integer32(-1));
		}

		@Override
		protected Integer32 convert(Object value){
			return new Integer32(Boolean.TRUE.equals(value) ? 1 : 0);
		}
	}
	
	private static final class StringValueProvider extends ScalarValueProvider<OctetString>{
		
		public StringValueProvider(final String oid, final JmxMonitor.AttributeProvider attrProvider){
			super(oid, attrProvider, new OctetString(""));
		}

		@Override
		protected OctetString convert(Object value){
			return new OctetString(value.toString());
		}
	}
	
	private static final class FloatValueProvider extends ScalarValueProvider<OctetString>{
		
		public FloatValueProvider(final String oid, final JmxMonitor.AttributeProvider attrProvider){
			super(oid, attrProvider, new OctetString("-1.0"));
		}

		@Override
		protected OctetString convert(Object value){
			return new OctetString(value.toString());
		}
	}
	
	private static boolean areEqual(final Object v1, final Object v2){
		return v1 == null ? v2 == null : v1.equals(v2);
	}
	
	private static boolean equalsToOneOf(final Object value, Object... values){
		for(final Object v: values)
			if(areEqual(value, v)) return true;
		return false;
	}

	public static ManagedObject createScalarValueProvider(final String oid, final JmxMonitor.AttributeProvider attributeProvider){
		if(attributeProvider == null) return null;
		final String typeName = attributeProvider.getAttributeClassName();
		//for all integers
		if(equalsToOneOf(typeName, 
				int.class.getCanonicalName(), Integer.class.getCanonicalName(), 
				byte.class.getCanonicalName(), Byte.class.getCanonicalName(),
				short.class.getCanonicalName(), Short.class.getCanonicalName()))
			return new IntegerValueProvider(oid, attributeProvider);
		//for boolean
		else if(equalsToOneOf(typeName, boolean.class.getCanonicalName(), Boolean.class.getCanonicalName()))
			return new BooleanValueProvider(oid, attributeProvider);
		//for float and double
		else if(equalsToOneOf(typeName, float.class.getCanonicalName(), Float.class.getCanonicalName(), double.class.getCanonicalName(), Double.class.getCanonicalName()))
			return new FloatValueProvider(oid, attributeProvider);
		else return new StringValueProvider(oid, attributeProvider);
	}
}
