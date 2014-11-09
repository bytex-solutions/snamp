package com.itworks.snamp.testing;

import com.google.common.base.Supplier;
import com.itworks.snamp.InMemoryTable;
import com.itworks.snamp.Table;
import com.itworks.snamp.TableFactory;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.itworks.snamp.TableFactory.STRING_TABLE_FACTORY;

/**
 * @author Roman Sakno
 */
public class InMemoryTableTest extends AbstractUnitTest<InMemoryTable> {

    public InMemoryTableTest(){
        super(InMemoryTable.class);
    }

    @Test
    public void tableModificaitonTest(){
        final Table<String> table = STRING_TABLE_FACTORY.create(new HashMap<String, Class<?>>(){{
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

    @Test
    public final void fromArrayTest(){
        final Map<String, Object>[] rows = new Map[2];
        Map<String, Object> row = rows[0] = new HashMap<>(3);
        row.put("key1", 1L);
        row.put("key2", "2");
        row.put("key3", true);
        row = rows[1] = new HashMap<>(3);
        row.put("key1", 42L);
        row.put("key2", "hello");
        row.put("key3", false);
        final Table<String> t = STRING_TABLE_FACTORY.fromArray(rows);
        assertEquals(2, t.getRowCount());
        assertEquals(Boolean.TRUE, t.getCell("key3", 0));
        assertEquals(Object.class, t.getColumnType("key2"));
    }

    public static final class RowBean{
        private int column1;
        private String column2;

        public RowBean(){

        }

        public int getColumn1() {
            return column1;
        }

        public void setColumn1(final int column1) {
            this.column1 = column1;
        }

        public String getColumn2() {
            return column2;
        }

        public void setColumn2(final String column2) {
            this.column2 = column2;
        }
    }

    @Test
    public final void fromJavaBeanTest() throws IntrospectionException, ReflectiveOperationException {
        final RowBean row1 = new RowBean();
        row1.setColumn1(42);
        row1.setColumn2("string1");
        final RowBean row2 = new RowBean();
        row2.setColumn1(43);
        row2.setColumn2("string2");
        final Table<String> t = TableFactory.fromBeans(RowBean.class,
                new RowBean[]{row1, row2}, "column1", "column2");
        assertEquals(2, t.getRowCount());
        assertEquals(42, t.getCell("column1", 0));
        assertEquals("string1", t.getCell("column2", 0));
        assertEquals(43, t.getCell("column1", 1));
        assertEquals("string2", t.getCell("column2", 1));
    }

    @Test
    public final void toJavaBeanTest() throws IntrospectionException, ReflectiveOperationException {
        final Table<String> t = STRING_TABLE_FACTORY.create(new HashMap<String, Class<?>>(2){{
            put("column1", Integer.class);
            put("column2", String.class);
        }});
        t.addRow(new HashMap<String, Object>(1){{
            put("column1", 42);
            put("column2", "string1");
        }});
        t.addRow(new HashMap<String, Object>(1){{
            put("column1", 43);
            put("column2", "string2");
        }});
        final List<RowBean> rows = TableFactory.toBeans(RowBean.class, new Supplier<RowBean>() {
            @Override
            public RowBean get() {
                return new RowBean();
            }
        }, t);
        assertEquals(2, rows.size());
        assertEquals(42, rows.get(0).getColumn1());
        assertEquals("string1", rows.get(0).getColumn2());
        assertEquals(43, rows.get(1).getColumn1());
        assertEquals("string2", rows.get(1).getColumn2());
    }
}
