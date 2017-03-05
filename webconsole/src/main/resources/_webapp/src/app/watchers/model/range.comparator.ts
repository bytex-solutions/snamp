import { ColoredAttributePredicate } from './colored.predicate';

public export class IsInRangePredicate extends ColoredAttributePredicate {
    public type:string = ColoredAttributePredicate.RANGE;
    public rangeStart:number;
    public rangeEnd:number;
    public isRangeStartInclusive:boolean;
    public isRangeEndInclusive:boolean;
}