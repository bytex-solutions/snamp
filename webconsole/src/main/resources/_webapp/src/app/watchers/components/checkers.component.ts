import { Component, Input, OnInit } from '@angular/core';
import { ScriptletDataObject } from '../model/scriptlet.data.object';

import { ColoredAttributePredicate } from '../model/colored.predicate';
import { ConstantAttributePredicate } from '../model/constant.attribute.predicate';
import { NumberComparatorPredicate } from '../model/number.comparator.predicate';
import { IsInRangePredicate } from '../model/range.comparator';

@Component({
  moduleId: module.id,
  selector: 'checkers',
  templateUrl: './templates/checkers.html',
  styles: ['.btn-inline { display: inline-block; margin-left: 15px; } .normalspaces {  white-space: normal; }']
})
export class CheckersComponent {
    @Input() entity:{ [key:string]:ScriptletDataObject; } = {};
    @Input() hideDetails:boolean = false;

    public isConstantType(predicate:ColoredAttributePredicate):boolean {
        return (predicate instanceof ConstantAttributePredicate);
    }

    public isOperatorType(predicate:ColoredAttributePredicate):boolean {
        return (predicate instanceof NumberComparatorPredicate);
    }

    public isRangeType(predicate:ColoredAttributePredicate):boolean {
        return (predicate instanceof IsInRangePredicate);
    }
}

