import { TwoDimensionalChartOfAttributeValues } from './abstract.2d.chart.attributes.values';
import { ChronoAxis } from './chrono.axis';
import { AttributeValueAxis } from './attribute.value.axis';
import { AbstractChart } from './abstract.chart';
import { ChartData } from './chart.data';

const Chart = require('chart.js')

export class LineChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues {
    public type:string = AbstractChart.LINE;

    private _chartObject:any = undefined;

    public createDefaultAxisX() {
        return new ChronoAxis();
    }

    public createDefaultAxisY() {
        return new AttributeValueAxis();
    }

    private prepareDatasets():any {
        let _value:any[] = [];
        for (let i = 0; i < this.instances.length; i++) {
            let _currentValue:any = {};
            _currentValue.label = this.instances[i];
            _currentValue.data = [];
            _currentValue.borderColor = AbstractChart.hslFromValue(i, this.instances.length, 0.7);
            _currentValue.strokeColor =  AbstractChart.hslFromValue(i, this.instances.length, 0.1   );
            _currentValue.pointColor =  AbstractChart.hslFromValue(i, this.instances.length, 0.1);
            _currentValue.pointStrokeColor =  AbstractChart.hslFromValue(i, this.instances.length, 0.1);
            _currentValue.pointHighlightStroke =  AbstractChart.hslFromValue(i, this.instances.length, 0.1);
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
        console.log("new data for line chart is: ", _data);
        this.chartData.push(_data);
        let _index:number = this.chartData.length - 1;
        if (this._chartObject != undefined) {
            let _ds:any[] = this._chartObject.data.datasets;
            let _found:boolean = false;
            for (let i = 0; i < _ds.length; i++) {
                if (_ds[i].label == _data.instanceName) {
                    _ds[i].data.push({x: _data.timestamp, y: _data.attributeValue});
                    _found = true;
                    break;
                }
            }
            if (!_found) {
                this._chartObject.data.datasets = this.prepareDatasets();
            }
            this._chartObject.update();
        }
    }

    public draw():void    {
        var ctx = $("#" + this.id);
        var _result = new Chart(ctx, {
            type: AbstractChart.CHART_TYPE_OF(this.type),
            data: {
                labels: this.instances,
                datasets: this.prepareDatasets()
            },
              options: {
                animation: {
                    duration: 200,
                    easing: 'linear'
                },
                scales: {
                  xAxes: [{
                    type: 'time',
                    time: {
                      time: {
                          unit: 'millisecond'
                      }
                    }
                  }],
                },
              }
        });
        this._chartObject = _result;
    }

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        _value["component"] = this.component;
        _value["instances"] = this.instances;
        _value["X"] = this.getAxisX().toJSON();
        _value["Y"] = this.getAxisY().toJSON();
        return _value;
    }
}