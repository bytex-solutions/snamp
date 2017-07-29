package com.bytex.snamp.jmx;

import org.junit.Assert;
import org.junit.Test;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularType;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 1.0
 */
public final class TabularDataBuilderRowFillTest extends Assert {
    @Test
    public void builderTest() throws OpenDataException {
        final TabularType type = new TabularTypeBuilder()
                .setTypeName("DummyTable", true)
                .setDescription("Description", true)
                .addColumn("column1", "dummy desc", SimpleType.STRING, true)
                .addColumn("column2", "dummy desc", SimpleType.BOOLEAN, false)
                .build();
        final TabularData data = new TabularDataBuilderRowFill(type)
                .newRow()
                    .cell("column1", "Frank Underwood")
                    .cell("column2", false)
                    .flush()
                .newRow()
                    .cell("column1", "Barry Burton")
                    .cell("column2", true)
                .flush()
                .get();
        assertEquals(2, data.size());
        TabularDataUtils.forEachRow(data, value -> {
            assertEquals(2, value.getCompositeType().keySet().size());
            assertTrue(value.get("column1") instanceof String);
            assertNotNull(value.get("column2") instanceof Boolean);
        });
    }
}
