import { Axis } from "./axis/abstract.axis";
import { AbstractChart } from "./abstract.chart";

export abstract class TwoDimensionalChart extends AbstractChart {
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
}