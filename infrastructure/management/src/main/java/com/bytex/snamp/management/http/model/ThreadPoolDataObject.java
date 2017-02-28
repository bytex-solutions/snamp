package com.bytex.snamp.management.http.model;

import com.bytex.snamp.configuration.ThreadPoolConfiguration;
import org.codehaus.jackson.annotate.JsonProperty;

import java.time.Duration;

/**
 * Represents JSON DTO for {@link ThreadPoolConfiguration}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class ThreadPoolDataObject extends AbstractDataObject<ThreadPoolConfiguration> {
    private int minPoolSize;
    private int maxPoolSize;
    private Duration keepAliveTime;
    private int queueSize;
    private int priority;

    public ThreadPoolDataObject(){
        minPoolSize = ThreadPoolConfiguration.DEFAULT_MIN_POOL_SIZE;
        maxPoolSize = ThreadPoolConfiguration.DEFAULT_MAX_POOL_SIZE;
        keepAliveTime = ThreadPoolConfiguration.DEFAULT_KEEP_ALIVE_TIME;
        queueSize = ThreadPoolConfiguration.INFINITE_QUEUE_SIZE;
        priority = ThreadPoolConfiguration.DEFAULT_THREAD_PRIORITY;
    }

    public ThreadPoolDataObject(final ThreadPoolConfiguration configuration){
        super(configuration);
        minPoolSize = configuration.getMinPoolSize();
        maxPoolSize = configuration.getMaxPoolSize();
        keepAliveTime = configuration.getKeepAliveTime();
        queueSize = configuration.getQueueSize();
        priority = configuration.getThreadPriority();
    }

    @JsonProperty
    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(final int value) {
        minPoolSize = value;
    }

    @JsonProperty
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(final int value) {
        maxPoolSize = value;
    }

    @JsonProperty
    public long getKeepAliveTime() {
        return keepAliveTime.toMillis();
    }

    public void setKeepAliveTime(final long timeInMillis) {
        keepAliveTime = Duration.ofMillis(timeInMillis);
    }

    @JsonProperty
    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(final int value) {
        queueSize = value;
    }

    @JsonProperty
    public int getThreadPriority() {
        return priority;
    }

    public void setThreadPriority(final int value) {
        priority = value;
    }

    /**
     * Exports state of this object into entity configuration.
     *
     * @param entity Entity to modify.
     */
    @Override
    public void exportTo(final ThreadPoolConfiguration entity) {
        super.exportTo(entity);
        entity.setQueueSize(queueSize);
        entity.setMaxPoolSize(maxPoolSize);
        entity.setMinPoolSize(minPoolSize);
        entity.setThreadPriority(priority);
        entity.setKeepAliveTime(keepAliveTime);
    }
}
