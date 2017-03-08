import { Component, Input, OnInit } from '@angular/core';
import { ScriptletDataObject } from '../model/scriptlet.data.object';

@Component({
  moduleId: module.id,
  selector: 'checkers',
  templateUrl: './templates/checkers.html',
  styles: ['.btn-inline { display: inline-block; margin-left: 15px; } .normalspaces {  white-space: normal; }']
})
export class CheckersComponent {
    @Input() entity:{ [key:string]:ScriptletDataObject; } = {};
}

