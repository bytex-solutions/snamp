package com.itworks.snamp.connectors.snmp;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.reflect.TypeToken;
import com.itworks.snamp.ArrayUtils;
import com.itworks.snamp.mapping.TypeLiterals;
import com.itworks.snamp.connectors.WellKnownTypeSystem;
import org.snmp4j.smi.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;

/**
 * Represents SNMP type system.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SnmpTypeSystem extends WellKnownTypeSystem {


    private static final class TimeTicksType extends SnmpManagedEntityScalarType<TimeTicks> {
        public TimeTicksType(final TimeTicksConversionFormat format) {
            super(TimeTicks.class, SMIConstants.SYNTAX_TIMETICKS, format.createTypeProjection());
        }

        @Override
        protected TimeTicks convertToScalar(final Object value) throws InvalidSnmpValueException {
            if (value instanceof String) {
                final TimeTicks result = new TimeTicks();
                result.setValue((String) value);
                return result;
            } else if (value instanceof Long)
                return new TimeTicks((Long) value);
            else throw createInvalidValueException(value);
        }
    }

    private static final class IpAddressType extends SnmpManagedEntityScalarType<IpAddress> {
        public IpAddressType(final IpAddressConversionFormat format) {
            super(IpAddress.class, SMIConstants.SYNTAX_IPADDRESS, format.createTypeProjection());
        }

        @Override
        protected IpAddress convertToScalar(final Object value) throws InvalidSnmpValueException {
            if (value instanceof byte[])
                return new IpAddress((byte[]) value);
            else if (value instanceof Byte[])
                return new IpAddress(ArrayUtils.unboxArray((Byte[]) value));
            else if (value instanceof Object[])
                return convertToScalar(Arrays.copyOf((Object[]) value, Array.getLength(value), Byte[].class));
            else if (value instanceof String)
                return new IpAddress((String) value);
            else throw createInvalidValueException(value);
        }
    }

    private static final class ObjectIdentifierType extends SnmpManagedEntityScalarType<OID> {
        public ObjectIdentifierType(final OidConversionFormat format) {
            super(OID.class, SMIConstants.SYNTAX_OBJECT_IDENTIFIER, format.createTypeProjection());
        }

        @Override
        protected OID convertToScalar(final Object value) throws InvalidSnmpValueException {
            if (value instanceof int[])
                return new OID((int[]) value);
            else if (value instanceof Integer[])
                return new OID(ArrayUtils.unboxArray((Integer[]) value));
            else if (value instanceof Object[])
                return convertToScalar(Arrays.copyOf((Object[]) value, Array.getLength(value), Integer[].class));
            else if (value instanceof String)
                return new OID((String) value);
            else throw createInvalidValueException(value);
        }
    }

    private static final class OctetStringType extends SnmpManagedEntityScalarType<OctetString> {
        private final OctetStringConversionFormat format;

        public OctetStringType(final OctetStringConversionFormat conversionFormat) {
            super(OctetString.class, SMIConstants.SYNTAX_OCTET_STRING, conversionFormat.createTypeProjection());
            this.format = conversionFormat;
        }

        private static OctetString convertToScalar(final String value, final OctetStringConversionFormat format) {
            switch (format) {
                case HEX:
                    return OctetString.fromHexString(value);
                case TEXT:
                    return new OctetString(value);
                default:
                    return OctetString.fromByteArray(value.getBytes());
            }
        }

        @Override
        protected OctetString convertToScalar(final Object value) throws InvalidSnmpValueException {
            if (value instanceof byte[])
                return OctetString.fromByteArray((byte[]) value);
            else if (value instanceof Byte[])
                return OctetString.fromByteArray(ArrayUtils.unboxArray((Byte[]) value));
            else if (value instanceof Object[])
                return convertToScalar(Arrays.copyOf((Object[]) value, Array.getLength(value), Byte[].class));
            else if (value instanceof String)
                return convertToScalar((String) value, format);
            else if (value instanceof OctetString)
                return (OctetString) value;
            else throw createInvalidValueException(value);
        }
    }

    static final TypeToken<Integer32> INTEGER_32 = TypeToken.of(Integer32.class);
    static final TypeToken<Counter64> COUNTER_64 = TypeToken.of(Counter64.class);
    static final TypeToken<Gauge32> GAUGE_32 = TypeToken.of(Gauge32.class);
    static final TypeToken<UnsignedInteger32> UNSIGNED_INTEGER_32 = TypeToken.of(UnsignedInteger32.class);
    static final TypeToken<Opaque> OPAQUE = TypeToken.of(Opaque.class);

    SnmpTypeSystem() {
        registerConverter(INTEGER_32, TypeLiterals.INTEGER,
                new Function<Integer32, Integer>() {
                    @Override
                    public Integer apply(final Integer32 input) {
                        return input.toInt();
                    }
                });
        registerConverter(INTEGER_32, TypeLiterals.LONG,
                new Function<Integer32, Long>() {
                    @Override
                    public Long apply(final Integer32 input) {
                        return input.toLong();
                    }
                });
        registerConverter(COUNTER_64, TypeLiterals.LONG,
                new Function<Counter64, Long>() {
                    @Override
                    public Long apply(final Counter64 input) {
                        return input.toLong();
                    }
                });
        registerConverter(GAUGE_32, TypeLiterals.LONG,
                new Function<Gauge32, Long>() {
                    @Override
                    public Long apply(final Gauge32 input) {
                        return input.toLong();
                    }
                });
        registerConverter(UNSIGNED_INTEGER_32, TypeLiterals.LONG,
                new Function<UnsignedInteger32, Long>() {
                    @Override
                    public Long apply(final UnsignedInteger32 input) {
                        return input.toLong();
                    }
                });
        registerConverter(OPAQUE, TypeLiterals.OBJECT_ARRAY,
                new Function<Opaque, Object[]>() {
                    @Override
                    public Byte[] apply(final Opaque input) {
                        return ArrayUtils.boxArray(input.toByteArray());
                    }
                });
    }

    public SnmpManagedEntityType resolveSnmpScalarType(final Variable value, final Map<String, String> options) {
        //Opaque
        if (value instanceof Opaque)
            return createEntityType(new Supplier<SnmpManagedEntityScalarType<Opaque>>() {
                public SnmpManagedEntityScalarType<Opaque> get() {
                    return new SnmpManagedEntityScalarType<Opaque>(Opaque.class, SMIConstants.SYNTAX_OPAQUE) {
                        @Override
                        protected Opaque convertToScalar(final Object value) throws InvalidSnmpValueException {
                            if (value instanceof byte[])
                                return new Opaque((byte[]) value);
                            else if (value instanceof Byte[])
                                return new Opaque(ArrayUtils.unboxArray((Byte[]) value));
                            else if (value instanceof Object[])
                                return convertToScalar(Arrays.copyOf((Object[]) value, Array.getLength(value), Byte[].class));
                            else throw createInvalidValueException(value);
                        }
                    };
                }
            }, TypeLiterals.OBJECT_ARRAY, TypeLiterals.BYTE_ARRAY, TypeToken.of(byte[].class));
            //Octet string
        else if (value instanceof OctetString)
            return new OctetStringType(OctetStringConversionFormat.getFormat((OctetString) value, options));
            //Integer32
        else if (value instanceof Integer32)
            return createEntityType(new Supplier<SnmpManagedEntityScalarType<Integer32>>() {
                public SnmpManagedEntityScalarType<Integer32> get() {
                    return new SnmpManagedEntityScalarType<Integer32>(Integer32.class, SMIConstants.SYNTAX_INTEGER) {
                        @Override
                        protected Integer32 convertToScalar(final Object value) throws InvalidSnmpValueException {
                            if (value instanceof Integer)
                                return new Integer32((Integer) value);
                            else if (value instanceof Short)
                                return new Integer32((Short) value);
                            else if (value instanceof Byte)
                                return new Integer32((Byte) value);
                            else if (value instanceof Integer32)
                                return (Integer32) value;
                            else throw createInvalidValueException(value);
                        }
                    };
                }
            }, TypeLiterals.INTEGER);
            //Counter 32
        else if (value instanceof Counter32)
            return createEntityType(new Supplier<SnmpManagedEntityScalarType<Counter32>>() {
                public SnmpManagedEntityScalarType<Counter32> get() {
                    return new SnmpManagedEntityScalarType<Counter32>(Counter32.class, SMIConstants.SYNTAX_COUNTER32) {
                        @Override
                        protected Counter32 convertToScalar(final Object value) throws InvalidSnmpValueException {
                            if (value instanceof Long)
                                return new Counter32((Long) value);
                            else if (value instanceof Integer)
                                return new Counter32((Integer) value);
                            else if (value instanceof Short)
                                return new Counter32((Short) value);
                            else if (value instanceof Byte)
                                return new Counter32((Byte) value);
                            else if (value instanceof Counter32)
                                return (Counter32) value;
                            else throw createInvalidValueException(value);
                        }
                    };
                }
            }, TypeLiterals.LONG);
            //Counter 64
        else if (value instanceof Counter64)
            return createEntityType(new Supplier<SnmpManagedEntityScalarType<Counter64>>() {
                public SnmpManagedEntityScalarType<Counter64> get() {
                    return new SnmpManagedEntityScalarType<Counter64>(Counter64.class, SMIConstants.SYNTAX_COUNTER64) {
                        @Override
                        protected Counter64 convertToScalar(final Object value) throws InvalidSnmpValueException {
                            if (value instanceof Long)
                                return new Counter64((Long) value);
                            else if (value instanceof Integer)
                                return new Counter64((Integer) value);
                            else if (value instanceof Short)
                                return new Counter64((Short) value);
                            else if (value instanceof Byte)
                                return new Counter64((Byte) value);
                            else if (value instanceof Counter64)
                                return (Counter64) value;
                            else throw createInvalidValueException(value);
                        }
                    };
                }
            }, TypeLiterals.LONG);
            //Gauge32
        else if (value instanceof Gauge32)
            return createEntityType(new Supplier<SnmpManagedEntityScalarType<Gauge32>>() {
                public SnmpManagedEntityScalarType<Gauge32> get() {
                    return new SnmpManagedEntityScalarType<Gauge32>(Gauge32.class, SMIConstants.SYNTAX_GAUGE32) {
                        @Override
                        protected Gauge32 convertToScalar(final Object value) throws InvalidSnmpValueException {
                            if (value instanceof Long)
                                return new Gauge32((Long) value);
                            else if (value instanceof Integer)
                                return new Gauge32((Integer) value);
                            else if (value instanceof Short)
                                return new Gauge32((Short) value);
                            else if (value instanceof Byte)
                                return new Gauge32((Byte) value);
                            else if (value instanceof Gauge32)
                                return (Gauge32) value;
                            else throw createInvalidValueException(value);
                        }
                    };
                }
            }, TypeLiterals.LONG);
            //Time ticks
        else if (value instanceof TimeTicks)
            return new TimeTicksType(TimeTicksConversionFormat.getFormat(options));
            //UnsignedInteger32
        else if (value instanceof UnsignedInteger32)
            return createEntityType(new Supplier<SnmpManagedEntityScalarType<UnsignedInteger32>>() {
                public SnmpManagedEntityScalarType<UnsignedInteger32> get() {
                    return new SnmpManagedEntityScalarType<UnsignedInteger32>(UnsignedInteger32.class, SMIConstants.SYNTAX_UNSIGNED_INTEGER32) {
                        @Override
                        protected UnsignedInteger32 convertToScalar(final Object value) throws InvalidSnmpValueException {
                            if (value instanceof Long)
                                return new UnsignedInteger32((Long) value);
                            else if (value instanceof Integer)
                                return new UnsignedInteger32((Integer) value);
                            else if (value instanceof Short)
                                return new UnsignedInteger32((Short) value);
                            else if (value instanceof Byte)
                                return new UnsignedInteger32((Byte) value);
                            else if (value instanceof UnsignedInteger32)
                                return (UnsignedInteger32) value;
                            else throw createInvalidValueException(value);
                        }
                    };
                }
            }, TypeLiterals.LONG);
            //Object Identifier
        else if (value instanceof OID)
            return new ObjectIdentifierType(OidConversionFormat.getFormat(options));
            //Ip-address
        else if (value instanceof IpAddress)
            return new IpAddressType(IpAddressConversionFormat.getFormat(options));
        else return null;
    }
}
