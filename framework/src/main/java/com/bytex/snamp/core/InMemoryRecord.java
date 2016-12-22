package com.bytex.snamp.core;

import com.bytex.snamp.Acceptor;
import com.bytex.snamp.Convert;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableMap;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.bytex.snamp.core.KeyValueStorage.*;

/**
 * Represents in-memory record.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@ThreadSafe
final class InMemoryRecord extends AtomicReference<Serializable> implements Record, SerializableRecordView, TextRecordView, MapRecordView, LongRecordView, DoubleRecordView, JsonRecordView {
    private static final long serialVersionUID = -442829143265236511L;

    private static final class MapValue extends HashMap<String, Object> {
        private static final long serialVersionUID = 8695790321685285451L;

        private MapValue(final Map<String, ?> values) {
            super(values);
        }
    }

    private volatile boolean detached;
    private final AtomicInteger version;

    InMemoryRecord() {
        detached = false;
        version = new AtomicInteger(0);
    }

    <R extends KeyValueStorage.Record, E extends Throwable> void init(final Acceptor<? super R, E> initializer, final Class<R> recordView) throws E {
        if (recordView.isInstance(this)) {
            initializer.accept(recordView.cast(this));
            if (get() == null)
                throw new IllegalStateException("Record is not initialized");
        } else
            throw new ClassCastException("Unsupported record view " + recordView);
    }

    void detach() {
        detached = true;
        set(null);
    }

    @Override
    public boolean isDetached() {
        return detached;
    }

    @Override
    public void refresh() {
    }

    @Override
    public int getVersion() {
        return version.get();
    }

    @Override
    public Serializable getValue() {
        return get();
    }

    @Override
    public void setValue(final Serializable value) {
        if (detached)
            throw new IllegalStateException("Record is detached");
        set(Objects.requireNonNull(value));
    }

    @Override
    public String getAsText() {
        final Serializable result = getValue();
        return result != null ? result.toString() : null;
    }

    @Override
    public void setAsText(final String value) {
        setValue(value);
    }

    @Override
    public Map<String, ?> getAsMap() {
        final Serializable value = getValue();
        return value instanceof MapValue ? (MapValue) value : ImmutableMap.of();
    }

    @Override
    public void setAsMap(final Map<String, ?> values) {
        setValue(new MapValue(values));
    }

    @Override
    public long getAsLong() {
        return Convert.toLong(getValue());
    }

    @Override
    public void accept(final long value) {
        setValue(value);
    }

    @Override
    public double getAsDouble() {
        return Convert.toDouble(getValue());
    }

    @Override
    public void accept(final double value) {
        setValue(value);
    }

    @Override
    public Reader getAsJson() {
        final String json = getAsText();
        return json != null && json.length() > 0 ? new StringReader(json) : IOUtils.EMPTY_READER;
    }

    @Override
    public void setAsJson(final Reader value) throws IOException {
        setAsText(IOUtils.toString(value));
    }
}
