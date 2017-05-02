import { TwoDimensionalChart } from "./two.dimensional.chart";
import { ChartData } from "./data/abstract.data";

export abstract class ChartOfAttributeValues extends TwoDimensionalChart {
    public group:string;
    public resources:string[] = [];

    public newValue(_data:ChartData):void {
        this.chartData.push(_data);
    }
}