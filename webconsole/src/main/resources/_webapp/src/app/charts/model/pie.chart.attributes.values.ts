import { TwoDimensionalChartOfAttributeValues } from './abstract.2d.chart.attributes.values';
import { InstanceNameAxis } from './instance.axis';
import { AttributeValueAxis } from './attribute.value.axis';
import { AbstractChart } from './abstract.chart';
import { ChartData } from './chart.data';

export class PieChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues {
    public type:string = AbstractChart.PIE;

    private _chartObject:Chart = undefined;

    public createDefaultAxisX() {
        return new InstanceNameAxis();
    }

    public createDefaultAxisY() {
        return new AttributeValueAxis();
    }

    public updateData(_data:ChartData):void {
        this._chartObject.addData(_data, this.chartData.length + 1);
    }

    public draw():void {
        var _result = new Chart($("#" + this.id), {
            type: AbstractChart.CHART_TYPE_OF(this.type),
            data: {
                labels: this.getLabels(),
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