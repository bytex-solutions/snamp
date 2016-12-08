import { Component, Input ,ViewChild, ElementRef } from '@angular/core';
import { Binding } from '../model/model.binding';

@Component({
  selector: 'bindings',
  templateUrl: 'app/configuration/components/templates/binding-table.component.html'
})
export class BindingTable {
    @Input() bindings: Binding[];
}