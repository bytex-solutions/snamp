package com.itworks.snamp.testing.jmx;

import com.itworks.snamp.jmx.TabularDataBuilder;
import com.itworks.snamp.testing.AbstractUnitTest;
import org.junit.Test;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import static com.itworks.snamp.jmx.CompositeDataUtils.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class TabularDataBuilderTest extends AbstractUnitTest<TabularDataBuilder> {

    @Test
    public void buildTest() throws OpenDataException {
        final TabularData table = new TabularDataBuilder()
                .columns()
                .addIntColumn("intColumn", "descr", true)
                .addStringColumn("stringColumn", "descr", false)
                .queryObject(TabularDataBuilder.class)
                .setTypeName("table", true)
                .setTypeDescription("test table", true)
                .add(1, "First")
                .add(2, "Second")
                .add(3, "Third")
                .build();
        assertEquals(3, table.size());
        for(final Object row: table.values()){
            assertTrue(row instanceof CompositeData);
            final CompositeData typedRow = (CompositeData)row;
            switch (getInteger(typedRow, "intColumn", 0)){
                case 1:
                    assertEquals("First", getString(typedRow, "stringColumn", "")); continue;
                case 2:
                    assertEquals("Second", getString(typedRow, "stringColumn", "")); continue;
                case 3:
                    assertEquals("Third", getString(typedRow, "stringColumn", "")); continue;
                default:
                    fail();
            }
        }
    }
}
