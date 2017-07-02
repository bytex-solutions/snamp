import { Component, Input ,ViewChild, ElementRef } from '@angular/core';
import { Binding } from '../model/model.binding';

@Component({
  moduleId: module.id,
  selector: 'bindings',
  templateUrl: './templates/binding-table.component.html'
})
export class BindingTable {
    @Input() bindings: Binding[];
}
