package com.bytex.snamp.web.serviceModel.charts;

import com.bytex.snamp.web.serviceModel.WebConsoleService;
import com.bytex.snamp.web.serviceModel.WebMessage;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Sakno
 * @version 2.0
 * @since 2.0
 */
@JsonTypeName("chartData")
public final class ChartDataMessage extends WebMessage {
    private static final long serialVersionUID = 2810215967189225444L;
    private final Map<String, ChartData> chartData;

    ChartDataMessage(final WebConsoleService source) {
        super(source);
        chartData = new HashMap<>();
    }

    @JsonProperty("dataForCharts")
    public Map<String, ChartData> getChartData(){
        return chartData;
    }
}
