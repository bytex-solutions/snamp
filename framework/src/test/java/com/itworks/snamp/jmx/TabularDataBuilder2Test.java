package com.itworks.snamp.jmx;

import com.itworks.snamp.SafeConsumer;
import org.junit.Assert;
import org.junit.Test;

import javax.management.openmbean.*;

/**
 * @author Roman Sakno
 * @version 1.0
 * @since 1.0
 */
public final class TabularDataBuilder2Test extends Assert {
    @Test
    public void builderTest() throws OpenDataException {
        final TabularType type = new TabularTypeBuilder()
                .setTypeName("DummyTable", true)
                .setDescription("Description", true)
                .addColumn("column1", "dummy desc", SimpleType.STRING, true)
                .addColumn("column2", "dummy desc", SimpleType.BOOLEAN, false)
                .build();
        final TabularData data = new TabularDataBuilder2(type)
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
        TabularDataUtils.forEachRow(data, new SafeConsumer<CompositeData>() {
            @Override
            public void accept(final CompositeData value) {
                assertEquals(2, value.getCompositeType().keySet().size());
                assertTrue(value.get("column1") instanceof String);
                assertNotNull(value.get("column2") instanceof Boolean);
            }
        });
    }
}
