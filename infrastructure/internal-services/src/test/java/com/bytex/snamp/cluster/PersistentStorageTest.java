package com.bytex.snamp.cluster;

import com.bytex.snamp.IntBox;
import com.bytex.snamp.core.KeyValueStorage;
import com.bytex.snamp.io.IOUtils;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class PersistentStorageTest extends Assert {
    private DatabaseNode database;

    @Before
    public void setupDatabaseNode() throws Exception {
        database = new DatabaseNode();
        database.startupFromConfiguration().activate();
    }

    @After
    public void shutdownDatabaseNode() throws Exception {
        database.dropDatabases();
        database.shutdown();
        database = null;
    }

    @Test
    public void indexTest() {
        final KeyValueStorage storage = database.getSharedObject("$testStorage");
        assertTrue(storage.isPersistent());
        for(int i = 0; i < 100; i++) {
            assertNotNull(storage.getOrCreateRecord(i, KeyValueStorage.TextRecordView.class, record -> record.setAsText("Hello, world")));
        }
        final Set<? extends Comparable<?>> keys = storage.keySet();
        assertFalse(keys.isEmpty());
        assertEquals(100, keys.size());
        assertTrue(keys.contains(1));
        assertTrue(keys.contains(99));
    }

    @Test
    public void getOrCreateRecordTest() {
        final KeyValueStorage storage = database.getSharedObject("$testStorage");
        assertTrue(storage.isPersistent());
        KeyValueStorage.TextRecordView record = storage.getOrCreateRecord("String Key", KeyValueStorage.TextRecordView.class, KeyValueStorage.TextRecordView.INITIALIZER);
        record.setAsText("Hello, world");
        record = storage.getRecord("String Key", KeyValueStorage.TextRecordView.class).orElse(null);
        assertNotNull(record);
        assertEquals("Hello, world", record.getAsText());
        record.setAsText("Barry Burton");
        record = storage.getRecord("String Key", KeyValueStorage.TextRecordView.class).orElse(null);
        assertNotNull(record);
        assertEquals("Barry Burton", record.getAsText());
        storage.clear();
        assertEquals(0, storage.getSize());
    }

    @Test
    public void differentCollectionsTest(){
        final KeyValueStorage storage1 = database.getSharedObject("$testStorage1");
        assertTrue(storage1.isPersistent());
        final KeyValueStorage storage2 = database.getSharedObject("$testStorage2");
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
        final KeyValueStorage storage1 = database.getSharedObject("$testStorage1");
        assertTrue(storage1.isPersistent());
        KeyValueStorage.TextRecordView record1 = storage1.getOrCreateRecord("Frank Underwood", KeyValueStorage.TextRecordView.class, KeyValueStorage.TextRecordView.INITIALIZER);
        record1.setAsText("Hello, world!");
        KeyValueStorage.TextRecordView record2 = storage1.getOrCreateRecord("Frank Underwood", KeyValueStorage.TextRecordView.class, KeyValueStorage.TextRecordView.INITIALIZER);
        assertEquals(record1.getAsText(), record2.getAsText());
    }

    @Test
    public void differentTypesTest() throws ExecutionException, InterruptedException, AssertionError {
        final String KEY = "Frank Underwood";
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final Future<Boolean> task = executor.submit(() -> {
            final KeyValueStorage storage1 =
                    executor.submit(() -> database.getSharedObject("$testStorage1")).get();
            assertNotNull(storage1);
            assertTrue(storage1.isPersistent());
            storage1.getOrCreateRecord(KEY, KeyValueStorage.TextRecordView.class, KeyValueStorage.TextRecordView.INITIALIZER);

            final KeyValueStorage.SerializableRecordView customRecord = storage1.getRecord(KEY, KeyValueStorage.SerializableRecordView.class)
                    .orElseThrow(AssertionError::new);
            customRecord.setValue(new StringBuffer("Hello, world!"));
            assertTrue(customRecord.getValue() instanceof StringBuffer);

            final KeyValueStorage.MapRecordView mapRecord = storage1.getRecord(KEY, KeyValueStorage.MapRecordView.class)
                    .orElseThrow(AssertionError::new);
            mapRecord.setAsMap(ImmutableMap.of("key1", "value1", "key2", "value2"));
            final Map<String, ?> map = mapRecord.getAsMap();
            assertNotNull(map);
            assertEquals("value1", map.get("key1"));
            assertEquals("value2", map.get("key2"));

            final KeyValueStorage.JsonRecordView jsonRecord = storage1.getRecord(KEY, KeyValueStorage.JsonRecordView.class)
                    .orElseThrow(AssertionError::new);
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
            return true;
        });
        assertTrue(task.get());
    }

    @Test
    public void readAllRecordsTest() throws InterruptedException {
        final KeyValueStorage storage1 = database.getSharedObject("$testStorage1");
        assertTrue(storage1.isPersistent());
        storage1.updateOrCreateRecord("1", KeyValueStorage.TextRecordView.class, record -> record.setAsText("Frank Underwood"));
        storage1.updateOrCreateRecord("2", KeyValueStorage.TextRecordView.class, record -> record.setAsText("Barry Burton"));
        Thread.sleep(10_000);
        assertEquals(2, storage1.getSize());
        final IntBox count = IntBox.of(0);
        storage1.forEachRecord(KeyValueStorage.TextRecordView.class, k -> k instanceof String, (key, record) -> {
            count.incrementAndGet();
            if(key.equals("1"))
                assertEquals("Frank Underwood", record.getAsText());
            else if(key.equals("2"))
                assertEquals("Barry Burton", record.getAsText());
            else
                fail("Unexpected key " + key);
            return true;
        });
        assertEquals(2, count.getAsInt());
        final Set<?> keys = storage1.keySet();
        assertTrue(keys.contains("1"));
        assertTrue(keys.contains("2"));
        assertEquals(2, keys.size());
    }
}
