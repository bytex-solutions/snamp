import { ChartData } from "./abstract.data";
import { AttributeChartData } from "./attribute.chart.data";
import { AbstractChart } from "../abstract.chart";
import { HealthStatusChartData } from "./health.status.chart.data";
import { ResourceCountData } from "./resource.count.data";
import { ScalingData } from "./scaling.data";
import { VotingData } from "./voting.data";

export class ChartDataFabric {

    public static chartDataFromJSON(chartType:string, _json:any):ChartData {
        let _data:ChartData;
        switch (chartType) {
            case AbstractChart.HEALTH_STATUS:
                _data = new HealthStatusChartData();
                break;
            case AbstractChart.RESOURCE_COUNT:
                _data = new ResourceCountData();
                break;
            case AbstractChart.HBAR:
            case AbstractChart.LINE:
            case AbstractChart.PANEL:
            case AbstractChart.PIE:
            case AbstractChart.VBAR:
                _data = new AttributeChartData();
                break;
            case AbstractChart.SCALE_IN:
            case AbstractChart.SCALE_OUT:
                _data = new ScalingData();
                break;
            case AbstractChart.VOTING:
                _data = new VotingData();
                break;
            default:
                throw new Error("Unrecognized chart type for constructing the chart data: " + chartType);

        }
        _data.fillFromJSON(_json);
        _data.chartType = chartType;
        return _data;
    }
}