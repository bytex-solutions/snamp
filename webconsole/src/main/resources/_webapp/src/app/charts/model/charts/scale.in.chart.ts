import { ScalingRateChart } from "../scaling.rate.chart";
import { NumericAxis } from "../axis/numeric.axis";
import { AbstractChart } from "../abstract.chart";

export class ScaleInChart extends ScalingRateChart {
    get type():string {
        return AbstractChart.SCALE_IN;
    }

    public createDefaultAxisY() {
        let _axis:NumericAxis = super.createDefaultAxisY();
        _axis.unitOfMeasurement = "operations";
        _axis.name = "downscale";
        return _axis;
    }
}
