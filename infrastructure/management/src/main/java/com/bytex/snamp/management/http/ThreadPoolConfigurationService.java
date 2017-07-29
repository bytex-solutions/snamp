package com.bytex.snamp.management.http;

import com.bytex.snamp.configuration.EntityMapResolver;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;
import com.bytex.snamp.management.http.model.ThreadPoolDataObject;

import javax.ws.rs.Path;

/**
 * Represents configuration service for
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
@Path("/configuration/threadPools")
public final class ThreadPoolConfigurationService extends AbstractEntityConfigurationService<ThreadPoolConfiguration, ThreadPoolDataObject> {
    ThreadPoolConfigurationService() {
        super(EntityMapResolver.THREAD_POOLS);
    }

    @Override
    protected ThreadPoolDataObject toDataTransferObject(final ThreadPoolConfiguration entity) {
        return new ThreadPoolDataObject(entity);
    }
}
