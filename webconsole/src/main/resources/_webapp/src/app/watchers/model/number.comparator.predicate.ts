import { ColoredAttributePredicate } from './colored.predicate';

public export class NumberComparatorPredicate extends ColoredAttributePredicate {
    public type:string = ColoredAttributePredicate.COMPARATOR;
    public operator:string;
    public value:number;
}