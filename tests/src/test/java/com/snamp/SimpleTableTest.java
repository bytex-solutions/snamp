package com.snamp;

import org.junit.Test;

import java.util.*;

/**
 * @author Roman Sakno
 */
public class SimpleTableTest extends SnampClassTestSet<SimpleTable<?>> {
    @Test
    public void tableModificaitonTest(){
        final Table<String> table = new SimpleTable<>(new HashMap<String, Class<?>>(){{
            put("COL1", Integer.class);
            put("COL2", String.class);
            put("COL3", Boolean.class);
        }});
        assertEquals(String.class, table.getColumnType("COL2"));
        assertTrue(table.getColumns().size() == 3);
        assertTrue(table.getRowCount() == 0);
        table.addRow(new HashMap<String, Object>(){{
            put("COL1", 10);
            put("COL2", "hello");
            put("COL3", false);
        }});
        table.addRow(new HashMap<String, Object>(){{
            put("COL1", 42);
            put("COL2", "world");
            put("COL3", true);
        }});
        assertTrue(table.getRowCount() == 2);
        assertEquals("world", table.getCell("COL2", 1));
        assertEquals(10, table.getCell("COL1", 0));
        table.setCell("COL1", 0, 22);
        assertEquals(22, table.getCell("COL1", 0));
        table.removeRow(0);
        assertEquals(1, table.getRowCount());
    }
}
