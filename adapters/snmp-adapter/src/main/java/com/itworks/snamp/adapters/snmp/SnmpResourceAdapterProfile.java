package com.itworks.snamp.adapters.snmp;

import com.google.common.base.Supplier;
import com.itworks.snamp.adapters.profiles.BasicResourceAdapterProfile;
import com.itworks.snamp.jmx.WellKnownType;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.itworks.snamp.adapters.snmp.SnmpAdapterConfigurationDescriptor.*;

/**
 * Represents default profile for SNMP Resource Adapter.
 * Other profiles should derive from the default profile.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
class SnmpResourceAdapterProfile extends BasicResourceAdapterProfile implements SnmpTypeMapper {
    static final String PROFILE_NAME = DEFAULT_PROFILE_NAME;

    SnmpResourceAdapterProfile(final Map<String, String> parameters,
                                         final boolean defaultProfile) {
        super(parameters, defaultProfile);
    }

    /**
     * Clones this profile.
     *
     * @return A new cloned instance of this profile.
     */
    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public SnmpResourceAdapterProfile clone() {
        return new SnmpResourceAdapterProfile(this, isDefault());
    }

    private static SnmpType map(final WellKnownType type){
        if(type != null)
            switch (type){
                case BOOL: return SnmpType.BOOLEAN;
                case CHAR:
                case OBJECT_NAME:
                case STRING: return SnmpType.TEXT;
                case BIG_DECIMAL:
                case BIG_INT: return SnmpType.NUMBER;
                case BYTE:
                case INT:
                case SHORT: return SnmpType.INTEGER;
                case LONG: return SnmpType.LONG;
                case FLOAT:
                case DOUBLE: return SnmpType.FLOAT;
                case DATE: return SnmpType.UNIX_TIME;
                case BYTE_BUFFER:
                case SHORT_BUFFER:
                case CHAR_BUFFER:
                case INT_BUFFER:
                case LONG_BUFFER:
                case FLOAT_BUFFER:
                case DOUBLE_BUFFER: return SnmpType.BUFFER;
                case BYTE_ARRAY:
                case WRAPPED_BYTE_ARRAY:
                case BOOL_ARRAY:
                case WRAPPED_BOOL_ARRAY:
                    return SnmpType.BLOB;
                case FLOAT_ARRAY:
                case WRAPPED_FLOAT_ARRAY:
                case SHORT_ARRAY:
                case WRAPPED_SHORT_ARRAY:
                case INT_ARRAY:
                case WRAPPED_INT_ARRAY:
                case LONG_ARRAY:
                case WRAPPED_LONG_ARRAY:
                case DICTIONARY:
                case TABLE: return SnmpType.TABLE;
            }
        return SnmpType.FALLBACK;
    }

    static SnmpTypeMapper createDefaultTypeMapper(){
        return new SnmpTypeMapper() {
            @Override
            public SnmpType apply(final WellKnownType type) {
                return map(type);
            }
        };
    }

    @Override
    public SnmpType apply(final WellKnownType type) {
        return map(type);
    }

    /**
     * Creates a new instance of SNMP Agent.
     * @param contextFactory JNDI context factory. Cannot be {@literal null}.
     * @param threadPoolFactory Thread pool factory. Cannot be {@literal null}.
     * @return A new instance of SNMP Agent.
     * @throws IOException Unable to create an instance of SNMP Agent.
     * @throws SnmpAdapterAbsentParameterException One of the required parameters is missing.
     */
    SnmpAgent createSnmpAgent(final DirContextFactory contextFactory,
                                     final Supplier<ExecutorService> threadPoolFactory) throws IOException, SnmpAdapterAbsentParameterException {
        return new SnmpAgent(getContext(),
                getEngineID(),
                getPort(),
                getAddress(),
                getSecurityConfiguration(contextFactory),
                getSocketTimeout(),
                threadPoolFactory.get());
    }

    /**
     * Creates a new instance of thread pool factory.
     * @param adapterInstanceName The name of resource adapter instance.
     * @return A new instance of thread pool factory.
     */
    Supplier<ExecutorService> createThreadPoolFactory(final String adapterInstanceName){
        return new SnmpThreadPoolConfig(this, adapterInstanceName);
    }

    final int getSocketTimeout(){
        return parseSocketTimeout(this);
    }

    final SecurityConfiguration getSecurityConfiguration(final DirContextFactory contextFactory){
        return parseSecurityConfiguration(this, contextFactory);
    }

    final String getAddress(){
        return parseAddress(this);
    }

    final OID getContext() throws SnmpAdapterAbsentParameterException {
        return new OID(parseContext(this));
    }

    final OctetString getEngineID(){
        return parseEngineID(this);
    }

    final int getPort(){
        return parsePort(this);
    }

    final long getRestartTimeout(){
        return parseRestartTimeout(this);
    }

    static SnmpResourceAdapterProfile createDefault(final Map<String, String> parameters){
        return new SnmpResourceAdapterProfile(parameters, true);
    }
}
