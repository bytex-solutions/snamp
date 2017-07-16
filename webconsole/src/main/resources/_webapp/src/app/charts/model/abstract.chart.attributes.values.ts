import { TwoDimensionalChart } from "./two.dimensional.chart";

export abstract class ChartOfAttributeValues extends TwoDimensionalChart {
    public group:string;
    public resources:string[] = [];
}