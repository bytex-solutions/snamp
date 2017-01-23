import { Axis } from './abstract.axis';
import { AttributeInformation } from './attribute';

export class AttributeValueAxis extends Axis {
    public type:string = "attributeValue";
    public sourceAttribute:AttributeInformation;

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        _value["sourceAttribute"] = this.sourceAttribute.toJSON();
        return _value;
    }
}