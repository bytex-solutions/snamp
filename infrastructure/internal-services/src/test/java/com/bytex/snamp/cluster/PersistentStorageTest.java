package com.bytex.snamp.cluster;

import com.bytex.snamp.concurrent.FutureThread;
import com.bytex.snamp.core.ClusterMember;
import com.bytex.snamp.core.KeyValueStorage;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class PersistentStorageTest extends Assert {
    private GridMember instance1;

//    @BeforeClass
//    public static void setupDatabaseHome(){
//        final String userHome = StandardSystemProperty.USER_HOME.value();
//        assertFalse(Strings.isNullOrEmpty(userHome));
//        System.setProperty(Orient.ORIENTDB_HOME, userHome);
//    }

    @Before
    public void setupHazelcastNodes() throws Exception {
        instance1 = new GridMember();
        instance1.startupFromConfiguration().activate();
    }

    @After
    public void shutdownHazelcastNodes() throws InterruptedException {
        instance1.destroyLocalServices();
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
    public void differentTypesTest() throws ExecutionException, InterruptedException {
        final String KEY = "Frank Underwood";
        final FutureThread<Void> thread = FutureThread.start(() -> {
            final KeyValueStorage storage1 =
                    FutureThread.start(() -> instance1.getService("$testStorage1", ClusterMember.PERSISTENT_KV_STORAGE)).get();
            assertNotNull(storage1);
            storage1.getOrCreateRecord(KEY, KeyValueStorage.TextRecordView.class, KeyValueStorage.TextRecordView.INITIALIZER);

            final KeyValueStorage.SerializableRecordView customRecord = storage1.getRecord(KEY, KeyValueStorage.SerializableRecordView.class).get();
            customRecord.setValue(new StringBuffer("Hello, world!"));
            assertTrue(customRecord.getValue() instanceof StringBuffer);

            final KeyValueStorage.MapRecordView mapRecord = storage1.getRecord(KEY, KeyValueStorage.MapRecordView.class).get();
            mapRecord.setAsMap(ImmutableMap.of("key1", "value1", "key2", "value2"));
            final Map<String, ?> map = mapRecord.getAsMap();
            assertNotNull(map);
            assertEquals("value1", map.get("key1"));
            assertEquals("value2", map.get("key2"));

            final KeyValueStorage.JsonRecordView jsonRecord = storage1.getRecord(KEY, KeyValueStorage.JsonRecordView.class).get();
            jsonRecord.setAsJson(new StringReader("{\"a\": 10, \"b\": {\"c\": true}}"));
            final String json = IOUtils.toString(jsonRecord.getAsJson());
            assertNotNull(json);
            assertFalse(json.isEmpty());

            final KeyValueStorage.DoubleRecordView doubleRecord = storage1.getOrCreateRecord(10.50D, KeyValueStorage.DoubleRecordView.class, KeyValueStorage.DoubleRecordView.INITIALIZER);
            assertNotNull(doubleRecord);
            doubleRecord.accept(50L);
            assertEquals(50D, doubleRecord.getAsDouble(), 0.01D);

            final KeyValueStorage.LongRecordView iRecord = storage1.getOrCreateRecord((byte) 25, KeyValueStorage.LongRecordView.class, KeyValueStorage.LongRecordView.INITIALIZER);
            assertNotNull(iRecord);
            iRecord.accept(50L);
            assertEquals(50L, iRecord.getAsLong());

            assertTrue(storage1.exists(KEY));
            assertTrue(storage1.delete(KEY));
            return null;
        });
        thread.get();
    }

    @Test
    public void readAllRecordsTest() throws InterruptedException {
        final KeyValueStorage storage1 = instance1.getService("$testStorage1", ClusterMember.PERSISTENT_KV_STORAGE);
        assertNotNull(storage1);
        storage1.updateOrCreateRecord("1", KeyValueStorage.TextRecordView.class, record -> record.setAsText("Frank Underwood"));
        storage1.updateOrCreateRecord("2", KeyValueStorage.TextRecordView.class, record -> record.setAsText("Barry Burton"));
        Thread.sleep(10_000);
        storage1.forEachRecord(KeyValueStorage.TextRecordView.class, k -> k instanceof String, (key, record) -> {
            if(key.equals("1"))
                assertEquals("Frank Underwood", record.getAsText());
            else if(key.equals("2"))
                assertEquals("Barry Burton", record.getAsText());
            else
                fail("Unexpected key " + key);
            return true;
        });
    }
}
