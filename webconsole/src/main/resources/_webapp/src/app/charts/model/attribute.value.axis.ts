import { Axis } from './abstract.axis';
import { AttributeInformation } from './attribute';

export class AttributeValueAxis extends Axis {
    public type:string = Axis.ATTRIBUTES;
    public sourceAttribute:AttributeInformation;

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        if (_value["sourceAttribute"] != undefined) {
            _value["sourceAttribute"] = this.sourceAttribute.toJSON();
        }
        return _value;
    }
}