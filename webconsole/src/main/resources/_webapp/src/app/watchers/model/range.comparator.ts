import { ColoredAttributePredicate } from './colored.predicate';

export class IsInRangePredicate extends ColoredAttributePredicate {
    public type:string = ColoredAttributePredicate.RANGE;
    public rangeStart:number;
    public rangeEnd:number;
    public isRangeStartInclusive:boolean;
    public isRangeEndInclusive:boolean;

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["rangeStart"] = this.rangeStart;
        _value["rangeEnd"] = this.rangeEnd;
        _value["isRangeStartInclusive"] = this.rangeStart;
        _value["isRangeEndInclusive"] = this.rangeEnd;
        return _value;
    }

    public represent():string {
        let _value:string = "";
        _value += this.rangeEnd + " " + (this.isRangeEndInclusive ? "≥": ">")
            +  " value " + (this.isRangeStartInclusive ? "≥": ">") + " " + this.rangeStart;
        return _value;
    }
}