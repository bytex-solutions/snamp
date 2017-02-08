import { TwoDimensionalChartOfAttributeValues } from './abstract.2d.chart.attributes.values';
import { InstanceNameAxis } from './instance.axis';
import { AttributeValueAxis } from './attribute.value.axis';
import { AbstractChart } from './abstract.chart';
import { ChartData } from './chart.data';

const d3 = require('d3');
const nv = require('nvd3');

export class HorizontalBarChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues {
    public type:string = AbstractChart.HBAR;

    private _chartObject:any = undefined;
    private _svgReadyData:any = undefined;

    public createDefaultAxisX() {
        return new AttributeValueAxis();
    }

    public createDefaultAxisY() {
        return new InstanceNameAxis();
    }

    constructor() {
        super();
        this._svgReadyData = this.prepareDatasets();
    }

    public newValue(_data:ChartData):void {
        let _index:number = -1;
        for (let i = 0; i < this.chartData.length; i++) {
            if (this.chartData[i].instanceName == _data.instanceName) {
                _index = i; // remember the index
                this.chartData[i] = _data; // change the data
                break;
            }
        }
        if (_index == -1) {
            this.chartData.push(_data); // if no data with this instance is found - append it to an array
            this._svgReadyData.push({
                    key: _data.instanceName,
                    values: [{
                        label: _data.attributeValue,
                        value: _data.attributeName
                    }]
                });
        } else {
            for (let i = 0; i < this._svgReadyData.length; i++) {
                if (this._svgReadyData[i].key == _data.instanceName) {
                    this._svgReadyData[i].values[0].value = _data.attributeValue;
                }
            }
        }

        if (this._chartObject != undefined) {
            this._chartObject.update();
        }
    }

    private prepareDatasets():any {
        let _value:any = [];
        for (let i = 0; i < this.chartData.length; i++) {
            _value.push({
                key: this.chartData[i].instanceName,
                values: [{
                    label: this.chartData[i].attributeName,
                    value: this.chartData[i].attributeValue
                }]
            });
        }
        return _value;
    }

    public draw():void {
         // refresh data to be actual in this phase
         this._svgReadyData = this.prepareDatasets();
         var _thisReference = this;
          nv.addGraph(function() {
            var chart = nv.models.multiBarHorizontalChart()
                .x(function(d) { return d.label })
                .y(function(d) { return d.value })
                .barColor(d3.scale.category20().range())
                .showValues(true)
                .stacked(false);

             chart.yAxis
                 .tickFormat(d3.format('d'));

             d3.select('#' + _thisReference.id)
                 .datum(_thisReference._svgReadyData)
                 .call(chart);

             nv.utils.windowResize(chart.update);
             _thisReference._chartObject = chart;
             return chart;
           });
    }

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        _value["component"] = this.component;
        _value["instances"] = this.instances;
        _value["X"] = this.getAxisX().toJSON();
        _value["Y"] = this.getAxisY().toJSON();
        if (!$.isEmptyObject(this.preferences)) {
            _value["preferences"] = this.preferences;
        }
        return _value;
    }
}