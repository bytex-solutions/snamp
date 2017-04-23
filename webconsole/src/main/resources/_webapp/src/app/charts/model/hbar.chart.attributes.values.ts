import { TwoDimensionalChartOfAttributeValues } from './abstract.2d.chart.attributes.values';
import { InstanceNameAxis } from './instance.axis';
import { AttributeValueAxis } from './attribute.value.axis';
import { AbstractChart } from './abstract.chart';
import { ChartData } from './chart.data';
import { ChartUtils } from "./chart.utils";

const Chart = require('chart.js');

export class HorizontalBarChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues {
    public type:string = AbstractChart.HBAR;

    private _chartObject:any = undefined;

    public createDefaultAxisX() {
        return new AttributeValueAxis();
    }

    public createDefaultAxisY() {
        return new InstanceNameAxis();
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
        let updateColors:boolean = false;
        if (_index == -1) {
            this.chartData.push(_data); // if no data with this instance is found - append it to array
            _index = this.chartData.length - 1; // and set it to the end of the array
            updateColors = true;
        }
        if (this._chartObject != undefined) {
            this._chartObject.data.datasets[0].data[_index] = _data.attributeValue;
            if (updateColors) {
                this._chartObject.data.datasets[0].backgroundColor = this.chartData.map((data, i) => ChartUtils.hslFromValue(i, this.chartData.length, 0.3));
                this._chartObject.data.datasets[0].borderColor =  new Array(this.chartData.length).fill("#536980");
                this._chartObject.data.datasets[0].hoverBackgroundColor = this.chartData.map((data, i) => ChartUtils.hslFromValue(i, this.chartData.length, 0.75));
            }
            this._chartObject.update();
        }
    }

    protected doDraw():void {
        this._chartObject = new Chart($("#" + this.id), {
            type: 'horizontalBar',
            data: {
                labels: this.instances,
                datasets: [{
                    label: (<AttributeValueAxis>this.getAxisX()).getLabelRepresentation(),
                    data: this.chartData.map(data => data.attributeValue),
                    backgroundColor : this.chartData.map((data, i) => ChartUtils.hslFromValue(i, this.chartData.length, 0.3)),
                    borderColor: new Array(this.chartData.length).fill("#536980"),
                    hoverBackgroundColor: this.chartData.map((data, i) => ChartUtils.hslFromValue(i, this.chartData.length, 0.75)),
                    borderWidth: 1
                }],
                options: {
                    responsive: true,
                    title: {
                        display: true,
                        text: this.component
                    }
                }
            }
        });
        this.fitToContainer();
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