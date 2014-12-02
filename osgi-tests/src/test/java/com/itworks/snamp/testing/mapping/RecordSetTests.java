package com.itworks.snamp.testing.mapping;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.itworks.snamp.ExceptionPlaceholder;
import com.itworks.snamp.mapping.*;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
        final RecordSet<String, Integer> set = RecordSetUtils.fromMap(map);
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
}
