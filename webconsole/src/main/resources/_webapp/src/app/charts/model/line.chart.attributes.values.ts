import { TwoDimensionalChartOfAttributeValues } from './abstract.2d.chart.attributes.values';
import { ChronoAxis } from './chrono.axis';
import { AttributeValueAxis } from './attribute.value.axis';
import { AbstractChart } from './abstract.chart';
import { ChartData } from './chart.data';

const d3 = require('d3');
const nv = require('nvd3');

export class LineChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues {
    public type:string = AbstractChart.LINE;

    private _chartObject:any = undefined;

    public createDefaultAxisX() {
        return new ChronoAxis();
    }

    public createDefaultAxisY() {
        return new AttributeValueAxis();
    }

    constructor() {
        super();
        this.setSizeX(6);
        this.setSizeY(3);
    }

    private prepareDatasets():any {
        let _value:any[] = [];
        for (let i = 0; i < this.instances.length; i++) {
            let _currentValue:any = {};
            _currentValue.key = this.instances[i];
            _currentValue.values = [];
            for (let j = 0; j < this.chartData.length; j++) {
                if (this.instances[i] == this.chartData[j].instanceName) {
                    _currentValue.values.push({x: this.chartData[j].timestamp, y: this.chartData[i].attributeValue});
                }

            }
            _value.push(_currentValue);
        }
        return _value;
    }

    public newValue(_data:ChartData):void {
        this.chartData.push(_data);
        let _index:number = this.chartData.length - 1;
        if (this._chartObject != undefined) {
            let _ds:any[] = d3.select('#' + this.id).datum();
            let _found:boolean = false;
            for (let i = 0; i < _ds.length; i++) {
                if (_ds[i].key == _data.instanceName) {
                    _ds[i].values.push({x: _data.timestamp, y: _data.attributeValue});
                    _found = true;
                    if (_ds.length > 200) {
                        _ds.shift(); // remove first element in case we have too many elements
                    }
                    break;
                }
            }
            if (!_found) {
                _ds = this.prepareDatasets();
            }
            d3.select('#' + this.id).datum(_ds).transition().duration(100).call(this._chartObject);
        }
    }


    public draw():void    {
       var _thisReference = this;
        nv.addGraph(function() {
          var chart = nv.models.lineWithFocusChart();

          chart.xAxis.tickFormat(function(d){
                return d3.time.format('%X')(new Date(d));
          });

          chart.xScale(d3.time.scale());

          chart.yAxis
              .tickFormat(d3.format('d'));

          chart.y2Axis.tickFormat(function(d){
                 return d3.time.format('%H:%M:%S')(new Date(d));
           });

          d3.select('#' + _thisReference.id).datum(_thisReference.prepareDatasets())
            .transition().duration(500).call(chart);

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
        if ($.isEmptyObject(this.preferences)) {
            _value["preferences"] = this.preferences;
        }
        return _value;
    }
}