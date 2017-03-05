import { ColoredAttributePredicate } from './colored.predicate';

public export class ConstantAttributePredicate extends ColoredAttributePredicate {
    public type:string = ColoredAttributePredicate.CONSTANT;
    public value:boolean;

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["value"] = this.value;
        return _value;
    }
}