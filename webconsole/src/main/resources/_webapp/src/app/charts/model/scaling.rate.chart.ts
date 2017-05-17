import { ChronoAxis } from "./axis/chrono.axis";
import { NumericAxis } from "./axis/numeric.axis";
import { TwoDimensionalChart } from "./two.dimensional.chart";
import { ChartData } from "./data/abstract.data";

export abstract class ScalingRateChart extends TwoDimensionalChart {

    public group:string = "";
    public metrics:string[] = [];
    public interval:string = "";

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