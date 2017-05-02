import { ChartData } from "./abstract.data";
import { AttributeChartData } from "./attribute.chart.data";
import { AbstractChart } from "../abstract.chart";
import { HealthStatusChartData } from "./health.status.chart.data";

export class ChartDataFabric {

    public static chartDataFromJSON(chartType:string, _json:any):ChartData {
        let _data:ChartData;
        if (chartType != AbstractChart.HEALTH_STATUS) {
            _data = new AttributeChartData();
        } else {
            _data = new HealthStatusChartData();
        }
        _data.fillFromJSON(_json);
        _data.chartType = chartType;
        return _data;
    }
}