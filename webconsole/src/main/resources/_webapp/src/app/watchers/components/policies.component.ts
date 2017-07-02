import { Component, Input, OnInit } from '@angular/core';
import { ScriptletDataObject } from '../model/scriptlet.data.object';
import { AbstractPolicy } from "../model/policy/abstract.policy";
import { AttributeBasedScalingPolicy } from "../model/policy/attribute.based.scaling.policy";
import { HealthStatusBasedScalingPolicy } from "../model/policy/health.status.based.scaling.policy";

@Component({
  moduleId: module.id,
  selector: 'policies',
  templateUrl: './templates/policies.html',
  styles: ['.btn-inline { display: inline-block; margin-left: 15px; } .normalspaces {  white-space: normal; }']
})
export class PoliciesComponent {
    @Input() entity:{ [key:string]:ScriptletDataObject; } = {};
    @Input() hideDetails:boolean = false;

    isAttrType(policy:AbstractPolicy):boolean {
        return policy instanceof AttributeBasedScalingPolicy;
    }
    isHealthStatusType(policy:AbstractPolicy):boolean {
        return policy instanceof HealthStatusBasedScalingPolicy;
    }
}

