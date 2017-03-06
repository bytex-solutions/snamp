import { ColoredAttributePredicate } from './colored.predicate';

export class NumberComparatorPredicate extends ColoredAttributePredicate {
    public type:string = ColoredAttributePredicate.COMPARATOR;
    public operator:string;
    public value:number;

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["operator"] = this.operator;
        _value["value"] = this.value;
        return _value;
    }
}