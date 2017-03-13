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

    public represent():string {
        let _value:string = "value ";
        switch (this.operator) {
            case "GREATER_THAN":
                _value += ">";
                break;
            case "GREATER_THAN_OR_EQUAL":
                _value += "≥";
                break;
            case "LESS_THAN":
                _value += "<";
                break;
            case "LESS_THAN_OR_EQUAL":
                _value += "≤";
                break;
            case "EQUAL":
                _value += ">";
                break;
            case "NOT_EQUAL":
                _value += "≠";
                break;
            default:
                throw new Error("Operator " + this.operator + "cannot be recognized");
        }
        _value += " " + this.value;
        return _value;
    }
}