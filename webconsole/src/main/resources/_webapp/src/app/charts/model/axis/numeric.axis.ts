import { Axis } from './abstract.axis';

export class NumericAxis extends Axis {
    public type:string = Axis.NUMERIC;
    public unitOfMeasurement:string = "";

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        return _value;
    }
}