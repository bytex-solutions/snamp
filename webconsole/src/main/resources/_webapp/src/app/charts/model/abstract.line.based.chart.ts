import { TwoDimensionalChartOfAttributeValues } from "./abstract.2d.chart.attributes.values";

export abstract class SeriesBasedChart extends TwoDimensionalChartOfAttributeValues {

    // generate time intervals to crop the older values from being drawing.
    public static generateIntervals():TimeInterval[] {
        let value:TimeInterval[] = [];
        value.push(new TimeInterval(1, "1 minute"));
        value.push(new TimeInterval(5, "5 minutes"));
        value.push(new TimeInterval(15, "15 minutes"));
        value.push(new TimeInterval(60, "1 hour"));
        value.push(new TimeInterval(720, "12 hours"));
        value.push(new TimeInterval(1440, "24 hours"));
        return value;
    }
}

export class TimeInterval {
    public id:number = 0;
    public description:string = "";

    constructor(id:number, description:string) {
        this.id = id;
        this.description = description;
    }
}