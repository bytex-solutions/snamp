import { Axis } from './abstract.axis';

export class ChronoAxis extends Axis {
    public type:string = "chrono";
    public unitOfMeasurement:string = "seconds";

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        return _value;
    }
}