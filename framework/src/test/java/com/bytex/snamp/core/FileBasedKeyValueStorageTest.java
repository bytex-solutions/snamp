package com.bytex.snamp.core;

import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Test;

/**
 * Represents tests for {@link FileBasedKeyValueStorage}.
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class FileBasedKeyValueStorageTest extends Assert {
    @Test
    public void readWriteTest(){
        final KeyValueStorage storage = DistributedServices.getProcessLocalObject("storage1", SharedObjectType.PERSISTENT_KV_STORAGE).orElseThrow(AssertionError::new);
        assertTrue(storage instanceof FileBasedKeyValueStorage);
        KeyValueStorage.MapRecordView mapRecord = storage.getOrCreateRecord(42L, KeyValueStorage.MapRecordView.class, KeyValueStorage.MapRecordView.INITIALIZER);
        mapRecord.setAsMap(ImmutableMap.of("key1", "value1", "key2", "value2"));
        mapRecord = storage.getRecord(42L, KeyValueStorage.MapRecordView.class).get();
        assertNotNull(mapRecord);
        assertTrue(storage.delete(42L));
        assertFalse(storage.getRecord(42L, KeyValueStorage.MapRecordView.class).isPresent());
    }
}
