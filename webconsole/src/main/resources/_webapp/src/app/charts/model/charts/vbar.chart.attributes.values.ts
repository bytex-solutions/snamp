import { ResourceNameAxis } from '../axis/resource.name.axis';
import { AttributeValueAxis } from '../axis/attribute.value.axis';
import { AbstractChart } from '../abstract.chart';
import { AttributeChartData } from "../data/attribute.chart.data";
import { ChartJsChart } from "../abstract.chartjs.chart";

const Chart = require('chart.js');

export class VerticalBarChartOfAttributeValues extends ChartJsChart {
    get type():string {
        return AbstractChart.VBAR;
    }

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
        Chart.defaults.global.maintainAspectRatio = false;
    }

    public draw():void {
        this.updateColors();
        this._chartObject = new Chart($("#" + this.id), {
            type: "bar",
            data: {
                labels: this.chartData.map(data => (<AttributeChartData>data).resourceName),
                datasets: [{
                    label: (<AttributeValueAxis>this.getAxisY()).getLabelRepresentation(),
                    data: this.chartData.map(data => (<AttributeChartData>data).attributeValue),
                    backgroundColor : this._backgroundColors,
                    borderColor: this._borderColorData,
                    hoverBackgroundColor: this._backgroundHoverColors,
                    borderWidth: 1
                }],
                options: {
                    animation: {
                        duration: 0
                    },
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