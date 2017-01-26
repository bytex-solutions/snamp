import { TwoDimensionalChartOfAttributeValues } from './abstract.2d.chart.attributes.values';
import { InstanceNameAxis } from './instance.axis';
import { AttributeValueAxis } from './attribute.value.axis';
import { AbstractChart } from './abstract.chart';
import { ChartData } from './chart.data';

const Chart = require('chart.js')

export class PieChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues {
    public type:string = AbstractChart.PIE;

    private _chartObject:any = undefined;

    public createDefaultAxisX() {
        return new InstanceNameAxis();
    }

    public createDefaultAxisY() {
        return new AttributeValueAxis();
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
            this.chartData.push(_data); // if no data with this instance is found - append it to array
            _index = this.chartData.length; // and set it to the end of the array
            this._chartObject.addData(_data, this.chartData.length);
        } else {
            this._chartObject.datasets[1].points[_index].value = _data.attributeValue;
        }
    }

    public draw():void {
        var ctx = $("#" + this.id);
        console.log("Prepared chart data: ", AbstractChart.CHART_TYPE_OF(this.type), this.instances,
            (<AttributeValueAxis>this.getAxisY()).getLabelRepresentation(), this.simplifyData());
        var _result = new Chart(ctx, {
            type: AbstractChart.CHART_TYPE_OF(this.type),
            data: {
                labels: this.instances,
                datasets: [{
                    label: (<AttributeValueAxis>this.getAxisY()).getLabelRepresentation(),
                    data: this.simplifyData(),
                    borderWidth: 1
                }]
            },
            options: {
                scales: {
                    yAxes: [{
                        ticks: {
                            beginAtZero:true
                        }
                    }]
                }
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