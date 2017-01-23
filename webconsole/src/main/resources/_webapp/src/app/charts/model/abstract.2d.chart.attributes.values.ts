import { ChartOfAttributeValues } from './abstract.chart.attributes.values';
import { Axis } from './abstract.axis';

export abstract class TwoDimensionalChartOfAttributeValues extends ChartOfAttributeValues {
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

    public setAxisX(x:axis):void {
        this.axisX = x;
    }

    public setAxisY(y:axis):void {
        this.axisY = y;
    }
}