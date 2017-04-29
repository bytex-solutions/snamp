import { AbstractChart } from './abstract.chart';
import { ChartData } from './chart.data';

export abstract class ChartOfAttributeValues extends AbstractChart {
    public group:string;
    public resources:string[] = [];

    public newValue(_data:ChartData):void {
        this.chartData.push(_data);
    }
}