import { ScalingRateChart } from "../scaling.rate.chart";
import { NumericAxis } from "../axis/numeric.axis";
import { AbstractChart } from "../abstract.chart";

export class ScaleOutChart extends ScalingRateChart {
    get type():string {
        return AbstractChart.SCALE_OUT;
    }

    public createDefaultAxisY() {
        let _axis:NumericAxis = super.createDefaultAxisY();
        _axis.unitOfMeasurement = "operations";
        _axis.name = "upscale";
        return _axis;
    }
}
