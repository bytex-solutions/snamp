import { Component, Input, OnInit } from '@angular/core';
import { ScriptletDataObject } from '../model/scriptlet.data.object';

@Component({
  moduleId: module.id,
  selector: 'trigger',
  templateUrl: './templates/trigger.html',
  styles: ['.flatbar { width: 100% !important; text-align: left !important; margin-left: -15px !important; }']
})
export class TriggerComponent {
    @Input() entity: ScriptletDataObject = undefined;
}

