package com.bytex.snamp.supervision.openstack;

import com.bytex.snamp.concurrent.LazyStrongReference;
import com.bytex.snamp.supervision.def.DefaultSupervisorConfigurationDescriptionProvider;

import java.util.Map;
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
    private static final String KEYSTONE_VER_PARAM = "keystone";

    private static final LazyStrongReference<OpenStackSupervisorDescriptionProvider> INSTANCE = new LazyStrongReference<>();


    private static final class SupervisorDescription extends DefaultSupervisorDescription{

        private SupervisorDescription() {
            super("SupervisorConfiguration", USER_NAME_PARAM, PASSWORD_PARAM);
        }
    }

    private OpenStackSupervisorDescriptionProvider(){
        super(new SupervisorDescription());
    }

    static OpenStackSupervisorDescriptionProvider getInstance(){
        return INSTANCE.lazyGet(OpenStackSupervisorDescriptionProvider::new);
    }

    OpenStackAuthenticator parseAuthenticator(final Map<String, String> configuration) {
        switch (configuration.getOrDefault(KEYSTONE_VER_PARAM, OpenStackAuthenticatorV3.VERSION)) {
            case OpenStackAuthenticatorV2.VERSION:
                return new OpenStackAuthenticatorV2();
            case OpenStackAuthenticatorV3.VERSION:
            default:
                return new OpenStackAuthenticatorV3();
        }
    }

    String parseUserName(final Map<String, String> configuration) throws OpenStackAbsentConfigurationParameterException {
        return getValue(configuration, USER_NAME_PARAM, Function.identity())
                .orElseThrow(() -> new OpenStackAbsentConfigurationParameterException(USER_NAME_PARAM));
    }

    String parsePassword(final Map<String, String> configuration) throws OpenStackAbsentConfigurationParameterException {
        return getValue(configuration, PASSWORD_PARAM, Function.identity())
                .orElseThrow(() -> new OpenStackAbsentConfigurationParameterException(PASSWORD_PARAM));
    }
}
