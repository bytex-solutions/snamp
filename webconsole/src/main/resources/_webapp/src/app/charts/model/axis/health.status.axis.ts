import { Axis } from "./abstract.axis";

export class HealthStatusAxis extends Axis {

    public type:string = Axis.HEALTH_STATUS;
    public unitOfMeasurement:string = "threatLevel";

    constructor() {
        super();
        this.name = "Health Statuses";
    }

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        return _value;
    }
}