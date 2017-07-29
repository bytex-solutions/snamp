package com.bytex.snamp.testing.concurrent;

import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import org.junit.Test;

import java.util.concurrent.ExecutorService;

/**
 * Provides tests for {@link com.bytex.snamp.concurrent.ThreadPoolRepository} service.
 * @author Roman Sakno
 * @version 2.1
 * @since 1.2
 */
public final class ThreadPoolRepositoryTest extends AbstractSnampIntegrationTest {
    private static final String POOL_NAME = "testPool";

    @Test
    public void checkThreadPoolConfigTest() throws InterruptedException {
        final ServiceHolder<ThreadPoolRepository> repository = ServiceHolder.tryCreate(getTestBundleContext(), ThreadPoolRepository.class)
                .orElseThrow(AssertionError::new);
        try{
            final ExecutorService service = repository.get().getThreadPool(POOL_NAME, false);
            assertNotNull(service);
        }
        finally {
            repository.release(getTestBundleContext());
        }
    }

    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {
        config.getThreadPools().getOrAdd(POOL_NAME);
    }
}
