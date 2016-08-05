package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.concurrent.GroupedThreadFactory;
import com.bytex.snamp.concurrent.impl.ThreadPoolRepositoryImpl;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Represents configuration of the thread pool.
 * @since 2.0
 * @version 2.0
 */
final class SerializableThreadPoolConfiguration extends AbstractEntityConfiguration implements ThreadPoolConfiguration {
    private int threadPriority;
    private int minPoolSize;
    private int maxPoolSize;
    private int queueSize;
    private Duration keepAliveTime;

    @SpecialUse
    public SerializableThreadPoolConfiguration(){
        threadPriority = DEFAULT_THREAD_PRIORITY;
        minPoolSize = DEFAULT_MIN_POOL_SIZE;
        maxPoolSize = DEFAULT_MAX_POOL_SIZE;
        queueSize = INFINITE_QUEUE_SIZE;
        keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
    }

    @Override
    public ExecutorService apply(final String threadGroup) {
        return ThreadPoolRepositoryImpl.createThreadPool(minPoolSize, maxPoolSize, keepAliveTime, queueSize, threadPriority, threadGroup);
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeInt(minPoolSize);
        out.writeInt(maxPoolSize);
        out.writeInt(threadPriority);
        out.writeInt(queueSize);
        out.writeObject(keepAliveTime);
        writeParameters(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        minPoolSize = in.readInt();
        maxPoolSize = in.readInt();
        threadPriority = in.readInt();
        queueSize = in.readInt();
        keepAliveTime = (Duration) in.readObject();
        readParameters(in);
    }

    @Override
    public int getThreadPriority() {
        return threadPriority;
    }

    @Override
    public void setThreadPriority(int value) {
        threadPriority = value;
        markAsModified();
    }

    @Override
    public int getMinPoolSize(){
        return minPoolSize;
    }

    @Override
    public void setMinPoolSize(final int value){
        minPoolSize = value;
        markAsModified();
    }

    @Override
    public int getMaxPoolSize(){
        return maxPoolSize;
    }

    @Override
    public void setMaxPoolSize(final int value){
        maxPoolSize = value;
        markAsModified();
    }

    @Override
    public Duration getKeepAliveTime() {
        return keepAliveTime;
    }

    @Override
    public void setKeepAliveTime(final Duration value) {
        keepAliveTime = Objects.requireNonNull(value);
        markAsModified();
    }

    @Override
    public int getQueueSize(){
        return queueSize;
    }

    @Override
    public void setQueueSize(final int value){
        queueSize = value;
        markAsModified();
    }
}
