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

    public newValue(_data:ChartData):void {
        this.chartData.push(_data);
        let _index:number = this.chartData.length - 1;
        if (this._chartObject != undefined) {
            this._chartObject.data.datasets[0].data[_index] = _data.attributeValue;
            this._chartObject.update();
        }
    }

    public draw():void    {
        var ctx = $("#" + this.id);
        console.log("Prepared chart data: ", ctx, AbstractChart.CHART_TYPE_OF(this.type), this.instances,
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