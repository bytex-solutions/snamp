import { Component, Input, OnInit } from '@angular/core';
import { ColoredAttributePredicate } from '../model/colored.predicate';

import { ConstantAttributePredicate } from '../model/constant.attribute.predicate';
import { NumberComparatorPredicate } from '../model/number.comparator.predicate';
import { IsInRangePredicate } from '../model/range.comparator';

@Component({
  moduleId: module.id,
  selector: 'coloredCondition',
  templateUrl: './templates/condition.html'
})
export class ColoredCondition {
    @Input() entity: ColoredAttributePredicate = undefined;
    @Input() entityType: string = "";

    conditionsType:EntityWithDescription[] = EntityWithDescription.generateConditionsTypes();
    constantExpressions:EntityWithDescription[] = EntityWithDescription.generateTrueFalseTypes();
    operators:EntityWithDescription[] = EntityWithDescription.generateOperatorsTypes();
    rangeOperators:EntityWithDescription[] = EntityWithDescription.generateRangeTypes();

    public isConstantType(language:string):boolean {
        return (language == ColoredAttributePredicate.CONSTANT);
    }

    public isOperatorType(language:string):boolean {
        return (language == ColoredAttributePredicate.COMPARATOR);
    }

    public isRangeType(language:string):boolean {
        return (language == ColoredAttributePredicate.RANGE);
    }

    public onTypeChange(event:string):void {
         switch (event) {
             case ColoredAttributePredicate.CONSTANT:
                this.entity = new ConstantAttributePredicate();
                break;
             case ColoredAttributePredicate.COMPARATOR:
                 this.entity = new NumberComparatorPredicate();
                 break;
             case ColoredAttributePredicate.RANGE:
                 this.entity = new IsInRangePredicate();
                 break;
             default:
                throw new Error("Could not recognize yellow checker type: " + event);
        }
        this.entityType = event;
    }

}

export class EntityWithDescription {
    id:any;
    description:string;

    constructor(id:any, description:string) {
        this.id = id;
        this.description = description;
    }

    public static generateConditionsTypes():EntityWithDescription[] {
        let _value:EntityWithDescription[] = [];
        _value.push(new EntityWithDescription(ColoredAttributePredicate.CONSTANT, "Boolean constant"));
        _value.push(new EntityWithDescription(ColoredAttributePredicate.COMPARATOR, "Compare with certain value"));
        _value.push(new EntityWithDescription(ColoredAttributePredicate.RANGE, "Range comparator"));
        return _value;
    }

    public static generateTrueFalseTypes():EntityWithDescription[] {
        let _value:EntityWithDescription[] = [];
        _value.push(new EntityWithDescription(true, "True"));
        _value.push(new EntityWithDescription(false, "False"));
        return _value;
    }

    public static generateOperatorsTypes():EntityWithDescription[] {
        let _value:EntityWithDescription[] = [];
        _value.push(new EntityWithDescription("GREATER_THAN", ">"));
        _value.push(new EntityWithDescription("GREATER_THAN_OR_EQUAL", "≥"));
        _value.push(new EntityWithDescription("LESS_THAN", "<"));
        _value.push(new EntityWithDescription("LESS_THAN_OR_EQUAL", "≤"));
        _value.push(new EntityWithDescription("EQUAL", "="));
        _value.push(new EntityWithDescription("NOT_EQUAL", "≠"));
        return _value;
    }

    public static generateRangeTypes():EntityWithDescription[] {
        let _value:EntityWithDescription[] = [];
        _value.push(new EntityWithDescription(false, ">"));
        _value.push(new EntityWithDescription(true, "≥"));
        return _value;
    }
}