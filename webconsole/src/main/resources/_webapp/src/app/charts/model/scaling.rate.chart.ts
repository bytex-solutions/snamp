import { ChronoAxis } from "./axis/chrono.axis";
import { NumericAxis } from "./axis/numeric.axis";
import { TwoDimensionalChart } from "./two.dimensional.chart";
import { ChartData } from "./data/abstract.data";
import { DescriptionIdClass } from "./abstract.line.based.chart";

export abstract class ScalingRateChart extends TwoDimensionalChart {

    public group:string = "";
    public metrics:string[] = [];
    public interval:string = "";

    /**
     * LAST_RATE, MEAN_RATE, MAX_RATE, LAST_MAX_RATE_PER_SECOND, LAST_MAX_RATE_PER_MINUTE, LAST_MAX_RATE_PER_12_HOURS
     */

    public static prepareRareMetrics():DescriptionIdClass[] {
        return [
            new DescriptionIdClass("last_rate",                  "Measured rate of actions for the last time"),
            new DescriptionIdClass("mean_rate",                  "Mean rate of actions per unit of time from the historical perspective"),
            new DescriptionIdClass("max_rate",                   "Max rate of actions observed in the specified interval"),
            new DescriptionIdClass("last_max_rate_per_second",   "Max rate of actions received per second for the last time"),
            new DescriptionIdClass("last_max_rate_per_minute",   "Max rate of actions received per minute for the last time"),
            new DescriptionIdClass("last_max_rate_per_12_hours", "Max rate of actions received per 12 hours for the last time")
        ];
    }

    public createDefaultAxisX() {
        return new ChronoAxis();
    }

    public createDefaultAxisY() {
        return new NumericAxis();
    }

    constructor() {
        super();
        this.setSizeX(10);
        this.setSizeY(10);
    }

    public newValues(data:ChartData[]):void {
        console.log("Data for chart of type ", this.type, " is: ", data);
    }

    public draw():void {

    }

    toJSON(): any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        _value["group"] = this.group;
        _value["metrics"] = this.metrics;
        _value["interval"] = this.interval;
        _value["X"] = this.getAxisX().toJSON();
        _value["Y"] = this.getAxisY().toJSON();
        if (!$.isEmptyObject(this.preferences)) {
            _value["preferences"] = this.preferences;
        }
        return _value;
    }
}