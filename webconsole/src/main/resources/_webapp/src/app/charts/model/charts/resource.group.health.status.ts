import { TwoDimensionalChart } from "../two.dimensional.chart";
import { Axis } from "../axis/abstract.axis";
import { ResourceNameAxis } from "../axis/resource.name.axis";
import { HealthStatusAxis } from "../axis/health.status.axis";
import { AbstractChart } from "../abstract.chart";
import { ChartData } from "../data/abstract.data";

export class ResourceGroupHealthStatusChart extends TwoDimensionalChart {
    get type():string {
        return AbstractChart.HEALTH_STATUS;
    }

    public group:string;

    createDefaultAxisX(): Axis {
        return new ResourceNameAxis();
    }

    createDefaultAxisY(): Axis {
        return new HealthStatusAxis();
    }


    toJSON(): any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        _value["group"] = this.group;
        _value["X"] = this.getAxisX().toJSON();
        _value["Y"] = this.getAxisY().toJSON();
        if (!$.isEmptyObject(this.preferences)) {
            _value["preferences"] = this.preferences;
        }
        return _value;
    }

    protected doDraw(): void {
    }

    newValue(_data: ChartData): void {
    }
}