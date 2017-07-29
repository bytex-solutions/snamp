package com.bytex.snamp.configuration.impl;

import com.bytex.snamp.SpecialUse;
import com.bytex.snamp.configuration.ThreadPoolConfiguration;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Represents serializable configuration of the thread pool.
 * @since 2.0
 * @version 2.1
 */
final class SerializableThreadPoolConfiguration extends AbstractEntityConfiguration implements ThreadPoolConfiguration {
    private static final long serialVersionUID = 8726763924738566197L;
    private int threadPriority;
    private int minPoolSize;
    private int maxPoolSize;
    private int queueSize;
    private Duration keepAliveTime;

    @SpecialUse(SpecialUse.Case.SERIALIZATION)
    public SerializableThreadPoolConfiguration(){
        threadPriority = DEFAULT_THREAD_PRIORITY;
        minPoolSize = DEFAULT_MIN_POOL_SIZE;
        maxPoolSize = DEFAULT_MAX_POOL_SIZE;
        queueSize = INFINITE_QUEUE_SIZE;
        keepAliveTime = DEFAULT_KEEP_ALIVE_TIME;
    }

    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeInt(minPoolSize);
        out.writeInt(maxPoolSize);
        out.writeInt(threadPriority);
        out.writeInt(queueSize);
        out.writeObject(keepAliveTime);
        super.writeExternal(out);
    }

    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        minPoolSize = in.readInt();
        maxPoolSize = in.readInt();
        threadPriority = in.readInt();
        queueSize = in.readInt();
        keepAliveTime = (Duration) in.readObject();
        super.readExternal(in);
    }

    private void loadParameters(final Map<String, String> parameters){
        clear();
        putAll(parameters);
    }

    private void load(final ThreadPoolConfiguration configuration){
        minPoolSize = configuration.getMinPoolSize();
        maxPoolSize = configuration.getMaxPoolSize();
        threadPriority = configuration.getThreadPriority();
        queueSize = configuration.getQueueSize();
        keepAliveTime = configuration.getKeepAliveTime();
        loadParameters(configuration);
    }

    @Override
    public void load(final Map<String, String> parameters) {
        if(parameters instanceof ThreadPoolConfiguration)
            load((ThreadPoolConfiguration) parameters);
        else
            loadParameters(parameters);
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

    @Override
    public int hashCode() {
        return super.hashCode() ^ Objects.hash(keepAliveTime, minPoolSize, maxPoolSize, threadPriority, queueSize);
    }

    private boolean equals(final ThreadPoolConfiguration other){
        return minPoolSize == other.getMinPoolSize() &&
                maxPoolSize == other.getMaxPoolSize() &&
                threadPriority == other.getThreadPriority() &&
                keepAliveTime.equals(other.getKeepAliveTime()) &&
                queueSize == other.getQueueSize() &&
                super.equals(other);
    }

    @Override
    public boolean equals(final Object other) {
        return other instanceof ThreadPoolConfiguration && equals((ThreadPoolConfiguration) other);
    }
}
