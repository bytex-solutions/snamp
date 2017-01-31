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

// random data section
    private bump(a, m):any {
        var x = 1 / (.1 + Math.random()),
            y = 2 * Math.random() - .5,
            z = 10 / (.1 + Math.random());
        for (var i = 0; i < m; i++) {
          var w = (i / m - y) * z;
          a[i] += x * Math.exp(-w * w);
        }
      }

    private stream_layers(n, m, o):any {
      var _thisReference = this;
      return d3.range(n).map(function() {
          var a = [], i;
          for (i = 0; i < m; i++) a[i] = o + o * Math.random();
          for (i = 0; i < 5; i++) _thisReference.bump(a, m);
          return a.map(_thisReference.stream_index);
        });
    }

     private stream_index(d, i):any {
        return {x: i, y: Math.max(0, d)};
    }
// random data section


    private prepareDatasets():any {
        let _value:any[] = [];
        for (let i = 0; i < this.instances.length; i++) {
            let _currentValue:any = {};
            _currentValue.label = this.instances[i];
            _currentValue.data = [];
            _currentValue.borderColor = AbstractChart.hslFromValue(i, this.instances.length, 0.7);
            _currentValue.pointColor =  AbstractChart.hslFromValue(i, this.instances.length, 0.1);
            _currentValue.fill = false;
            _currentValue.radius = 1;
            _currentValue.borderWidth = 1;
            for (let j = 0; j < this.chartData.length; j++) {
                if (this.instances[i] == this.chartData[j].instanceName) {
                    _currentValue.data.push({x: _currentValue.data.timestamp, y: this.chartData[i].attributeValue});
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
            this._chartObject.update();
        }
    }

    private testData():any {
      return this.stream_layers(3,128,.1).map(function(data, i) {
        return {
          key: 'Stream' + i,
          values: data
        };
      });
    }

    public draw():void    {
       var _thisReference = this;
        nv.addGraph(function() {
          var chart = nv.models.lineWithFocusChart();

          chart.xAxis
              .tickFormat(d3.format(',f'));

          chart.yAxis
              .tickFormat(d3.format(',.2f'));

          chart.y2Axis
              .tickFormat(d3.format(',.2f'));

          d3.select('#' + _thisReference.id)
              .datum(_thisReference.testData())
              .transition().duration(500)
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
        if ($.isEmptyObject(this.preferences)) {
            _value["preferences"] = this.preferences;
        }
        return _value;
    }
}