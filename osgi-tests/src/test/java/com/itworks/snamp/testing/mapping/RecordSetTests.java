package com.itworks.snamp.testing.mapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.mapping.*;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public class RecordSetTests extends AbstractUnitTest<RecordSet> {
    @Test
    public void assignableTest(){
        final RecordSet<String, String> set = new KeyedRecordSet<String, String>() {
            @Override
            protected Set<String> getKeys() {
                return null;
            }

            @Override
            protected String getRecord(final String key) {
                return null;
            }
        };
        assertTrue(TypeLiterals.isInstance(set, TypeLiterals.NAMED_RECORD_SET));
    }

    @Test(expected = MalformedURLException.class)
    public void exceptionTypeTest() throws MalformedURLException {
        final RecordReader<String, Object, MalformedURLException> reader = new RecordReader<String, Object, MalformedURLException>() {
            @Override
            public void read(final String index, final Object value) throws MalformedURLException {
                if(Objects.equals(index, "Failed"))
                    throw new MalformedURLException();
            }
        };
        assertNotNull(RecordSetUtils.getExceptionType(reader));
        assertEquals(MalformedURLException.class.getCanonicalName(), RecordSetUtils.getExceptionType(reader).getCanonicalName());
        final RecordSet<String, Object> set = RecordSetUtils.fromMap(ImmutableMap.<String, Object>of("Idx", 0, "Failed", 1));
        set.parallel(Executors.newFixedThreadPool(2)).forEach(reader);
    }

    @Test
    public void fromArrayTest() throws Exception {
        final Long[] array = {1L, 3L, 5L, 7L, 9L};
        final RecordSet<Integer, Long> set =  RecordSetUtils.fromArray(array);
        assertNotNull(set);
        assertEquals(array.length, set.size());
        set.sequential().forEach(new RecordReader<Integer, Long, ExceptionPlaceholder>() {
            @Override
            public void read(final Integer index, final Long value) {
                switch (index){
                    case 0: assertEquals(1L, value.longValue()); return;
                    case 1: assertEquals(3L, value.longValue()); return;
                    case 2: assertEquals(5L, value.longValue()); return;
                    case 3: assertEquals(7L, value.longValue()); return;
                    case 4: assertEquals(9L, value.longValue()); return;
                }
            }
        });
        assertNotNull(set.sequential());
        assertNotNull(set.parallel(Executors.newSingleThreadExecutor()));
    }

    @Test
    public void fromListTest() throws Exception {
        final List<Long> list = ImmutableList.of(1L, 3L, 5L, 7L, 9L);
        final RecordSet<Integer, Long> set =  RecordSetUtils.fromList(list);
        assertNotNull(set);
        assertEquals(list.size(), set.size());
        set.sequential().forEach(new RecordReader<Integer, Long, ExceptionPlaceholder>() {
            @Override
            public void read(final Integer index, final Long value) {
                switch (index){
                    case 0: assertEquals(1L, value.longValue()); return;
                    case 1: assertEquals(3L, value.longValue()); return;
                    case 2: assertEquals(5L, value.longValue()); return;
                    case 3: assertEquals(7L, value.longValue()); return;
                    case 4: assertEquals(9L, value.longValue()); return;
                }
            }
        });
        assertNotNull(set.sequential());
        assertNotNull(set.parallel(Executors.newSingleThreadExecutor()));
    }

    @Test
    public void fromMapTest() throws Exception {
        final Map<String, Integer> map = ImmutableMap.of("One", 1,
                "Two", 2,
                "Three", 3);
        final RecordSet<String, Integer> set = new KeyedRecordSet<String, Integer>() {
            @Override
            protected Set<String> getKeys() {
                return map.keySet();
            }

            @Override
            protected Integer getRecord(final String key) {
                return map.get(key);
            }
        };
        assertTrue(TypeLiterals.isInstance(set, TypeLiterals.NAMED_RECORD_SET));
        assertEquals(map.size(), set.size());
        set.sequential().forEach(new RecordReader<String, Integer, ExceptionPlaceholder>() {
            @Override
            public void read(final String index, final Integer value) {
                switch (index){
                    case "One": assertEquals(1, value.intValue());return;
                    case "Two": assertEquals(2, value.intValue());return;
                    case "Three": assertEquals(3, value.intValue());return;
                }
            }
        });
        assertNotNull(set.parallel(Executors.newSingleThreadExecutor()));
    }

    @Test
    public void addRowTest(){
        RowSet<String> set = RecordSetUtils.singleRow(Collections.singletonMap("Column 1", "Cell 1"));
        set = RecordSetUtils.addRow(set, Collections.singletonMap("Column 1", "Cell 2"));
        assertEquals(2, set.size());
        assertEquals(1, set.getColumns().size());
        assertTrue(set.getColumns().contains("Column 1"));
        final List<? extends Map<String, String>> rows = RecordSetUtils.toList(set);
        assertEquals(rows.size(), set.size());
        assertEquals("Cell 2", rows.get(1).get("Column 1"));
        assertEquals("Cell 1", rows.get(0).get("Column 1"));
    }

    @Test
    public void insertRowTest(){
        RowSet<String> set = RecordSetUtils.singleRow(Collections.singletonMap("Column 1", "Cell 1"));
        set = RecordSetUtils.insertRow(set, Collections.singletonMap("Column 1", "Cell 2"), 0);
        set = RecordSetUtils.insertRow(set, Collections.singletonMap("Column 1", "Cell 3"), 2);
        assertEquals(3, set.size());
        final List<? extends Map<String, String>> rows = RecordSetUtils.toList(set);
        assertEquals(set.size(), rows.size());
        assertEquals("Cell 2", rows.get(0).get("Column 1"));
        assertEquals("Cell 1", rows.get(1).get("Column 1"));
        assertEquals("Cell 3", rows.get(2).get("Column 1"));
    }

    @Test
    public void removeRowTest() {
        RowSet<String> set = RecordSetUtils.singleRow(Collections.singletonMap("Column 1", "Dummy"));
        assertTrue(TypeLiterals.isInstance(set, TypeLiterals.ROW_SET));
        assertEquals(1, set.size());
        set = RecordSetUtils.removeRow(set, 0);
        assertTrue(TypeLiterals.isInstance(set, TypeLiterals.ROW_SET));
        assertEquals(0, set.size());
        set = RecordSetUtils.fromRows(ImmutableSet.of("Column 1", "Column 2"),
                ImmutableList.of(ImmutableMap.of("Column 1", "Cell 1",
                                "Column 2", "Cell 2",
                                "Column 3", "Cell 3"),
                        ImmutableMap.of("Column 1", "Cell 4",
                                "Column 2", "Cell 5",
                                "Column 3", "Cell 6"),
                        ImmutableMap.of("Column 1", "Cell 7",
                                "Column 2", "Cell 8",
                                "Column 3", "Cell 9")));
        final RowSet<String> modifiedSet1 = RecordSetUtils.removeRow(set, 1);
        assertEquals(set.size() - 1, modifiedSet1.size());
        final List<? extends Map<String, String>> rows = RecordSetUtils.toList(modifiedSet1);
        assertEquals(set.size() - 1, rows.size());
        Map<String, String> row = rows.get(1);
        assertEquals("Cell 8", row.get("Column 2"));
        row = rows.get(0);
        assertEquals("Cell 2", row.get("Column 2"));
    }
}