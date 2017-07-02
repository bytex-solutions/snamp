import { Component, Input, OnInit } from '@angular/core';
import { ScriptletDataObject } from '../model/scriptlet.data.object';
import { isNullOrUndefined } from "util";

@Component({
  moduleId: module.id,
  selector: 'trigger',
  templateUrl: './templates/trigger.html',
  styles: ['.btn-inline { display: inline-block; margin-left: 15px; } .normalspaces {  white-space: normal; }']
})
export class TriggerComponent {
    @Input() entity: ScriptletDataObject = undefined;
    @Input() hideDetails:boolean = false;

    public isEmpty():boolean {
        return (this.entity.language != "Groovy" && isNullOrUndefined(this.entity.object))
            || (this.entity.language == "Groovy" && (isNullOrUndefined(this.entity.script) || this.entity.script.length < 3));
    }
}

