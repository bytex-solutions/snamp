import { Axis } from "../axis/abstract.axis";
import { AbstractChart } from "../abstract.chart";
import { ChronoAxis } from "../axis/chrono.axis";
import { NumericAxis } from "../axis/numeric.axis";
import { ResourceCountData } from "../data/resource.count.data";
import { SeriesBasedChart } from "../abstract.line.based.chart";
import {isNullOrUndefined} from "util";

const d3 = require('d3');
const nv = require('nvd3');

export class NumberOfResourcesChart extends SeriesBasedChart {
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

    private prepareDatasets():any {
        let _value:any[] = [{ key: this.group, values: []}];
        for (let i = 0; i < this.chartData.length; i++) {
            _value[0].values.push({x: this.chartData[i].timestamp, y: (<ResourceCountData>this.chartData[i]).count});

        }
        return _value;
    }

    public newValues(_data:ResourceCountData[]):void { // its guaranteed the only element of array exists
        if (document.hidden || isNullOrUndefined(_data)) return;
        this.chartData.push(_data[0]);
        if (!isNullOrUndefined(this._chartObject != undefined)) {
            let _ds:any[] = d3.select('#' + this.id).datum();
            if (_ds.length == 0) {
                _ds = this.prepareDatasets();
            } else {
                _ds[0].values.push({ x: _data[0].timestamp, y: _data[0].count});
                if ((new Date().getTime() - (<Date>_ds[0].values[0].x).getTime()) > this.preferences["interval"] * 60 * 1000) {
                    _ds[0].values.shift(); // remove first element in case it's out of interval range
                }
            }
            this._chartObject.update();
        } else {
            this.draw();
        }
    }
}