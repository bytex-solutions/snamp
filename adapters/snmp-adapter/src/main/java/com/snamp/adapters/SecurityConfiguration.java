package com.snamp.adapters;

import static com.snamp.configuration.SnmpAdapterConfigurationDescriptor.*;

import com.snamp.internal.KeyValueParser;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.TransportStateReference;
import org.snmp4j.agent.mo.snmp.*;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.asn1.*;
import org.snmp4j.event.CounterEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.security.*;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.*;
import javax.naming.ldap.*;

/**
 * Represents security configuration of the SNMP adapter that is used
 * to setup SNMPv3 settings. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SecurityConfiguration {

    public static enum LdapAuthenticationType{
        SIMPLE("simple"),
        MD5("DIGEST-MD5"),
        KERBEROS("GSSAPI");
        private final String name;

        private LdapAuthenticationType(final String jndiName){
            this.name = jndiName;
        }

        public final void setupEnvironment(final Hashtable<String, ? super String> env){
            env.put(Context.SECURITY_AUTHENTICATION, name);
        }
    }

    /**
     * Represents SNMPv3 user. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class User{
        private static final KeyValueParser parser = new KeyValueParser("\\,", "\\=");
        private OID authenticationProtocol;
        private OID privacyProtocol;
        private String password;
        private String encryptionKey;
        private LdapAuthenticationType ldapAuthentication;

        /**
         * Initializes a new user security information.
         */
        public User(){
            authenticationProtocol = null;
            privacyProtocol = null;
            password = encryptionKey = "";
            ldapAuthentication = null;
        }

        public LdapAuthenticationType getLdapAuthenticationType(){
            return ldapAuthentication;
        }

        public void setLdapAuthenticationType(final LdapAuthenticationType value){
            ldapAuthentication = value;
        }

        /**
         * Sets authentication protocol that is used to authenticate this user.
         * @param protocolID Well-known identifier of the SNMPv3 authentication protocol.
         * @see org.snmp4j.security.AuthMD5#ID
         * @see org.snmp4j.security.AuthSHA#ID
         */
        public final void setAuthenticationProtocol(final OID protocolID){
            authenticationProtocol = protocolID;
        }

        /**
         * Sets authentication protocol implementation for this user.
         * @param protocol Authentication protocol implementation.
         */
        public final void setAuthenticationProtocolImpl(final SecurityProtocol protocol){
            setAuthenticationProtocol(protocol != null ? protocol.getID() : null);
        }

        public final OID getAuthenticationProtocol(){
            return authenticationProtocol;
        }

        public final AuthenticationProtocol getAuthenticationProtocolImpl(){
            return authenticationProtocol != null ?
                    SecurityProtocols.getInstance().getAuthenticationProtocol(authenticationProtocol) :
                    null;
        }

        public final void setPrivacyProtocol(final OID protocolID){
            privacyProtocol = protocolID;
        }

        public final OID getPrivacyProtocol(){
            return privacyProtocol;
        }

        public final void setPrivaceProtocolImpl(final SecurityProtocol protocol){
            setPrivacyProtocol(protocol != null ? protocol.getID() : null);
        }

        public final PrivacyProtocol getPrivacyProtocolImpl(){
            return SecurityProtocols.getInstance().getPrivacyProtocol(privacyProtocol);
        }

        public final void setPassword(final String password){
            this.password = password != null ? password : "";
        }

        public final String getPassword(){
            return password;
        }

        public final void setPrivacyProtocol(final String protocol) {
            if(protocol == null || protocol.isEmpty()) privacyProtocol = null;
            else switch (protocol){
                case "AES-128":
                case "aes-128":
                case "aes128":
                case "AES128": setPrivacyProtocol(PrivAES128.ID); return;
                case "AES-192":
                case "aes-192":
                case "aes192":
                case "AES192": setPrivacyProtocol(PrivAES192.ID);return;
                case "AES-256":
                case "aes-256":
                case "aes256":
                case "AES256": setPrivacyProtocol(PrivAES256.ID); return;
                case "DES":
                case "des": setPrivacyProtocol(PrivDES.ID); return;
                case "3DES":
                case "3des":
                case "3-DES":
                case "3-des": setPrivacyProtocol(Priv3DES.ID); return;
                default: setPrivacyProtocol(new OID(protocol)); return;
            }
        }

        public final void setAuthenticationProtocol(final String protocol) {
            if(protocol == null || protocol.isEmpty()) authenticationProtocol = null;
            else switch (protocol.replace(" ", "").toLowerCase()){
                case "md5":
                case "md-5": setAuthenticationProtocol(AuthMD5.ID); return;
                case "sha": setAuthenticationProtocol(AuthSHA.ID); return;
                case "snmp=md5,ldap=simple":
                    setAuthenticationProtocol(AuthMD5.ID);
                    setLdapAuthenticationType(LdapAuthenticationType.SIMPLE);
                    return;
                case "snmp=md5,ldap=md5":
                    setAuthenticationProtocol(AuthMD5.ID);
                    setLdapAuthenticationType(LdapAuthenticationType.MD5);
                    return;
                case "snmp=m5,ldap=kerberos":
                    setAuthenticationProtocol(AuthMD5.ID);
                    setLdapAuthenticationType(LdapAuthenticationType.KERBEROS);
                    return;
                case "snmp=sha,ldap=simple":
                    setAuthenticationProtocol(AuthSHA.ID);
                    setLdapAuthenticationType(LdapAuthenticationType.SIMPLE);
                    return;
                case "snmp=sha,ldap=md5":
                    setAuthenticationProtocol(AuthSHA.ID);
                    setLdapAuthenticationType(LdapAuthenticationType.MD5);
                    return;
                case "snmp=sha,ldap=kerberos":
                    setAuthenticationProtocol(AuthSHA.ID);
                    setLdapAuthenticationType(LdapAuthenticationType.KERBEROS);
                    return;
                default:
                    //attempts to parse key-value pair in format
                    authenticationProtocol = new OID(protocol); return;
            }
        }

        /**
         * Gets passphrase that is used to encrypt SNMPv3 traffic.
         * @return The passphrase that is used to encrypt SNMPv3 traffic.
         */
        public final String getPrivacyKey() {
            return encryptionKey;
        }

        /**
         * Sets passphrase that is used to encrypt SNMPv3 traffic.
         * @param passphrase The passphrase that is used to encrypt SNMPv3 traffic.
         */
        public final void setPrivacyKey(final String passphrase){
            encryptionKey = passphrase;
        }

        public final OctetString getPasswordAsOctectString() {
            return password == null || password.isEmpty() ? null : new OctetString(password);
        }

        public final OctetString getPrivacyKeyAsOctetString(){
            return encryptionKey == null || encryptionKey.isEmpty() ? null : new OctetString(encryptionKey);
        }
    }

    /**
     * Represents MIB access rights.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static enum AccessRights{
        /**
         * MO's value be obtained by SNMP manager.
         */
        READ,

        /**
         * MO's value can be overwritten by SNMP manager.
         */
        WRITE,

        /**
         * SNMP manager can receive SNMP traps.
         */
        NOTIFY;
    }

    /**
     * Represents group of users. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class UserGroup extends HashMap<String, User>{
        private SecurityLevel level;
        private final EnumSet<AccessRights> rights;

        /**
         * Initializes a new empty user group.
         */
        public UserGroup(){
            level = SecurityLevel.noAuthNoPriv;
            rights = EnumSet.noneOf(AccessRights.class);
        }

        /**
         * Gets security level applied to all users in this group.
         * @return Security level applied to all users in this group.
         */
        public final SecurityLevel getSecurityLevel(){
            return level;
        }

        /**
         * Sets security level for all users in this group.
         * @param value Security level for all users in this group.
         */
        public final void setSecurityLevel(final SecurityLevel value){
            level = value;
        }

        public final void setSecurityLevel(final String value){
            setSecurityLevel((value == null || value.isEmpty()) ? SecurityLevel.noAuthNoPriv : SecurityLevel.valueOf(value));
        }

        /**
         * Sets MIB access rights to all users in this group.
         * @param rights MIB access rights of all users in this group.
         */
        public final void setAccessRights(final Collection<AccessRights> rights){
            this.rights.clear();
            this.rights.addAll(rights);
        }

        public final boolean hasAccessRights(final Collection<AccessRights> rights){
            return this.rights.containsAll(rights);
        }

        public final boolean hasAccessRights(final AccessRights... rights){
            return hasAccessRights(Arrays.asList(rights));
        }

        /**
         * Sets MIB access rights to all users in this group.
         * @param rights MIB access rights of all users in this group.
         */
        public final void setAccessRights(final AccessRights... rights){
            setAccessRights(Arrays.asList(rights));
        }

        /**
         * Gets MIB access rights available for all users in this group.
         * @return MIB access rights.
         */
        public final Set<AccessRights> getAccessRights(){
            return EnumSet.copyOf(rights);
        }

        public final void setAccessRights(final Iterable<String> rights){
            this.rights.clear();
            for(final String r: rights)
                this.rights.add(AccessRights.valueOf(r.toUpperCase()));
        }

        public final void setAccessRights(final String rights) {
            setAccessRights(splitAndTrim(rights, ";"));
        }
    }

    private final OctetString securityEngineID;
    private final Map<String, UserGroup> groups;
    private String ldapUri;

    /**
     * Initializes a new empty security configuration.
     * @param securityEngine Security engine ID (authoritative engine).
     */
    public SecurityConfiguration(final byte[] securityEngine){
        this.securityEngineID = new OctetString(securityEngine);
        this.groups = new HashMap<>(10);
    }

    /**
     * Determines whether this security configuration uses LDAP.
     * @return {@literal true}, if this security configuration uses LDAP; otherwise, {@literal false}.
     */
    public final boolean useLdap(){
        return ldapUri != null && ldapUri.length() > 0;
    }

    /**
     * Sets LDAP URI in the following format: ldap://address-or-host:port.
     * @param uri LDAP server URI.
     */
    public final void setLdapUri(final String uri){
        ldapUri = uri;
    }

    /**
     * Gets LDAP URI.
     * @return LDAP server URI.
     */
    public final String getLdapUri(){
        return ldapUri;
    }

    public final boolean addGroup(final String groupName, final UserGroup group){
        if(groups.containsKey(groupName)) return false;
        groups.put(groupName, group);
        return true;
    }

    public final boolean removeGroup(final String groupName){
        return groups.remove(groupName) != null;
    }

    public final boolean containsGroup(final String groupName){
        return groups.containsKey(groupName);
    }

    private static final Collection<String> splitAndTrim(final String value, final String separator){
        final String[] result = value.split(separator);
        for(int i = 0; i < result.length; i++)
            result[i] = result[i].trim();
        return Arrays.asList(result);
    }


    private static void fillGroups(final Map<String, String> adapterSettings, final Iterable<String> groups, final Map<String, UserGroup> output){
        final String SECURITY_LEVEL_TEMPLATE = "%s-security-level";
        final String ACCESS_RIGHTS_TEMPLATE = "%s-access-rights";
        final String USERS_TEMPLATE = "%s-users";
        for(final String groupName: groups){
            final UserGroup groupInfo = new UserGroup();
            output.put(groupName, groupInfo);
            //process group's security level
            groupInfo.setSecurityLevel(adapterSettings.get(String.format(SECURITY_LEVEL_TEMPLATE, groupName)));
            //process group's access rights
            groupInfo.setAccessRights(adapterSettings.get(String.format(ACCESS_RIGHTS_TEMPLATE, groupName)));
            fillUsers(adapterSettings, groupInfo, splitAndTrim(adapterSettings.get(String.format(USERS_TEMPLATE, groupName)), ";"));
        }
    }

    private static void fillUsers(final Map<String,String> adapterSettings, final UserGroup groupInfo, final Collection<String> userNames) {
        final String PASSWORD_TEMPLATE = "%s-password";
        final String AUTH_PROTOCOL_TEMPLATE = "%s-auth-protocol";
        final String PRIVACY_KEY_TEMPLATE = "%s-privacy-key";
        final String PRIVACY_PROTOCOL_TEMPLATE = "%s-privacy-protocol";
        for(final String name: userNames){
            final User userInfo = new User();
            groupInfo.put(name, userInfo);
            userInfo.setPassword(adapterSettings.get(String.format(PASSWORD_TEMPLATE, name)));
            userInfo.setPrivacyProtocol(adapterSettings.get(String.format(PRIVACY_PROTOCOL_TEMPLATE, name)));
            userInfo.setPrivacyKey(adapterSettings.get(String.format(PRIVACY_KEY_TEMPLATE, name)));
            userInfo.setAuthenticationProtocol(adapterSettings.get(String.format(AUTH_PROTOCOL_TEMPLATE, name)));
        }
    }

    public final boolean read(final Map<String, String> adapterSettings){
        if(adapterSettings.containsKey(LDAP_URI_PROPERTY))
            setLdapUri(adapterSettings.get(LDAP_URI_PROPERTY));
        if(adapterSettings.containsKey(SNMPv3_GROUPS_PROPERTY)){
            fillGroups(adapterSettings, splitAndTrim(adapterSettings.get(SNMPv3_GROUPS_PROPERTY), ";"), groups);
            return true;
        }
        else return false;
    }


    public static interface UserSelector{
        boolean match(final String userName, final User user, final UserGroup owner);
    }

    public final Map<String, User> findUsers(final UserSelector selector){
        final Map<String, User> result = new HashMap<>(10);
        for(final UserGroup group: groups.values())
            for(final Map.Entry<String, User> user: group.entrySet())
                if(selector.match(user.getKey(), user.getValue(), group))
                    result.put(user.getKey(), user.getValue());
        return result;
    }

    public static UserSelector createUserSelector(final AccessRights... rights){
        return new UserSelector() {
            @Override
            public boolean match(final String userName, final User user, final UserGroup owner) {
                return owner.hasAccessRights(rights);
            }
        };
    }

    public final String findFirstUser(final UserSelector selector){
        for(final UserGroup group: groups.values())
            for(final Map.Entry<String, User> user: group.entrySet())
                if(selector.match(user.getKey(), user.getValue(), group))
                    return user.getKey();
        return null;
    }

    public final SecurityLevel getUserSecurityLevel(final String userName){
        for(final UserGroup group: groups.values())
            for(final String lookup: group.keySet())
                if(Objects.equals(userName, lookup)) return group.getSecurityLevel();
        return null;
    }

    public final Set<AccessRights> getUserAccessRights(final String userName){
        for(final UserGroup group: groups.values())
            for(final String lookup: group.keySet())
                if(Objects.equals(userName, lookup)) return group.getAccessRights();
        return null;
    }

    public final User getUserByName(final String userName){
        for(final UserGroup group: groups.values())
            for(final String lookup: group.keySet())
                if(Objects.equals(userName, lookup)) return group.get(userName);
        return null;
    }

    public final Set<String> getAllUsers(){
        final Set<String> result = new HashSet<>(15);
        for(final UserGroup group: groups.values())
            for(final String lookup: group.keySet())
                result.add(lookup);
        return result;
    }

    private static final class LdapUsmUser extends UsmUser{
        private final LdapAuthenticationType ldapAuthenticationType;

        public LdapUsmUser(final OctetString securityName,
                           final OID authenticationProtocol,
                           final OctetString authenticationPassphrase,
                           final OID privacyProtocol,
                           final OctetString privacyPassphrase,
                           final LdapAuthenticationType ldapAuthType){
            super(securityName, authenticationProtocol, authenticationPassphrase, privacyProtocol, privacyPassphrase);
            ldapAuthenticationType = ldapAuthType;
        }


        public String getUserName() {
            return getSecurityName().toString();
        }



        public String getPassword() {
            final OctetString password = getAuthenticationPassphrase();
            return password != null ? password.toString() : "";
        }


        public LdapAuthenticationType getAuthenticationType() {
            return ldapAuthenticationType;
        }
    }

    public final void setupUserBasedSecurity(final USM security){
        for(final UserGroup group: groups.values())
            for(final Map.Entry<String, User> user: group.entrySet()){
                final OctetString userName = new OctetString(user.getKey());
                final User userDef = user.getValue();
                if(userDef.getLdapAuthenticationType() != null && useLdap())
                    security.addUser(userName, securityEngineID,
                            new LdapUsmUser(userName,
                                    userDef.getAuthenticationProtocol(),
                                    userDef.getPasswordAsOctectString(),
                                    userDef.getPrivacyProtocol(),
                                    userDef.getPrivacyKeyAsOctetString(),
                                    userDef.getLdapAuthenticationType()
                            ));
                else security.addUser(userName, securityEngineID,
                        new UsmUser(userName,
                                userDef.getAuthenticationProtocol(),
                                userDef.getPasswordAsOctectString(),
                                userDef.getPrivacyProtocol(),
                                userDef.getPrivacyKeyAsOctetString()));
            }
    }

    public final void setupViewBasedAcm(final VacmMIB vacm){
        for(final Map.Entry<String, UserGroup> group: groups.entrySet()){
            final UserGroup groupDef = group.getValue();
            for(final Map.Entry<String, User> user: groupDef.entrySet()){
                vacm.addGroup(SecurityModel.SECURITY_MODEL_USM, new OctetString(user.getKey()),
                        new OctetString(group.getKey()),
                        StorageType.nonVolatile);
            }
            vacm.addAccess(new OctetString(group.getKey()), new OctetString(),
                    SecurityModel.SECURITY_MODEL_USM, groupDef.getSecurityLevel().getSnmpValue(),
                    MutableVACM.VACM_MATCH_EXACT,
                    groupDef.hasAccessRights(AccessRights.READ) ? new OctetString("fullReadView") : null,
                    groupDef.hasAccessRights(AccessRights.WRITE) ? new OctetString("fullWriteView") : null,
                    groupDef.hasAccessRights(AccessRights.NOTIFY) ? new OctetString("fullNotifyView") : null,
                    StorageType.nonVolatile);
        }
    }

    private static final class LdapUSM extends USM{
        private final String ldapUri;
        private final Logger logger;

        public LdapUSM(final SecurityProtocols securityProtocols,
                   final OctetString localEngineID, final int engineBoots,
                   final String ldapUri) {
            super(securityProtocols, localEngineID, engineBoots);
            if(ldapUri == null || ldapUri.isEmpty()) throw new IllegalArgumentException("LDAP server URI is not specified");
            this.ldapUri = ldapUri;
            logger = SnmpHelpers.getLogger();
        }

        private OctetString getSecurityName(final OctetString engineID,
                                            final OctetString userName) {
            if (userName.length() == 0) return userName;
            UsmUserEntry user = getUserTable().getUser(engineID, userName);
            if (user  == null && isEngineDiscoveryEnabled())
                user = getUserTable().getUser(userName);
            return user != null ? user.getUsmUser().getSecurityName() : null;
        }

        /**
         * Overriding of this method inserts LDAP authentication.
         * @param snmpVersion
         * @param maxMessageSize
         * @param securityParameters
         * @param securityModel
         * @param securityLevel
         * @param wholeMsg
         * @param tmStateReference
         * @param securityEngineID
         * @param securityName
         * @param scopedPDU
         * @param maxSizeResponseScopedPDU
         * @param securityStateReference
         * @param statusInfo
         * @return
         * @throws IOException
         */
        @Override
        public final int processIncomingMsg(final int snmpVersion, final int maxMessageSize, final SecurityParameters securityParameters, final SecurityModel securityModel, final int securityLevel, final BERInputStream wholeMsg, final TransportStateReference tmStateReference, final OctetString securityEngineID, final OctetString securityName, final BEROutputStream scopedPDU, final Integer32 maxSizeResponseScopedPDU, final SecurityStateReference securityStateReference, final StatusInformation statusInfo) throws IOException {
            UsmSecurityParameters usmSecurityParameters =
                    (UsmSecurityParameters) securityParameters;
            UsmSecurityStateReference usmSecurityStateReference =
                    (UsmSecurityStateReference) securityStateReference;
            securityEngineID.setValue(usmSecurityParameters.getAuthoritativeEngineID());

            byte[] message = buildMessageBuffer(wholeMsg);

            if ((securityEngineID.length() == 0) ||
                    (getTimeTable().checkEngineID(securityEngineID,
                            isEngineDiscoveryEnabled()) !=
                            SnmpConstants.SNMPv3_USM_OK)) {
                // generate report
                logger.config("RFC3414 §3.2.3 Unknown engine ID: " + securityEngineID.toHexString());

                securityEngineID.setValue(usmSecurityParameters.getAuthoritativeEngineID());
                securityName.setValue(usmSecurityParameters.getUserName().getValue());

                if (statusInfo != null) {
                    CounterEvent event = new CounterEvent(this,
                            SnmpConstants.
                                    usmStatsUnknownEngineIDs);
                    fireIncrementCounter(event);
                    statusInfo.setSecurityLevel(new Integer32(securityLevel));
                    statusInfo.setErrorIndication(new VariableBinding(event.getOid(),
                            event.getCurrentValue()));
                }
                return SnmpConstants.SNMPv3_USM_UNKNOWN_ENGINEID;
            }

            securityName.setValue(usmSecurityParameters.getUserName().getValue());

            int scopedPDUPosition = usmSecurityParameters.getScopedPduPosition();

            // get security name
            if ((usmSecurityParameters.getUserName().length() > 0) ||
                    (securityLevel > SecurityLevel.NOAUTH_NOPRIV)) {
                OctetString secName = getSecurityName(securityEngineID, usmSecurityParameters.getUserName());
                if (secName == null) {
                        logger.config("RFC3414 §3.2.4 Unknown security name: " +
                                securityName.toHexString());
                    if (statusInfo != null) {
                        CounterEvent event = new CounterEvent(this,
                                SnmpConstants.usmStatsUnknownUserNames);
                        fireIncrementCounter(event);
                        statusInfo.setSecurityLevel(new Integer32(SecurityLevel.NOAUTH_NOPRIV));
                        statusInfo.setErrorIndication(new VariableBinding(event.getOid(),
                                event.getCurrentValue()));
                    }
                    return SnmpConstants.SNMPv3_USM_UNKNOWN_SECURITY_NAME;
                }
            }
            else {
                logger.config("Accepting zero length security name");
                securityName.setValue(new byte[0]);
            }

            if ((usmSecurityParameters.getUserName().length() > 0) ||
                    (securityLevel > SecurityLevel.NOAUTH_NOPRIV)) {
                UsmUserEntry user = getUser(securityEngineID, securityName);
                if (user == null) {
                    logger.config("RFC3414 §3.2.4 Unknown security name: " +
                                securityName.toHexString()+ " for engine ID "+
                                securityEngineID.toHexString());
                    CounterEvent event =
                            new CounterEvent(this, SnmpConstants.usmStatsUnknownUserNames);
                    fireIncrementCounter(event);
                    if (statusInfo != null) {
                        if (SNMP4JSettings.getReportSecurityLevelStrategy() ==
                                SNMP4JSettings.ReportSecurityLevelStrategy.noAuthNoPrivIfNeeded) {
                            statusInfo.setSecurityLevel(new Integer32(SecurityLevel.NOAUTH_NOPRIV));
                        }
                        statusInfo.setErrorIndication(new VariableBinding(event.getOid(),
                                event.getCurrentValue()));
                    }
                    return SnmpConstants.SNMPv3_USM_UNKNOWN_SECURITY_NAME;
                }

                usmSecurityStateReference.setUserName(user.getUserName().getValue());

                AuthenticationProtocol auth =
                        getSecurityProtocols().getAuthenticationProtocol(
                                user.getUsmUser().getAuthenticationProtocol());
                PrivacyProtocol priv =
                        getSecurityProtocols().getPrivacyProtocol(
                                user.getUsmUser().getPrivacyProtocol());

                usmSecurityStateReference.setAuthenticationKey(user.getAuthenticationKey());
                usmSecurityStateReference.setPrivacyKey(user.getPrivacyKey());
                usmSecurityStateReference.setAuthenticationProtocol(auth);
                usmSecurityStateReference.setPrivacyProtocol(priv);
                if (((securityLevel >= SecurityLevel.AUTH_NOPRIV) && (auth == null)) ||
                        (((securityLevel >= SecurityLevel.AUTH_PRIV) && (priv == null)))) {
                    logger.config("RFC3414 §3.2.5 - Unsupported security level: " +
                                securityLevel + " by user "+user);

                    CounterEvent event =
                            new CounterEvent(this, SnmpConstants.usmStatsUnsupportedSecLevels);
                    fireIncrementCounter(event);
                    if (SNMP4JSettings.getReportSecurityLevelStrategy() ==
                            SNMP4JSettings.ReportSecurityLevelStrategy.noAuthNoPrivIfNeeded) {
                        statusInfo.setSecurityLevel(new Integer32(SecurityLevel.NOAUTH_NOPRIV));
                    }
                    statusInfo.setErrorIndication(new VariableBinding(event.getOid(),
                            event.getCurrentValue()));
                    return SnmpConstants.SNMPv3_USM_UNSUPPORTED_SECURITY_LEVEL;
                }
                if (securityLevel >= SecurityLevel.AUTH_NOPRIV) {
                    if (statusInfo != null) {
                        int authParamsPos =
                                usmSecurityParameters.getAuthParametersPosition() +
                                        usmSecurityParameters.getSecurityParametersPosition();
                        final LdapAuthenticationType ldapAuthenticationType =
                            user.getUsmUser() instanceof LdapUsmUser ?
                                    ((LdapUsmUser)user.getUsmUser()).getAuthenticationType():
                                    LdapAuthenticationType.SIMPLE;
                        boolean authentic = authenticate(user.getUserName(), user.getUsmUser().getAuthenticationPassphrase(), ldapAuthenticationType);
                        if (!authentic) {
                            CounterEvent event =
                                    new CounterEvent(this, SnmpConstants.usmStatsWrongDigests);
                            fireIncrementCounter(event);
                            if (SNMP4JSettings.getReportSecurityLevelStrategy() ==
                                    SNMP4JSettings.ReportSecurityLevelStrategy.noAuthNoPrivIfNeeded) {
                                statusInfo.setSecurityLevel(new Integer32(SecurityLevel.NOAUTH_NOPRIV));
                            }
                            statusInfo.setErrorIndication(new VariableBinding(event.getOid(),
                                    event.getCurrentValue()));
                            return SnmpConstants.SNMPv3_USM_AUTHENTICATION_FAILURE;
                        }
                        // check time
                        int status = getTimeTable().checkTime(new UsmTimeEntry(securityEngineID,
                                usmSecurityParameters.getAuthoritativeEngineBoots(),
                                usmSecurityParameters.getAuthoritativeEngineTime()));

                        switch (status) {
                            case SnmpConstants.SNMPv3_USM_NOT_IN_TIME_WINDOW: {
                                logger.config("RFC3414 §3.2.7.a Not in time window; engineID='" +
                                        securityEngineID +
                                        "', engineBoots=" +
                                        usmSecurityParameters.getAuthoritativeEngineBoots() +
                                        ", engineTime=" +
                                        usmSecurityParameters.getAuthoritativeEngineTime());
                                CounterEvent event =
                                        new CounterEvent(this, SnmpConstants.usmStatsNotInTimeWindows);
                                fireIncrementCounter(event);
                                statusInfo.setSecurityLevel(new Integer32(SecurityLevel.AUTH_NOPRIV));
                                statusInfo.setErrorIndication(new VariableBinding(event.getOid(),
                                        event.getCurrentValue()));
                                return status;
                            }
                            case SnmpConstants.SNMPv3_USM_UNKNOWN_ENGINEID: {
                                logger.config("RFC3414 §3.2.7.b - Unkown engine ID: " +
                                            securityEngineID);
                                CounterEvent event =
                                        new CounterEvent(this, SnmpConstants.usmStatsUnknownEngineIDs);
                                fireIncrementCounter(event);
                                if (SNMP4JSettings.getReportSecurityLevelStrategy() ==
                                        SNMP4JSettings.ReportSecurityLevelStrategy.noAuthNoPrivIfNeeded) {
                                    statusInfo.setSecurityLevel(new Integer32(SecurityLevel.NOAUTH_NOPRIV));
                                }
                                statusInfo.setErrorIndication(new VariableBinding(event.getOid(),
                                        event.getCurrentValue()));
                                return status;

                            }
                        }
                    }
                    if (securityLevel >= SecurityLevel.AUTH_PRIV) {
                        OctetString privParams = usmSecurityParameters.getPrivacyParameters();
                        DecryptParams decryptParams = new DecryptParams(privParams.getValue(),
                                0, privParams.length());
                        try {
                            int scopedPDUHeaderLength = message.length - scopedPDUPosition;
                            ByteBuffer bis = ByteBuffer.wrap(message, scopedPDUPosition,
                                    scopedPDUHeaderLength);
                            BERInputStream scopedPDUHeader = new BERInputStream(bis);
                            long headerStartingPosition = scopedPDUHeader.getPosition();
                            int scopedPDULength =
                                    BER.decodeHeader(scopedPDUHeader, new BER.MutableByte());
                            int scopedPDUPayloadPosition =
                                    scopedPDUPosition +
                                            (int)(scopedPDUHeader.getPosition() - headerStartingPosition);
                            scopedPDUHeader.close();
                            // early release pointer:
                            scopedPDUHeader = null;
                            byte[] scopedPduBytes =
                                    priv.decrypt(message, scopedPDUPayloadPosition, scopedPDULength,
                                            user.getPrivacyKey(),
                                            usmSecurityParameters.getAuthoritativeEngineBoots(),
                                            usmSecurityParameters.getAuthoritativeEngineTime(),
                                            decryptParams);
                            ByteBuffer buf = ByteBuffer.wrap(scopedPduBytes);
                            scopedPDU.setFilledBuffer(buf);
                        }
                        catch (Exception ex) {
                            logger.config("RFC 3414 §3.2.8 Decryption error: "+ex.getMessage());
                            return SnmpConstants.SNMPv3_USM_DECRYPTION_ERROR;
                        }
                    }
                    else {
                        int scopedPduLength = message.length - scopedPDUPosition;
                        ByteBuffer buf =
                                ByteBuffer.wrap(message, scopedPDUPosition, scopedPduLength);
                        scopedPDU.setFilledBuffer(buf);
                    }
                }
                else {
                    int scopedPduLength = message.length - scopedPDUPosition;
                    ByteBuffer buf =
                            ByteBuffer.wrap(message, scopedPDUPosition, scopedPduLength);
                    scopedPDU.setFilledBuffer(buf);
                }
            }
            else {
                int scopedPduLength = message.length - scopedPDUPosition;
                ByteBuffer buf =
                        ByteBuffer.wrap(message, scopedPDUPosition, scopedPduLength);
                scopedPDU.setFilledBuffer(buf);
            }
            // compute real max size response pdu according  to RFC3414 §3.2.9
            int maxSecParamsOverhead =
                    usmSecurityParameters.getBERMaxLength(securityLevel);
            maxSizeResponseScopedPDU.setValue(maxMessageSize -
                    maxSecParamsOverhead);

            usmSecurityStateReference.setSecurityName(securityName.getValue());
            return SnmpConstants.SNMPv3_USM_OK;
        }

        private boolean authenticate(final String userName, final String password, final LdapAuthenticationType authType){
            final Hashtable<String, Object> env = new Hashtable<>(7);
            authType.setupEnvironment(env);
            env.put(Context.SECURITY_PRINCIPAL, userName);
            env.put(Context.SECURITY_CREDENTIALS, password);
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapUri);

            //ensures that objectSID attribute values
            //will be returned as a byte[] instead of a String
            env.put("java.naming.ldap.attributes.binary", "objectSID");

            // the following is helpful in debugging errors
            //env.put("com.sun.jndi.ldap.trace.ber", System.err);
            try {
                final LdapContext ctx = new InitialLdapContext(env, null);
                logger.fine(String.format("User %s is authenticated successfully on LDAP %s", userName, ldapUri));
                ctx.close();
                return true;
            }
            catch (final NamingException e) {
                logger.log(Level.WARNING,
                        String.format("Failed to authenticate %s user on LDAP %s", userName, ldapUri),
                        e);
                return false;
            }
        }

        private boolean authenticate(final OctetString userName, final OctetString authenticationKey, final LdapAuthenticationType authType) {
            return authenticate(userName.toString(), authenticationKey.toString(), authType);
        }
    }

    public USM createUserBasedSecurityModel(final SecurityProtocols protocols, final OctetString contextEngineID, final int engineBoots){
        return useLdap() ? new LdapUSM(protocols, contextEngineID, engineBoots, getLdapUri()) : new USM(protocols, contextEngineID, engineBoots);
    }
}
