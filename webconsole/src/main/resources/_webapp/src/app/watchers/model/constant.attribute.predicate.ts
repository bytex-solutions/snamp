import { ColoredAttributePredicate } from './colored.predicate';

public export class ConstantAttributePredicate extends ColoredAttributePredicate {
    public type:string = ColoredAttributePredicate.CONSTANT;
    public value:boolean;
}