import { Axis } from './abstract.axis';

export class ResourceNameAxis extends Axis {
    public type:string = Axis.RESOURCE;
    constructor() {
        super();
        this.name = "resources";
    }

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        return _value;
    }
}