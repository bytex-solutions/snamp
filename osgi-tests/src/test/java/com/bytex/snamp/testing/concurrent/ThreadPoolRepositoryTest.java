package com.bytex.snamp.testing.concurrent;

import com.bytex.snamp.concurrent.ThreadPoolRepository;
import com.bytex.snamp.configuration.AgentConfiguration;
import com.bytex.snamp.core.ServiceHolder;
import com.bytex.snamp.io.IOUtils;
import com.bytex.snamp.testing.AbstractSnampIntegrationTest;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;

import static com.bytex.snamp.concurrent.ThreadPoolRepository.DEFAULT_POOL;

/**
 * Provides tests for {@link com.bytex.snamp.concurrent.ThreadPoolRepository} service.
 * @author Roman Sakno
 * @version 1.2
 * @since 1.2
 */
public final class ThreadPoolRepositoryTest extends AbstractSnampIntegrationTest {

    private static void registerThreadPool(final String name, final BundleContext context){
        final ServiceHolder<ConfigurationAdmin> repositoryRef = ServiceHolder.tryCreate(context, ConfigurationAdmin.class);
        assertNotNull(repositoryRef);
        try{
            final Configuration config = repositoryRef.get().getConfiguration("com.bytex.snamp.concurrency.threadPools");
            Dictionary<String, Object> properties = config.getProperties();
            if(properties == null) properties = new Hashtable<>();
            final ThreadPoolConfig tp = new ThreadPoolConfig();
            tp.setMaxPoolSize(42);
            properties.put(name, IOUtils.serialize(tp));
            config.update(properties);
        } catch (final IOException e) {
            fail(e.getMessage());
        } finally {
            repositoryRef.release(context);
        }
    }

    @Test
    public void registerThreadPoolTest() throws InterruptedException {
        final ServiceHolder<ThreadPoolRepository> repositoryRef = ServiceHolder.tryCreate(getTestBundleContext(), ThreadPoolRepository.class);
        assertNotNull(repositoryRef);
        try{
            ThreadPoolConfig config = repositoryRef.get().getConfiguration(DEFAULT_POOL);
            assertEquals(new ThreadPoolConfig(), config);
            ExecutorService executor = repositoryRef.get().getThreadPool(DEFAULT_POOL, false);
            assertNotNull(executor);
            //register thread pool
            config = new ThreadPoolConfig();
            config.setMaxPoolSize(100);
            repositoryRef.get().registerThreadPool("Pool1", config);
            executor = repositoryRef.get().getThreadPool("Pool1", false);
            assertNotNull(executor);
            //register thread pool via config admin
            registerThreadPool("Pool2", getTestBundleContext());
            Thread.sleep(2000); //wait to process asynchronous configuration event
            config = repositoryRef.get().getConfiguration("Pool2");
            assertNotNull(config);
            assertEquals(42, config.getMaxPoolSize());
            executor = repositoryRef.get().getThreadPool("Pool2", false);
            assertNotNull(executor);
        }
        finally {
            repositoryRef.release(getTestBundleContext());
        }
    }

    @Override
    protected boolean enableRemoteDebugging() {
        return false;
    }

    @Override
    protected void setupTestConfiguration(final AgentConfiguration config) {

    }
}
