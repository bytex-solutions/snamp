import { TwoDimensionalChartOfAttributeValues } from '../abstract.2d.chart.attributes.values';
import { ChronoAxis } from '../axis/chrono.axis';
import { AttributeValueAxis } from '../axis/attribute.value.axis';
import { AbstractChart } from '../abstract.chart';
import { AttributeChartData } from "../data/attribute.chart.data";

const d3 = require('d3');
const nv = require('nvd3');

export class LineChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues {
    get type():string {
        return AbstractChart.LINE;
    }

    private _chartObject:any = undefined;

    public createDefaultAxisX() {
        return new ChronoAxis();
    }

    public createDefaultAxisY() {
        return new AttributeValueAxis();
    }

    constructor() {
        super();
        this.setSizeX(20);
        this.setSizeY(10);
    }

    private prepareDatasets():any {
        let _value:any[] = [];
        for (let i = 0; i < this.resources.length; i++) {
            let _currentValue:any = {};
            _currentValue.key = this.resources[i];
            _currentValue.values = [];
            for (let j = 0; j < this.chartData.length; j++) {
                if (this.resources[i] == (<AttributeChartData>this.chartData[j]).resourceName) {
                    _currentValue.values.push({x: this.chartData[j].timestamp, y: (<AttributeChartData>this.chartData[j]).attributeValue});
                }

            }
            _value.push(_currentValue);
        }
        return _value;
    }

    public newValue(_data:AttributeChartData):void {
        this.chartData.push(_data);
        let _index:number = this.chartData.length - 1;
        if (this._chartObject != undefined) {
            let _ds:any[] = d3.select('#' + this.id).datum();
            let _found:boolean = false;
            for (let i = 0; i < _ds.length; i++) {
                if (_ds[i].key == _data.resourceName) {
                    _ds[i].values.push({x: _data.timestamp, y: _data.attributeValue});
                    _found = true;
                    if ((new Date().getTime() - (<Date>_ds[i].values[0].x).getTime()) > this.preferences["interval"] * 60 * 1000) {
                        _ds[i].values.shift(); // remove first element in case it's out of interval range
                    }
                    break;
                }
            }
            if (!_found) {
                _ds = this.prepareDatasets();
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
                .axisLabel((<AttributeValueAxis>_thisReference.getAxisY()).sourceAttribute.unitOfMeasurement);

            chart.x2Axis.tickFormat(function (d) { return ''; });

            d3.select('#' + _thisReference.id).datum(_thisReference.prepareDatasets())
                .transition().call(chart);

            nv.utils.windowResize(chart.update);
            _thisReference._chartObject = chart;
            return chart;
        });
    }

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        _value["group"] = this.group;
        _value["resources"] = this.resources;
        _value["X"] = this.getAxisX().toJSON();
        _value["Y"] = this.getAxisY().toJSON();
        if (!$.isEmptyObject(this.preferences)) {
            _value["preferences"] = this.preferences;
        }
        return _value;
    }
}