import { AbstractChart } from './abstract.chart';
import { ChartData } from './chart.data';

export abstract class ChartOfAttributeValues extends AbstractChart {
    public component:string;
    public instances:string[] = [];

    public newValue(_data:ChartData):void {
        this.chartData.push(_data);
    }
}