import { AbstractChart } from './abstract.chart';

export abstract class ChartOfAttributeValues extends AbstractChart {
    public component:string;
    public instances:string[] = [];
}