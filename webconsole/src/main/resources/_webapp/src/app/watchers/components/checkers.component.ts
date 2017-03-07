import { Component, Input, OnInit } from '@angular/core';
import { ScriptletDataObject } from '../model/scriptlet.data.object';

@Component({
  moduleId: module.id,
  selector: 'checkers',
  templateUrl: './templates/checkers.html',
  styles: ['.flatbar { width: 100% !important; text-align: left !important; margin-left: -15px !important; }']
})
export class CheckersComponent {
    @Input() entity:{ [key:string]:ScriptletDataObject; } = {};
}

