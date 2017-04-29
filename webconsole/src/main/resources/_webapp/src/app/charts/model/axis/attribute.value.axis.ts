import { Axis } from './abstract.axis';
import { AttributeInformation } from '../attribute';

export class AttributeValueAxis extends Axis {
    public type:string = Axis.ATTRIBUTES;
    public sourceAttribute:AttributeInformation;

    public getLabelRepresentation():string {
        let _value:string = "";
        if (this.sourceAttribute != undefined) {
            _value += this.sourceAttribute.name;
            if (this.sourceAttribute.type != undefined && this.sourceAttribute.type.length > 0) {
                _value += " (" + this.sourceAttribute.type + ")";
            }

            if (this.sourceAttribute.unitOfMeasurement != undefined && this.sourceAttribute.unitOfMeasurement.length > 0) {
                _value += ", " + this.sourceAttribute.unitOfMeasurement;
            }
        }
        return _value;
    }

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        if (this.sourceAttribute != undefined) {
            _value["sourceAttribute"] = this.sourceAttribute.toJSON();
        }
        return _value;
    }
}