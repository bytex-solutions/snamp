package com.snamp.adapters;

import static com.snamp.configuration.SnmpAdapterConfigurationDescriptor.*;
import org.snmp4j.security.*;
import org.snmp4j.smi.OID;

import java.util.*;

/**
 * Represents security configuration of the SNMP adapter that is used
 * to setup SNMPv3 settings. This class cannot be inherited.
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
final class SecurityConfiguration {

    /**
     * Represents SNMPv3 user. This class cannot be inherited.
     * @author Roman Sakno
     * @since 1.0
     * @version 1.0
     */
    public static final class User{
        private OID authenticationProtocol;
        private OID privacyProtocol;
        private String password;

        /**
         * Initializes a new user security information.
         */
        public User(){
            authenticationProtocol = null;
            privacyProtocol = null;
            password = "";
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
            else switch (protocol){
                case "md5":
                case "md-5":
                case "MD-5":
                case "MD5": setAuthenticationProtocol(AuthMD5.ID); return;
                case "SHA":
                case "sha": setAuthenticationProtocol(AuthSHA.ID); return;
                default: authenticationProtocol = new OID(protocol); return;
            }
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
            level = (value == null || value.isEmpty()) ? SecurityLevel.noAuthNoPriv : SecurityLevel.valueOf(value);
        }

        /**
         * Sets MIB access rights to all users in this group.
         * @param rights MIB access rights of all users in this group.
         */
        public final void setAccessRights(final Collection<AccessRights> rights){
            this.rights.clear();
            this.rights.addAll(rights);
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
            setAccessRights(splitAndTrim(rights, ","));
        }
    }

    private final byte[] securityEngineID;
    private final Map<String, UserGroup> groups;

    /**
     * Initializes a new empty security configuration.
     * @param securityEngine Security engine ID (authoritative engine).
     */
    public SecurityConfiguration(final byte[] securityEngine){
        this.securityEngineID = securityEngine;
        this.groups = new HashMap<>(10);
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
            fillUsers(adapterSettings, groupInfo, splitAndTrim(adapterSettings.get(String.format(USERS_TEMPLATE, groupName)), ","));
        }
    }

    private static void fillUsers(final Map<String,String> adapterSettings, final UserGroup groupInfo, final Collection<String> userNames) {
        final String PASSWORD_TEMPLATE = "%s-password";
        final String AUTH_PROTOCOL_TEMPLATE = "%s-auth-protocol";
        final String PRIVACY_PROTOCOL_TEMPLATE = "%s-privacy-protocol";
        for(final String name: userNames){
            final User userInfo = new User();
            groupInfo.put(name, userInfo);
            userInfo.setPassword(adapterSettings.get(String.format(PASSWORD_TEMPLATE, name)));
            userInfo.setPrivacyProtocol(adapterSettings.get(String.format(PRIVACY_PROTOCOL_TEMPLATE, name)));
            userInfo.setAuthenticationProtocol(adapterSettings.get(String.format(AUTH_PROTOCOL_TEMPLATE, name)));
        }
    }

    public final boolean read(final Map<String, String> adapterSettings){
        if(adapterSettings.containsKey(SNMPv3_GROUPS_PROPERTY)){
            fillGroups(adapterSettings, splitAndTrim(adapterSettings.get(SNMPv3_GROUPS_PROPERTY), ","), groups);
            return true;
        }
        else return false;
    }
}
