import { TwoDimensionalChartOfAttributeValues } from "./abstract.2d.chart.attributes.values";

export abstract class SeriesBasedChart extends TwoDimensionalChartOfAttributeValues {

    // generate time intervals to crop the older values from being drawing.
    public static generateIntervals():DescriptionIdClass[] {
        let value:DescriptionIdClass[] = [];
        value.push(new DescriptionIdClass(1, "1 minute"));
        value.push(new DescriptionIdClass(5, "5 minutes"));
        value.push(new DescriptionIdClass(15, "15 minutes"));
        value.push(new DescriptionIdClass(60, "1 hour"));
        value.push(new DescriptionIdClass(720, "12 hours"));
        value.push(new DescriptionIdClass(1440, "24 hours"));
        return value;
    }

    // generate time intervals to crop the older values from being drawing.
    public static generateRateIntervals():DescriptionIdClass[] {
        let value:DescriptionIdClass[] = [];
        value.push(new DescriptionIdClass(1, "1 second", "second"));
        value.push(new DescriptionIdClass(1, "1 minute", "minute"));
        value.push(new DescriptionIdClass(5, "5 minutes", "five_minutes"));
        value.push(new DescriptionIdClass(15, "15 minutes", "fifteen_minutes"));
        value.push(new DescriptionIdClass(60, "1 hour", "hour"));
        value.push(new DescriptionIdClass(720, "12 hours", "twelve_hours"));
        value.push(new DescriptionIdClass(1440, "24 hours", "day"));
        return value;
    }
}

export class DescriptionIdClass {
    public id:number = 0;
    public description:string = "";
    public additionalId:string = "";

    constructor(id:any, description:string, additionalId?:string) {
        this.id = id;
        this.description = description;
        this.additionalId = additionalId;
    }
}