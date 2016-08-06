package com.bytex.snamp.adapters.snmp;

import com.bytex.snamp.adapters.profiles.BasicResourceAdapterProfile;
import com.bytex.snamp.jmx.WellKnownType;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

import javax.naming.NamingException;
import java.io.IOException;
import java.util.Map;

/**
 * Represents default profile for SNMP Resource Adapter.
 * Other profiles should derive from the default profile.
 * @author Roman Sakno
 * @version 2.0
 * @since 1.0
 */
class SnmpResourceAdapterProfile extends BasicResourceAdapterProfile implements SnmpTypeMapper {
    static final String PROFILE_NAME = DEFAULT_PROFILE_NAME;
    private final SnmpAdapterDescriptionProvider configurationParser;

    SnmpResourceAdapterProfile(final Map<String, String> parameters,
                               final boolean defaultProfile) {
        super(parameters, defaultProfile);
        this.configurationParser = SnmpAdapterDescriptionProvider.getInstance();
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
        return SnmpResourceAdapterProfile::map;
    }

    @Override
    public SnmpType apply(final WellKnownType type) {
        return map(type);
    }

    /**
     * Creates a new instance of SNMP Agent.
     * @param contextFactory JNDI context factory. Cannot be {@literal null}.
     * @return A new instance of SNMP Agent.
     * @throws IOException Unable to create an instance of SNMP Agent.
     * @throws SnmpAdapterAbsentParameterException One of the required parameters is missing.
     * @throws NamingException Unable to import security settings from LDAP server.
     */
    SnmpAgent createSnmpAgent(final DirContextFactory contextFactory) throws IOException, SnmpAdapterAbsentParameterException, NamingException {
        return new SnmpAgent(getContext(),
                getEngineID(),
                getPort(),
                getAddress(),
                getSecurityConfiguration(contextFactory),
                getSocketTimeout(),
                configurationParser.getThreadPool(this));
    }

    private int getSocketTimeout(){
        return configurationParser.parseSocketTimeout(this);
    }

    private SecurityConfiguration getSecurityConfiguration(final DirContextFactory contextFactory) throws NamingException{
        return configurationParser.parseSecurityConfiguration(this, contextFactory);
    }

    private String getAddress(){
        return configurationParser.parseAddress(this);
    }

    final OID getContext() throws SnmpAdapterAbsentParameterException {
        return new OID(configurationParser.parseContext(this));
    }

    private OctetString getEngineID(){
        return configurationParser.parseEngineID(this);
    }

    private int getPort(){
        return configurationParser.parsePort(this);
    }

    final long getRestartTimeout(){
        return configurationParser.parseRestartTimeout(this);
    }

    static SnmpResourceAdapterProfile createDefault(final Map<String, String> parameters){
        return new SnmpResourceAdapterProfile(parameters, true);
    }
}
