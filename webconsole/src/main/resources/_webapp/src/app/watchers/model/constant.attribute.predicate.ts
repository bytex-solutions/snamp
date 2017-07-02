import { ColoredAttributePredicate } from './colored.predicate';

export class ConstantAttributePredicate extends ColoredAttributePredicate {
    public type:string = ColoredAttributePredicate.CONSTANT;
    public value:boolean;

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["value"] = this.value;
        return _value;
    }

    public represent():string {
        return "value = " + (new Boolean(this.value)).toString();
    }
}