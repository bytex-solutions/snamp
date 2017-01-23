import { Axis } from './abstract.axis';

export class InstanceNameAxis extends Axis {
    public type:string = Axis.INSTANCE;
    constructor() {
        this.name = "instances";
    }

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        return _value;
    }
}