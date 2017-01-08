package com.bytex.snamp.web.serviceModel.charts;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
public final class JsonSerializationTest extends Assert {
    private final ObjectMapper jsonSerializer = new ObjectMapper();

    @Test
    public void dashboardSerializationTest() throws IOException {
        Dashboard charts = new Dashboard();
        final LineChartOfAttributeValues chart = new LineChartOfAttributeValues();
        chart.setName("attributes");
        chart.setComponentType("API Gateway");
        charts.addChart(chart);
        final String json = jsonSerializer.writeValueAsString(charts);
        assertNotNull(json);
        charts = jsonSerializer.readValue(json, Dashboard.class);
        assertNotNull(charts);
        assertEquals(1, charts.getCharts().size());
    }
}
