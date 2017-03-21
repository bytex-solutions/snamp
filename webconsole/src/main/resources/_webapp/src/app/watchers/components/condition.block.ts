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

    public isConstantType():boolean {
        return (this.entity instanceof ConstantAttributePredicate);
    }

    public isOperatorType():boolean {
        return (this.entity instanceof NumberComparatorPredicate);
    }

    public isRangeType():boolean {
        return (this.entity instanceof IsInRangePredicate);
    }

    public onTypeChange(event:string):void {
         switch (event) {
             case "ConstantAttributePredicate":
                this.entity = new ConstantAttributePredicate();
                break;
             case "NumberComparatorPredicate":
                 this.entity = new NumberComparatorPredicate();
                 break;
             case "IsInRangePredicate":
                 this.entity = new IsInRangePredicate();
                 break;
             default:
                throw new Error("Could not recognize yellow checker type: " + event);
        }
        this.entityType = event;
    }

    ngOnInit():void {
        this.entityType = (this.entity != undefined) ?  this.entity.constructor.name : "";
    }

    ngAfterViewInit():void {
        console.log("Entity: ", this.entity, ", entityType: ", this.entityType);
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
        _value.push(new EntityWithDescription("ConstantAttributePredicate", "Boolean constant"));
        _value.push(new EntityWithDescription("NumberComparatorPredicate", "Compare with certain value"));
        _value.push(new EntityWithDescription("IsInRangePredicate", "Range comparator"));
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