import { ChronoAxis } from "./axis/chrono.axis";
import { NumericAxis } from "./axis/numeric.axis";
import { TwoDimensionalChart } from "./two.dimensional.chart";
import { DescriptionIdClass } from "./abstract.line.based.chart";
import { ScalingData } from "./data/scaling.data";
import { ChartWithGroupName } from "./charts/group.name.based.chart";

const d3 = require('d3');
const nv = require('nvd3');

export abstract class ScalingRateChart extends TwoDimensionalChart implements ChartWithGroupName {

    public group:string = "";
    public metrics:string[] = [];
    public interval:string = "";

    private _chartObject:any = undefined;

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

    public createDefaultAxisX()
    {
        return new ChronoAxis();
    }

    public createDefaultAxisY() {
        return new NumericAxis();
    }

    constructor() {
        super();
        this.setSizeX(15);
        this.setSizeY(10);
    }

    private prepareDatasets():any {
        let _value:any[] = [];
        for (let i = 0; i < this.metrics.length; i++) {
            let _currentValue:any = {};
            _currentValue.key = this.metrics[i];
            _currentValue.values = [];
            for (let j = 0; j < this.chartData.length; j++) {
                if (this.metrics[i] == (<ScalingData>this.chartData[j]).type) {
                    _currentValue.values.push({x: this.chartData[j].timestamp, y: (<ScalingData>this.chartData[j]).value});
                }
            }
            _value.push(_currentValue);
        }
        return _value;
    }

    public newValues(allData:ScalingData[]):void {
        if (document.hidden) return;
        this.chartData.push(...allData);
        if (this._chartObject != undefined) {
            let _ds:any[] = d3.select('#' + this.id).datum();
            if (_ds.length != allData.length) {
                _ds = this.prepareDatasets();
            } else {
                for (let i = 0; i < _ds.length; i++) {
                    for (let j = 0; j < allData.length; j++) {
                        if (_ds[i].key == allData[j].type) {
                            _ds[i].values.push({x: allData[j].timestamp, y: allData[j].value});
                            if ((new Date().getTime() - (<Date>_ds[i].values[0].x).getTime()) > this.preferences["interval"] * 60 * 1000) {
                                _ds[i].values.shift(); // remove first element in case it's out of interval range
                            }
                            break;
                        }
                    }
                }
            }
            this._chartObject.update();
        }
    }

    public draw():void {
        let _thisReference = this;
        nv.addGraph(function() {
            let chart = nv.models.lineWithFocusChart();
            chart.interactiveUpdateDelay(0);
            d3.rebind('clipVoronoi');
            chart.clipVoronoi(false);

            chart.xAxis.tickFormat(function(d){
                return d3.time.format('%X')(new Date(d));
            });

            chart.xScale(d3.time.scale());

            chart.yAxis
                .tickFormat(d3.format('d'))
                .axisLabel((<NumericAxis>_thisReference.getAxisY()).unitOfMeasurement);

            chart.x2Axis.tickFormat(function (d) { return ''; });

            d3.select('#' + _thisReference.id).datum(_thisReference.prepareDatasets())
                .transition().call(chart);

            nv.utils.windowResize(chart.update);
            _thisReference._chartObject = chart;
            return chart;
        });
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