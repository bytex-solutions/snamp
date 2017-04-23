import { ChartOfAttributeValues } from './abstract.chart.attributes.values';
import { Axis } from './abstract.axis';
import { AttributeInformation } from './attribute';
import { AttributeValueAxis } from './attribute.value.axis';
import { ChartUtils } from "./chart.utils";

export abstract class TwoDimensionalChartOfAttributeValues extends ChartOfAttributeValues {

    // for chartJS purposes
    private static hslFromValue(i:number, count:number, opacity:any):string {
        let clr:any = 360 * i / count;
        return 'hsla(' + clr + ', 100%, 50%, ' + opacity + ')';
    }
    // for chartJS purposes
    private static borderColor:string = "#536980";

    // for chartJS purposes
    protected _borderColorData:any[] = [];
    protected _backgroundColors:any[] = [];
    protected _backgroundHoverColors:any[] = [];


    // for chartJS purposes
    protected updateColors():void {
        this._backgroundColors = this.chartData.map((data, i) => TwoDimensionalChartOfAttributeValues.hslFromValue(i, this.chartData.length, 0.3));
        this._borderColorData = new Array(this.chartData.length).fill(TwoDimensionalChartOfAttributeValues.borderColor);
        this._backgroundHoverColors = this.chartData.map((data, i) => TwoDimensionalChartOfAttributeValues.hslFromValue(i, this.chartData.length, 0.75));
    }

    private axisX:Axis;
    private axisY:Axis;
    abstract createDefaultAxisX():Axis;

    abstract createDefaultAxisY():Axis;

    public getAxisX():Axis {
        if (this.axisX == undefined) {
            this.axisX = this.createDefaultAxisX();
        }
        return this.axisX;
    }

    public getAxisY():Axis {
        if (this.axisY == undefined) {
            this.axisY = this.createDefaultAxisY();
        }
        return this.axisY;
    }

    public setAxisX(x:Axis):void {
        this.axisX = x;
    }

    public setAxisY(y:Axis):void {
        this.axisY = y;
    }

    public setSourceAttribute(sourceAttribute:AttributeInformation):void {
        if (this.getAxisX() instanceof AttributeValueAxis) {
            (<AttributeValueAxis>this.getAxisX()).sourceAttribute = sourceAttribute;
        }
        if (this.getAxisY() instanceof AttributeValueAxis) {
            (<AttributeValueAxis>this.getAxisY()).sourceAttribute = sourceAttribute;
        }
    }
}