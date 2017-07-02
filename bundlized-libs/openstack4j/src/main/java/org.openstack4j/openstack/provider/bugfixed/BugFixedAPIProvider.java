package org.openstack4j.openstack.provider.bugfixed;

import org.openstack4j.openstack.provider.DefaultAPIProvider;
import org.openstack4j.openstack.senlin.internal.bugfixed.BugFixedSenlinNodeServiceImpl;

/**
 * Various bug fixes for OpenStack API.
 * @author Roman Sakno
 */
public final class BugFixedAPIProvider extends DefaultAPIProvider {
    private final BugFixedSenlinNodeServiceImpl senlinNodeService = new BugFixedSenlinNodeServiceImpl();

    /**
     * {@inheritDoc}
     *
     * @param api
     */
    @Override
    public <T> T get(final Class<T> api) {
        final Object result;
        if(api.isInstance(senlinNodeService))
            result = senlinNodeService;
        else
            return super.get(api);
        return api.cast(result);
    }
}
