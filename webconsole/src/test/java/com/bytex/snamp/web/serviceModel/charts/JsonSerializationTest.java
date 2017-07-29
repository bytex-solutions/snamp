package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.jmx.WellKnownType;
import com.bytex.snamp.web.serviceModel.commons.AttributeInformation;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Roman Sakno
 * @version 2.1
 * @since 2.0
 */
public final class JsonSerializationTest extends Assert {
    private final ObjectMapper jsonSerializer = new ObjectMapper();

    @Test
    public void dashboardSerializationTest() throws IOException {
        Dashboard charts = new Dashboard();
        final LineChartOfAttributeValues chart1 = new LineChartOfAttributeValues();
        chart1.setName("attributes");
        chart1.getPreferences().put("size", "4");
        chart1.setGroupName("api-gateway");
        chart1.getAxisY().setAttributeInfo(new AttributeInformation("memory", WellKnownType.LONG, "bytes"));
        chart1.addResource("192.168.0.1");
        charts.addChart(chart1);

        final PanelOfAttributeValues chart2 = new PanelOfAttributeValues();
        chart2.setName("myPanel");
        chart2.setGroupName("trip-manager");
        chart2.getAxisY().setAttributeInfo(new AttributeInformation("cpu", WellKnownType.DOUBLE, "percents"));
        chart2.addResource("192.168.0.2");
        charts.addChart(chart2);

        final String json = jsonSerializer.writerWithDefaultPrettyPrinter().writeValueAsString(charts);
        assertNotNull(json);
        charts = jsonSerializer.readValue(json, Dashboard.class);
        assertNotNull(charts);
        assertEquals(2, charts.getCharts().size());
    }
}
