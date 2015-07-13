package com.itworks.snamp.connectors.openstack;

import com.itworks.snamp.connectors.attributes.AttributeDescriptor;
import org.openstack4j.api.OSClient;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.openstack.OSFactory;

import javax.management.Descriptor;
import static com.itworks.snamp.jmx.DescriptorUtils.*;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class OpenStackResourceConnectorConfigurationDescriptor {
    private static final String VERSION_PARAM = "version";
    private static final String DEFAULT_VERSION = "v2";

    private static final String RESOURCE_TYPE_PARAM = "resourceType";
    private static final String ADDRESS_PARAM = "address";
    private static final String DOMAIN_NAME_PARAM = "domainName";
    private static final String DOMAIN_ID_PARAM = "domainID";
    private static final String USER_NAME_PARAM = "userName";
    private static final String PASSWORD_PARAM = "password";
    private static final String TOKEN_ID_PARAM = "tokenID";

    private static final String ENTITY_ID_PARAM = "entityID";

    private static String getRequiredParam(final Map<String, String> params,
                                           final String paramName) throws OpenStackAbsentConfigurationParameterException {
        if (params.containsKey(paramName))
            return params.get(paramName);
        else throw new OpenStackAbsentConfigurationParameterException(paramName);
    }

    static String getEntityID(final Map<String, String> parameters) throws OpenStackAbsentConfigurationParameterException {
        return getRequiredParam(parameters, ENTITY_ID_PARAM);
    }

    static OSClient createClient(final Map<String, String> params) throws OpenStackAbsentConfigurationParameterException {
        final String version = params.containsKey(VERSION_PARAM) ?
                params.get(VERSION_PARAM) :
                DEFAULT_VERSION;

        final String address = getRequiredParam(params, ADDRESS_PARAM);
        final String userName = getRequiredParam(params, USER_NAME_PARAM);
        final String password = getRequiredParam(params, PASSWORD_PARAM);

        switch (version) {
            case "v3":
                final Identifier domainName;
                if (params.containsKey(DOMAIN_NAME_PARAM))
                    domainName = Identifier.byName(params.get(DOMAIN_NAME_PARAM));
                else if (params.containsKey(DOMAIN_ID_PARAM))
                    domainName = Identifier.byId(params.get(DOMAIN_ID_PARAM));
                else throw new OpenStackAbsentConfigurationParameterException(DOMAIN_NAME_PARAM);
                return OSFactory.builderV3()
                        .endpoint(address)
                        .credentials(userName, password, domainName)
                        .token(params.get(TOKEN_ID_PARAM))
                        .authenticate();
            default:
                return OSFactory.builder()
                        .endpoint(address)
                        .tenantName(params.get(DOMAIN_NAME_PARAM))
                        .tenantId(params.get(DOMAIN_ID_PARAM))
                        .credentials(userName, password)
                        .authenticate();
        }
    }

    static OpenStackResourceType getResourceType(final Map<String, String> params) throws OpenStackAbsentConfigurationParameterException {
        final String resourceType = getRequiredParam(params, RESOURCE_TYPE_PARAM);
        final OpenStackResourceType result = OpenStackResourceType.parse(resourceType);
        if (result == null) throw new OpenStackAbsentConfigurationParameterException(RESOURCE_TYPE_PARAM);
        else return result;
    }
}
