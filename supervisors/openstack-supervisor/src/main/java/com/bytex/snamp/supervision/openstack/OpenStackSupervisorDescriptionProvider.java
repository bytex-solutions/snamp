package com.bytex.snamp.supervision.openstack;

import com.bytex.snamp.concurrent.LazyStrongReference;
import com.bytex.snamp.supervision.def.DefaultSupervisorConfigurationDescriptionProvider;
import org.openstack4j.api.client.CloudProvider;
import org.openstack4j.model.common.Identifier;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static com.bytex.snamp.MapUtils.getValue;

/**
 * Describes configuration of OpenStack supervisor.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
final class OpenStackSupervisorDescriptionProvider extends DefaultSupervisorConfigurationDescriptionProvider {

    private static final String USER_NAME_PARAM = "userName";
    private static final String PASSWORD_PARAM = "password";
    private static final String ENDPOINT_PARAM = "authURL";
    private static final String CLOUD_PROVIDER_PARAM = "cloudProvider";
    private static final String USER_DOMAIN_PARAM = "userDomain";
    private static final String PROJECT_PARAM = "project";
    private static final String PROJECT_DOMAIN_PARAM = "projectDomain";
    private static final String REGION_PARAM = "region";
    private static final String CLUSTER_ID_PARAM = "clusterID";
    private static final String ELASTMAN_PARAM = "elasticityManagement";
    private static final String AUTO_DISCOVERY_PARAM = "autoDiscovery";

    private final Identifier defaultDomain;
    private final Identifier demoProject;

    private static final LazyStrongReference<OpenStackSupervisorDescriptionProvider> INSTANCE = new LazyStrongReference<>();


    private static final class SupervisorDescription extends DefaultSupervisorDescription{

        private SupervisorDescription() {
            super("SupervisorConfiguration", USER_NAME_PARAM, PASSWORD_PARAM);
        }
    }

    private OpenStackSupervisorDescriptionProvider(){
        super(new SupervisorDescription());
        defaultDomain = Identifier.byName("default");   //as in devstack
        demoProject = Identifier.byName("demo");        //as in devstack
    }

    static OpenStackSupervisorDescriptionProvider getInstance(){
        return INSTANCE.lazyGet(OpenStackSupervisorDescriptionProvider::new);
    }

    String parseUserName(final Map<String, String> configuration) throws OpenStackAbsentConfigurationParameterException {
        return getValue(configuration, USER_NAME_PARAM, Function.identity())
                .orElseThrow(() -> new OpenStackAbsentConfigurationParameterException(USER_NAME_PARAM));
    }

    String parsePassword(final Map<String, String> configuration) throws OpenStackAbsentConfigurationParameterException {
        return getValue(configuration, PASSWORD_PARAM, Function.identity())
                .orElseThrow(() -> new OpenStackAbsentConfigurationParameterException(PASSWORD_PARAM));
    }

    String parseApiEndpoint(final Map<String, String> configuration) throws OpenStackAbsentConfigurationParameterException {
        return getValue(configuration, ENDPOINT_PARAM, Function.identity())
                .orElseThrow(() -> new OpenStackAbsentConfigurationParameterException(ENDPOINT_PARAM));
    }

    CloudProvider parseCloudProvider(final Map<String, String> configuration){
        switch (configuration.getOrDefault(CLOUD_PROVIDER_PARAM, "").toLowerCase()){
            case "rackspace":
                return CloudProvider.RACKSPACE;
            case "hpcloud":
                return CloudProvider.HPCLOUD;
            default:
                return CloudProvider.UNKNOWN;
        }
    }

    String parseClusterID(final Map<String, String> configuration) throws OpenStackAbsentConfigurationParameterException {
        return getValue(configuration, CLUSTER_ID_PARAM, Function.identity())
                .orElseThrow(() -> new OpenStackAbsentConfigurationParameterException(CLUSTER_ID_PARAM));
    }

    Identifier parseUserDomain(final Map<String, String> configuration) {
        return getValue(configuration, USER_DOMAIN_PARAM, Identifier::byName).orElse(defaultDomain);
    }

    Identifier parseProject(final Map<String, String> configuration){
        return getValue(configuration, PROJECT_PARAM, Identifier::byName).orElse(demoProject);
    }

    Identifier parseProjectDomain(final Map<String, String> configuration){
        return getValue(configuration, PROJECT_DOMAIN_PARAM, Identifier::byName).orElse(defaultDomain);
    }

    boolean isElasticityManagementEnabled(final Map<String, String> configuration) {
        return getValue(configuration, ELASTMAN_PARAM, Boolean::parseBoolean).orElse(false);
    }

    Optional<String> parseRegion(final Map<String, String> configuration){
        return getValue(configuration, REGION_PARAM, Function.identity());
    }

    boolean isAutoDiscovery(final Map<String, String> configuration){
        return getValue(configuration, AUTO_DISCOVERY_PARAM, Boolean::parseBoolean).orElse(true);
    }
}
