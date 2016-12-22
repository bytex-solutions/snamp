package com.bytex.snamp.cluster;

import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.KeyValueStorage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class PersistentStorageTest extends Assert {
    private GridMember instance1;

    @Before
    public void setupHazelcastNodes() throws Exception {
        instance1 = new GridMember();
        instance1.startupFromConfiguration().activate();
    }

    @After
    public void shutdownHazelcastNodes() throws InterruptedException {
        instance1.close();
        instance1 = null;
    }

    @Test
    public void getOrCreateRecordTest() {
        final KeyValueStorage storage = instance1.getService("$testStorage", ClusterMember.PERSISTENT_KV_STORAGE);
        assertNotNull(storage);
        assertTrue(storage.isPersistent());
        KeyValueStorage.TextRecordView record = storage.getOrCreateRecord("String Key", KeyValueStorage.TextRecordView.class, KeyValueStorage.TextRecordView.INITIALIZER);
        record.setAsText("Hello, world");
        record = storage.getRecord("String Key", KeyValueStorage.TextRecordView.class).get();
        assertNotNull(record);
        assertEquals("Hello, world", record.getAsText());
    }

    @Test
    public void differentCollectionsTest(){
        final KeyValueStorage storage1 = instance1.getService("$testStorage1", ClusterMember.PERSISTENT_KV_STORAGE);
        final KeyValueStorage storage2 = instance1.getService("$testStorage2", ClusterMember.PERSISTENT_KV_STORAGE);
        assertNotNull(storage1);
        assertNotNull(storage2);
        assertTrue(storage1.isPersistent());
        assertTrue(storage2.isPersistent());
        KeyValueStorage.TextRecordView record1 = storage1.getOrCreateRecord(100500, KeyValueStorage.TextRecordView.class, KeyValueStorage.TextRecordView.INITIALIZER);
        KeyValueStorage.TextRecordView record2 = storage2.getOrCreateRecord(100500, KeyValueStorage.TextRecordView.class, KeyValueStorage.TextRecordView.INITIALIZER);
        assertNotEquals(record1, record2);
        record1.setAsText("Hello, world!");
        record2.setAsText("Frank Underwood");
        record1.refresh();
        record2.refresh();
        assertNotEquals(record1.getAsText(), record2.getAsText());
        assertEquals("Hello, world!", record1.getAsText());
        assertEquals("Frank Underwood", record2.getAsText());
    }

    @Test
    public void theSameCollectionTest(){
        final KeyValueStorage storage1 = instance1.getService("$testStorage1", ClusterMember.PERSISTENT_KV_STORAGE);
        assertNotNull(storage1);
        KeyValueStorage.TextRecordView record1 = storage1.getOrCreateRecord("Frank Underwood", KeyValueStorage.TextRecordView.class, KeyValueStorage.TextRecordView.INITIALIZER);
        record1.setAsText("Hello, world!");
        KeyValueStorage.TextRecordView record2 = storage1.getOrCreateRecord("Frank Underwood", KeyValueStorage.TextRecordView.class, KeyValueStorage.TextRecordView.INITIALIZER);
        assertEquals(record1.getAsText(), record2.getAsText());
    }

    @Test
    public void differentTypesTest(){
        final KeyValueStorage storage1 = instance1.getService("$testStorage1", ClusterMember.PERSISTENT_KV_STORAGE);
        assertNotNull(storage1);
        final KeyValueStorage.TextRecordView textRecord = storage1.getOrCreateRecord("Frank Underwood", KeyValueStorage.TextRecordView.class, KeyValueStorage.TextRecordView.INITIALIZER);
        textRecord.setAsText("Hello, world!");
        final KeyValueStorage.SerializableRecordView customRecord = storage1.getOrCreateRecord("Frank Underwood", KeyValueStorage.SerializableRecordView.class, record -> record.setValue(""));
        customRecord.setValue(new StringBuffer("Hello, world!"));
        assertTrue(customRecord.getValue() instanceof StringBuffer);
    }
}
