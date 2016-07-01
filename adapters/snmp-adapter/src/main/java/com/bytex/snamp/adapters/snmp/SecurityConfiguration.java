package com.bytex.snamp.adapters.snmp;

import com.google.common.base.Splitter;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.agent.security.MutableVACM;
import org.snmp4j.security.*;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.*;

import static com.bytex.snamp.adapters.snmp.helpers.OctetStringHelper.SNMP_ENCODING;
import static com.bytex.snamp.adapters.snmp.helpers.OctetStringHelper.toOctetString;

/**
 * Represents security configuration of the SNMP adapter that is used
 * to setup SNMPv3 settings. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.0
 */
final class SecurityConfiguration {
    private static final Splitter SEMICOLON_SPLITTER = Splitter.on(';').trimResults().omitEmptyStrings();

    /**
     * Represents configuration property that provides a set of user groups.
     */
    static final String SNMPv3_GROUPS_PARAM = "snmpv3-groups";

    /**
     * Represents LDAP server URI.
     */
    static final String LDAP_URI_PARAM = "ldap-uri";

    /**
     * Represents LDAP DN of the admin user that is used to read security configuration structure.
     */
    static final String LDAP_ADMINDN_PARAM = "ldap-user";

    /**
     * Represents LDAP admin user password.
     */
    static final String LDAP_ADMIN_PASSWORD_PARAM = "ldap-password";

    /**
     * Represents type of the LDAP authentication.
     */
    static final String LDAP_ADMIN_AUTH_TYPE_PARAM = "ldap-auth-protocol";

    /**
     * Represents user search filter template that is used to find users in the group.
     * <p>
     *     $GROUPNAME$ string inside of the filter will be replaced with group name.
     * </p>
     */
    static final String LDAP_USER_SEARCH_FILTER_PARAM = "ldap-user-search-filter";

    /**
     * Represents semicolon delimiter string of group DNs.
     */
    static final String LDAP_GROUPS_PARAM = "ldap-groups";

    /**
     * Represents search base DN.
     */
    static final String LDAP_BASE_DN_PARAM = "ldap-base-dn";

    /**
     * Represents JNDI/LDAP factory name.
     * <p>
     *     By default, this property equals to com.sun.jndi.ldap.LdapCtxFactory.
     * </p>
     */
    private static final String JNDI_LDAP_FACTORY_PARAM = "jndi-ldap-factory";

    /**
     * Represents name of the attribute in directory that holds the user attribute as release text.
     */
    private static final String LDAP_PASSWORD_HOLDER_PARAM = "ldap-user-password-attribute-name";

    private static final class DefaultDirContextFactory implements DirContextFactory {
        @Override
        public DirContext create(final Hashtable<String, ?> env) throws NamingException {
            return new InitialDirContext(env);
        }
    }

    private enum LdapAuthenticationType{
        NONE("none"),
        SIMPLE("simple"),
        MD5("DIGEST-MD5"),
        KERBEROS("GSSAPI");
        private final String name;

        LdapAuthenticationType(final String jndiName){
            this.name = jndiName;
        }

        public final void setupEnvironment(final Hashtable<String, ? super String> env){
            env.put(Context.SECURITY_AUTHENTICATION, name);
        }

        public static LdapAuthenticationType parse(final String authType) {
            if(authType == null || authType.isEmpty()) return SIMPLE;
            for(final LdapAuthenticationType type: values())
                if(Objects.equals(authType, type.name)) return type;
            return SIMPLE;
        }
    }
    /**
     * Represents SNMPv3 user. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.2
     */
    public static final class User{
        private OID authenticationProtocol;
        private OID privacyProtocol;
        private String password;
        private String encryptionKey;

        /**
         * Initializes a new user security information.
         */
        User(){
            authenticationProtocol = null;
            privacyProtocol = null;
            password = encryptionKey = "";
        }

        /**
         * Sets authentication protocol that is used to authenticate this user.
         * @param protocolID Well-known identifier of the SNMPv3 authentication protocol.
         * @see org.snmp4j.security.AuthMD5#ID
         * @see org.snmp4j.security.AuthSHA#ID
         */
        void setAuthenticationProtocol(final OID protocolID){
            authenticationProtocol = protocolID;
        }

        OID getAuthenticationProtocol(){
            return authenticationProtocol;
        }

        void setPrivacyProtocol(final OID protocolID){
            privacyProtocol = protocolID;
        }

        OID getPrivacyProtocol(){
            return privacyProtocol;
        }

        void setPassword(final String password){
            this.password = password != null ? password : "";
        }

        void setPassword(final byte[] password){
            setPassword(new String(password, SNMP_ENCODING));
        }

        boolean setPassword(final Object password){
            if(password instanceof String){
                setPassword((String)password);
                return true;
            }
            else if(password instanceof byte[]){
                setPassword((byte[])password);
                return true;
            }
            else return false;
        }

        void setPrivacyProtocol(final String protocol) {
            if(protocol == null || protocol.isEmpty()) privacyProtocol = null;
            else switch (protocol.toLowerCase()){
                case "aes-128":
                case "aes128": setPrivacyProtocol(PrivAES128.ID); return;
                case "aes-192":
                case "aes192": setPrivacyProtocol(PrivAES192.ID);return;
                case "aes-256":
                case "aes256": setPrivacyProtocol(PrivAES256.ID); return;
                case "des": setPrivacyProtocol(PrivDES.ID); return;
                case "3des":
                case "3-des": setPrivacyProtocol(Priv3DES.ID); return;
                default: setPrivacyProtocol(new OID(protocol));
            }
        }

        void setAuthenticationProtocol(final String protocol) {
            if(protocol == null || protocol.isEmpty()) authenticationProtocol = null;
            else switch (protocol.replace(" ", "").toLowerCase()){
                case "md5":
                case "md-5": setAuthenticationProtocol(AuthMD5.ID); return;
                case "sha": setAuthenticationProtocol(AuthSHA.ID); return;
                default:
                    //attempts to parse key-value pair in format
                    authenticationProtocol = new OID(protocol);
            }
        }

        /**
         * Sets passphrase that is used to encrypt SNMPv3 traffic.
         * @param passphrase The passphrase that is used to encrypt SNMPv3 traffic.
         */
        void setPrivacyKey(final String passphrase){
            encryptionKey = passphrase;
        }

        void setPrivacyKey(final byte[] passphrase){
            setPrivacyKey(new String(passphrase, SNMP_ENCODING));
        }

        boolean setPrivacyKey(final Object passphrase){
            if(passphrase instanceof String){
                setPrivacyKey((String)passphrase);
                return true;
            }
            else if(passphrase instanceof byte[]){
                setPrivacyKey((byte[])passphrase);
                return true;
            }
            else return false;
        }

        OctetString getPasswordAsOctetString() {
            return password == null || password.isEmpty() ?
                    null :
                    toOctetString(password);
        }

        OctetString getPrivacyKeyAsOctetString(){
            return encryptionKey == null || encryptionKey.isEmpty() ?
                    null :
                    toOctetString(encryptionKey);
        }

        void defineUser(final USM userHive, final OctetString userName, final OctetString engineID) {
            userHive.addUser(userName, engineID,
                    new UsmUser(userName,
                            getAuthenticationProtocol(),
                            getPasswordAsOctetString(),
                            getPrivacyProtocol(),
                            getPrivacyKeyAsOctetString()));
        }
    }

    /**
     * Represents MIB access rights.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.2
     */
    enum AccessRights{
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
        NOTIFY
    }

    /**
     * Represents group of users. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.2
     */
    private static final class UserGroup extends HashMap<String, User>{
        private static final long serialVersionUID = -9033732379101836365L;
        private SecurityLevel level;
        private final EnumSet<AccessRights> rights;

        /**
         * Initializes a new empty user group.
         */
        UserGroup(){
            level = SecurityLevel.noAuthNoPriv;
            rights = EnumSet.noneOf(AccessRights.class);
        }

        /**
         * Gets security level applied to all users in this group.
         * @return Security level applied to all users in this group.
         */
        SecurityLevel getSecurityLevel(){
            return level;
        }

        /**
         * Sets security level for all users in this group.
         * @param value Security level for all users in this group.
         */
        void setSecurityLevel(final SecurityLevel value){
            level = value;
        }

        void setSecurityLevel(final String value){
            setSecurityLevel((value == null || value.isEmpty()) ? SecurityLevel.noAuthNoPriv : SecurityLevel.valueOf(value));
        }

        boolean hasAccessRights(final Collection<AccessRights> rights){
            return this.rights.containsAll(rights);
        }

        boolean hasAccessRights(final AccessRights... rights){
            return hasAccessRights(Arrays.asList(rights));
        }

        void setAccessRights(final Iterable<String> rights){
            this.rights.clear();
            for(final String r: rights)
                this.rights.add(AccessRights.valueOf(r.toUpperCase()));
        }

        void setAccessRights(final String rights) {
            setAccessRights(SEMICOLON_SPLITTER.splitToList(rights));
        }
    }

    private final OctetString securityEngineID;
    private final Map<String, UserGroup> groups;
    private final DirContextFactory contextFactory;

    /**
     * Initializes a new empty security configuration.
     * @param securityEngine Security engine ID (authoritative engine).
     */
    SecurityConfiguration(final byte[] securityEngine, final DirContextFactory contextFactory){
        this.securityEngineID = new OctetString(securityEngine);
        this.groups = new HashMap<>(10);
        this.contextFactory = contextFactory != null ? contextFactory : new DefaultDirContextFactory();
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
            fillUsers(adapterSettings, groupInfo, SEMICOLON_SPLITTER.splitToList(adapterSettings.get(String.format(USERS_TEMPLATE, groupName))));
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

    boolean read(final Map<String, String> adapterSettings) throws NamingException{
        if(adapterSettings.containsKey(LDAP_URI_PARAM)) //import groups and users from LDAP
            return fillGroupsFromLdap(contextFactory, adapterSettings, adapterSettings.get(LDAP_URI_PARAM), groups);
        else if(adapterSettings.containsKey(SNMPv3_GROUPS_PARAM)){ //import groups and users from local configuration file
            fillGroups(adapterSettings, SEMICOLON_SPLITTER.splitToList(adapterSettings.get(SNMPv3_GROUPS_PARAM)), groups);
            return true;
        }
        else return false;
    }

    private static boolean fillGroupsFromLdap(final DirContextFactory contextFactory,
                                              final Map<String, String> adapterSettings,
                                              final String ldapUri,
                                              final Map<String, UserGroup> groups) throws NamingException {
        final String ldapUserName = adapterSettings.get(LDAP_ADMINDN_PARAM);
        final String ldapUserPassword = adapterSettings.get(LDAP_ADMIN_PASSWORD_PARAM);
        String jndiLdapFactory = adapterSettings.get(JNDI_LDAP_FACTORY_PARAM);
        if (jndiLdapFactory == null || jndiLdapFactory.isEmpty())
            jndiLdapFactory = "com.sun.jndi.ldap.LdapCtxFactory";
        final LdapAuthenticationType authenticationType = LdapAuthenticationType.parse(adapterSettings.get(LDAP_ADMIN_AUTH_TYPE_PARAM));
        final Hashtable<String, Object> env = new Hashtable<>(7);
        authenticationType.setupEnvironment(env);
        env.put(Context.SECURITY_PRINCIPAL, ldapUserName);
        env.put(Context.SECURITY_CREDENTIALS, ldapUserPassword);
        env.put(Context.INITIAL_CONTEXT_FACTORY, jndiLdapFactory);
        env.put(Context.PROVIDER_URL, ldapUri);

        //ensures that objectSID attribute values
        //will be returned as a byte[] instead of a String
        env.put("java.naming.ldap.attributes.binary", "objectSID");
        //LDAP version
        env.put("java.naming.ldap.version", "3");

        // the following is helpful in debugging errors
        //env.put("com.sun.jndi.ldap.trace.ber", System.err);
        final DirContext ctx = contextFactory.create(env);
        final String ldapGroups = adapterSettings.get(LDAP_GROUPS_PARAM);
        final String userSearchFilter = adapterSettings.get(LDAP_USER_SEARCH_FILTER_PARAM);
        final String baseDn = adapterSettings.get(LDAP_BASE_DN_PARAM);
        final String userPasswordHolder = adapterSettings.get(LDAP_PASSWORD_HOLDER_PARAM);
        fillGroupsFromLdap(ctx, SEMICOLON_SPLITTER.splitToList(ldapGroups), baseDn, userSearchFilter, groups, userPasswordHolder);
        ctx.close();
        return true;
    }

    private static void fillGroupsFromLdap(final DirContext directory,
                                           final Collection<String> ldapGroups,
                                           final String baseDn,
                                           final String userSearchFilter,
                                           final Map<String, UserGroup> groups,
                                           final String userPasswordHolder) throws NamingException{
        //parse each group
        for(final String ldapGroupFilter: ldapGroups)
            importGroupFromLdap(directory, ldapGroupFilter, userSearchFilter, baseDn, groups, userPasswordHolder);
    }

    private static void importGroupFromLdap(final DirContext directory,
                                            final String ldapGroup,
                                            final String userSearchFilter,
                                            final String baseDn,
                                            final Map<String, UserGroup> groups,
                                            final String userPasswordHolder) throws NamingException{
        final SearchControls groupControls = new SearchControls();
        groupControls.setSearchScope(SearchControls.OBJECT_SCOPE);
        //import settings from LDAP group
        final Enumeration<SearchResult> searchResult = directory.search(baseDn, ldapGroup, groupControls);
        if(searchResult.hasMoreElements())
            importGroupFromLdap(directory, searchResult.nextElement(), userSearchFilter, baseDn, groups, userPasswordHolder);
    }

    private static void importGroupFromLdap(final DirContext directory,
                                            final SearchResult ldapGroup,
                                            final String userSearchFilter,
                                            final String baseDn,
                                            final Map<String, UserGroup> groups,
                                            final String userPasswordHolder) throws NamingException {
        final String SECURITY_LEVEL_PARAM = "snamp-snmp-security-level";
        final String SECURITY_ACCESS_RIGHTS_PARAM = "snamp-snmp-allowed-operation";
        final UserGroup userGroup = new UserGroup();
        //parse security level
        final Attribute securityLevelAttr = ldapGroup.getAttributes().get(SECURITY_LEVEL_PARAM);
        if(securityLevelAttr == null) return;
        userGroup.setSecurityLevel(Objects.toString(securityLevelAttr.get()));
        //parse access rights
        final Collection<String> accessRights = new ArrayList<>(4);
        final Attribute accessRightsAttr = ldapGroup.getAttributes().get(SECURITY_ACCESS_RIGHTS_PARAM);
        if(accessRightsAttr == null || accessRightsAttr.size() == 0) return;
        else for(int i = 0; i < accessRightsAttr.size(); i++)
            accessRights.add(Objects.toString(accessRightsAttr.get(i)));
        userGroup.setAccessRights(accessRights);
        final String GROUP_PARAM = "\\$GROUPNAME\\$";
        //fill users
        importUsersFromLdap(directory, userGroup, userSearchFilter.replaceAll(GROUP_PARAM, ldapGroup.getNameInNamespace()), baseDn, userPasswordHolder);
        //add group to set
        groups.put(ldapGroup.getNameInNamespace(), userGroup);
    }

    private static void importUsersFromLdap(final DirContext directory,
                                            final Map<String, User> userGroup,
                                            final String userSearchFilter,
                                            final String baseDn,
                                            final String userPasswordHolder) throws NamingException{
        final SearchControls userControls = new SearchControls();
        userControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        final Enumeration<SearchResult> users = directory.search(baseDn, userSearchFilter, userControls);
        while (users.hasMoreElements())
            importUserFromLdap(userGroup, users.nextElement(), userPasswordHolder);
    }

    private static void importUserFromLdap(final Map<String, User> userGroup,
                                           final SearchResult userInfo,
                                           String userPasswordHolder) throws NamingException {
        if(userPasswordHolder == null || userPasswordHolder.isEmpty())
            userPasswordHolder = "userPassword";
        final User u = new User();
        //authentication protocol
        final String AUTH_PROTOCOL_PARAM = "snamp-snmp-auth-protocol";
        final Attribute authProtocol = userInfo.getAttributes().get(AUTH_PROTOCOL_PARAM);
        if(authProtocol == null) return;
        u.setAuthenticationProtocol(Objects.toString(authProtocol.get()));
        //user password
        final Attribute userPassword = userInfo.getAttributes().get(userPasswordHolder);
        if(userPassword != null)
            u.setPassword(userPassword.get());
        //privacy protocol
        final String PRIV_PROTOCOL_PARAM = "snamp-snmp-priv-protocol";
        final Attribute privProtocol = userInfo.getAttributes().get(PRIV_PROTOCOL_PARAM);
        if(privProtocol != null)
            u.setPrivacyProtocol(Objects.toString(privProtocol.get()));
        //privacy key
        final String PRIV_KEY_PARAM = "snamp-snmp-priv-key";
        final Attribute privKey = userInfo.getAttributes().get(PRIV_KEY_PARAM);
        if(privKey != null)
            u.setPrivacyKey(privKey.get());
        userGroup.put(userInfo.getName(), u);
    }

    private interface UserSelector{
        boolean match(final String userName, final User user, final UserGroup owner);
    }

    static UserSelector createUserSelector(final AccessRights... rights){
        return (userName, user, owner) -> owner.hasAccessRights(rights);
    }

    String findFirstUser(final UserSelector selector){
        for(final UserGroup group: groups.values())
            for(final Map.Entry<String, User> user: group.entrySet())
                if(selector.match(user.getKey(), user.getValue(), group))
                    return user.getKey();
        return null;
    }

    SecurityLevel getUserSecurityLevel(final String userName){
        for(final UserGroup group: groups.values())
            for(final String lookup: group.keySet())
                if(Objects.equals(userName, lookup)) return group.getSecurityLevel();
        return SecurityLevel.noAuthNoPriv;
    }

    void setupUserBasedSecurity(final USM security){
        for(final UserGroup group: groups.values())
            for(final Map.Entry<String, User> user: group.entrySet()){
                final OctetString userName = toOctetString(user.getKey());
                final User userDef = user.getValue();
                userDef.defineUser(security, userName, securityEngineID);
            }
    }

    void setupViewBasedAcm(final VacmMIB vacm){
        for(final Map.Entry<String, UserGroup> group: groups.entrySet()){
            final UserGroup groupDef = group.getValue();
            for(final Map.Entry<String, User> user: groupDef.entrySet()){
                vacm.addGroup(SecurityModel.SECURITY_MODEL_USM, toOctetString(user.getKey()),
                        toOctetString(group.getKey()),
                        StorageType.nonVolatile);
            }
            vacm.addAccess(toOctetString(group.getKey()), new OctetString(),
                    SecurityModel.SECURITY_MODEL_USM, groupDef.getSecurityLevel().getSnmpValue(),
                    MutableVACM.VACM_MATCH_EXACT,
                    groupDef.hasAccessRights(AccessRights.READ) ? toOctetString("fullReadView") : null,
                    groupDef.hasAccessRights(AccessRights.WRITE) ? toOctetString("fullWriteView") : null,
                    groupDef.hasAccessRights(AccessRights.NOTIFY) ? toOctetString("fullNotifyView") : null,
                    StorageType.nonVolatile);
        }
    }
}
