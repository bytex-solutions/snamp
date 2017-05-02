import { TwoDimensionalChartOfAttributeValues } from '../abstract.2d.chart.attributes.values';
import { ResourceNameAxis } from '../axis/resource.name.axis';
import { AttributeValueAxis } from '../axis/attribute.value.axis';
import { AbstractChart } from '../abstract.chart';
import {AttributeChartData} from "../data/attribute.chart.data";

const Chart = require('chart.js');

export class VerticalBarChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues {
    get type():string {
        return AbstractChart.VBAR;
    }

    private _chartObject:any = undefined;

    public createDefaultAxisX() {
        return new ResourceNameAxis();
    }

    public createDefaultAxisY() {
        return new AttributeValueAxis();
    }

    constructor() {
        super();
        this.setSizeX(10);
        this.setSizeY(10);
    }

    public newValue(_data:AttributeChartData):void {
        if (document.hidden) return;
        let _index:number = -1;
        for (let i = 0; i < this.chartData.length; i++) {
            if ((<AttributeChartData>this.chartData[i]).resourceName == _data.resourceName) {
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
                this.updateColors();
                this._chartObject.data.datasets[0].backgroundColor = this._backgroundColors;
                this._chartObject.data.datasets[0].borderColor = this._borderColorData;
                this._chartObject.data.datasets[0].hoverBackgroundColor = this._backgroundHoverColors;
            }
            this._chartObject.update();
        }
    }

    public doDraw():void {
        this._chartObject = new Chart($("#" + this.id), {
            type: "bar",
            data: {
                labels: this.resources,
                datasets: [{
                    label: (<AttributeValueAxis>this.getAxisY()).getLabelRepresentation(),
                    data: this.chartData.map(data => (<AttributeChartData>data).attributeValue),
                    backgroundColor : this._backgroundColors,
                    borderColor: this._borderColorData,
                    hoverBackgroundColor: this._backgroundHoverColors,
                    borderWidth: 1
                }],
                options: {
                    responsive: true,
                    title: {
                        display: true,
                        text: this.group
                    }
                }
            }
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