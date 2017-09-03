package com.bytex.snamp.core;

import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;

/**
 * Represents tests for {@link FileBasedKeyValueStorage}.
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class FileBasedKeyValueStorageTest extends Assert {
    private static void setRecordValue(final KeyValueStorage.JsonRecordView record, final String value){
        try(final Writer writer = record.createJsonWriter()){
            writer.append(value);
        } catch (final IOException e){
            throw new UncheckedIOException(e);  
        }
    }

    @Test
    public void readWriteTest() throws IOException {
        final KeyValueStorage storage = ClusterMember.get(null).getService(KeyValueStorage.persistent("storage1")).orElseThrow(AssertionError::new);
        assertTrue(storage instanceof FileBasedKeyValueStorage);
        KeyValueStorage.MapRecordView mapRecord = storage.getOrCreateRecord(42L, KeyValueStorage.MapRecordView.class, KeyValueStorage.MapRecordView.INITIALIZER);
        mapRecord.setAsMap(ImmutableMap.of("key1", "value1", "key2", "value2"));
        mapRecord = storage.getRecord(42L, KeyValueStorage.MapRecordView.class).get();
        assertNotNull(mapRecord);
        assertTrue(storage.delete(42L));
        assertFalse(storage.getRecord(42L, KeyValueStorage.MapRecordView.class).isPresent());
        KeyValueStorage.JsonRecordView jsonRecord = storage.getOrCreateRecord(100500, KeyValueStorage.JsonRecordView.class, record -> setRecordValue(record, "Frank Underwood"));
        try(final Reader reader = jsonRecord.getAsJson()){
            final String json = IOUtils.toString(reader);
            assertNotNull(json);
            assertFalse(json.isEmpty());
        }
        setRecordValue(jsonRecord, "Other value");
        jsonRecord = storage.getOrCreateRecord(100500, KeyValueStorage.JsonRecordView.class, record -> setRecordValue(record, "{}"));
        try(final Reader reader = jsonRecord.getAsJson()){
            final String json = IOUtils.toString(reader);
            assertNotNull(json);
            assertFalse(json.isEmpty());
        }
    }
}
