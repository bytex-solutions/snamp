import { TwoDimensionalChart } from "../two.dimensional.chart";
import { Axis } from "../axis/abstract.axis";
import { AbstractChart } from "../abstract.chart";
import { HealthStatusChartData } from "../data/health.status.chart.data";
import { ChronoAxis } from "../axis/chrono.axis";
import { NumericAxis } from "../axis/numeric.axis";
import {ResourceCountData} from "../data/resource.count.data";

const d3 = require('d3');
const nv = require('nvd3');

export class NumberOfResourcesChart extends TwoDimensionalChart {
    get type():string {
        return AbstractChart.RESOURCE_COUNT;
    }

    public group:string;
    private _chartObject:any = undefined;

    createDefaultAxisX(): Axis {
        return new ChronoAxis();
    }

    createDefaultAxisY(): Axis {
        let _na =  new NumericAxis();
        _na.unitOfMeasurement = "resources";
        _na.name = "resources";
        return _na;
    }

    constructor() {
        super();
        this.setSizeX(20);
        this.setSizeY(10);
    }

    toJSON(): any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        _value["group"] = this.group;
        _value["X"] = this.getAxisX().toJSON();
        _value["Y"] = this.getAxisY().toJSON();
        if (!$.isEmptyObject(this.preferences)) {
            _value["preferences"] = this.preferences;
        }
        return _value;
    }

    public draw(): void {

    }

    private prepareDatasets():any {

    }

    public newValues(_data:ResourceCountData[]):void {
        if (document.hidden) return;
        console.log("Data received for chart is: ", _data);
        this.chartData = _data;
        if (this._chartObject != undefined) {
            //let _data:any[] = this.prepareDatasets();

        }
    }

    public newValue(_data:HealthStatusChartData):void {
        if (document.hidden) return;
        // do nothing
    }
}