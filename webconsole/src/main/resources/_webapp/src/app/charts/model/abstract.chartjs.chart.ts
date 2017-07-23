import { ChartUtils } from "./chart.utils";
import { TwoDimensionalChartOfAttributeValues } from "./abstract.2d.chart.attributes.values";
import { AttributeChartData } from "./data/attribute.chart.data";
import { isNullOrUndefined } from "util";
const Chart = require('chart.js');

export abstract class ChartJsChart extends TwoDimensionalChartOfAttributeValues {

    // for chartJS purposes
    private static hslFromValue(i:number, count:number, opacity:any):string {
        let clr:any = 360 * i / count;
        return 'hsl(' + clr + ', 100%, 50%)';
    }
    // for chartJS purposes
    private static borderColor:string = "#536980";

    // for chartJS purposes
    protected _borderColorData:any[] = [];
    protected _backgroundColors:any[] = [];
    protected _backgroundHoverColors:any[] = [];


    // for chartJS purposes
    protected updateColors():void {
        this._backgroundColors = this.chartData.map((data, i) => ChartJsChart.hslFromValue(i, this.chartData.length, 0.6));
        this._borderColorData = new Array(this.chartData.length).fill(ChartJsChart.borderColor);
        this._backgroundHoverColors = this.chartData.map((data, i) => ChartJsChart.hslFromValue(i, this.chartData.length, 0.85));
    }

    protected _chartObject:any = undefined;

    constructor() {
        super();
        Chart.defaults.global.maintainAspectRatio = false;
        Chart.defaults.global.animation.duration = 100;
    }

    public newValues(_data:AttributeChartData[]):void {
        if (document.hidden || isNullOrUndefined(_data)) return;
        if (!isNullOrUndefined(this._chartObject)) {
            for (let i = 0; i < _data.length; i++) {
                let _index:number = -1;
                for (let j = 0; j < this.chartData.length; j++) {
                    if ((<AttributeChartData>this.chartData[j]).resourceName == _data[i].resourceName) {
                        _index = j; // remember the index
                        this.chartData[j] = _data[i]; // change the data
                        break;
                    }
                }
                if (_index == -1) {
                    if (this._chartObject != undefined) {
                        this._chartObject.destroy();
                    }
                    this.chartData = _data;
                    this.draw();
                    return;
                } else if (this._chartObject != undefined) {
                    this._chartObject.data.datasets[0].data[_index] = _data[i].attributeValue;
                }
            }
            this._chartObject.update();
        } else {
            this.draw()
        }
    }

    public reinitialize() {
        if (!isNullOrUndefined(this._chartObject)) {
            this._chartObject.destroy();
            this._chartObject = undefined;
        }
    }
}