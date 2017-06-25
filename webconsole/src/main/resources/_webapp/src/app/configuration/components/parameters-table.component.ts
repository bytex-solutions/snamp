import { Component, Input ,ViewChild, ElementRef, OnInit, ViewEncapsulation, ViewChildren, QueryList } from '@angular/core';
import { ApiClient, REST } from '../../services/app.restClient';
import { KeyValue } from '../model/model.entity';
import { TypedEntity } from '../model/model.typedEntity';
import { ParamDescriptor } from '../model/model.paramDescriptor';
import { Response } from '@angular/http';
import { InlineEditComponent } from '../../controls/editor/inline-edit.component';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

import { VEXBuiltInThemes, Modal } from 'angular2-modal/plugins/vex';
import { Resource } from "../model/model.resource";
import { isNullOrUndefined } from "util";

@Component({
  moduleId: module.id,
  selector: 'parameters',
  templateUrl: './templates/parameters-table.component.html',
  encapsulation: ViewEncapsulation.None
})
export class ParametersTable implements OnInit {
    @Input() entity: TypedEntity;
    @ViewChild('newParam') newParamElement:ElementRef;
    @ViewChild('listParamValue') listParamValue:ElementRef;
    @ViewChild('customParamValue') customParamValue:ElementRef;

    @ViewChildren(InlineEditComponent) editComponents: QueryList<InlineEditComponent>;

    selectedParam:ParamDescriptor = undefined;
    stubValue:string = ParamDescriptor.stubValue;

    containsRequired:boolean = false;
    containsOptional:boolean = false;

    constructor(public http:ApiClient, public modal: Modal) {}

    ngOnInit():void {
        this.entity.paramDescriptors.subscribe((descriptors:ParamDescriptor[]) => {
            for (let i in descriptors) {
               if (descriptors[i].required) {
                   this.containsRequired = true;
               } else {
                   this.containsOptional = true;
               }
            }
            if (this.selectedParam == undefined && descriptors.length > 0) {
                 this.selectedParam = descriptors[0];
            }
        });
    }

    getUrlForParameter(key:string):string {
        return REST.ENTITY_PARAMETERS(this.entity.getName(), this.entity.name, key);
    }

    saveParameter(parameter:KeyValue):void {
        this.http.put(this.getUrlForParameter(parameter.key), parameter.value)
            .map((res: Response) => res.text())
            .subscribe(data => this.entity.setParameter(parameter));
    }

    removeParameter(parameter:KeyValue):void {
        this.http.delete(this.getUrlForParameter(parameter.key))
                .map((res: Response) => res.text())
                .subscribe(data => this.entity.removeParameter(parameter.key));
    }

    checkAndRemoveParameter(parameter:KeyValue):void {
        this.entity.isParamRequired(parameter.key).subscribe((res:boolean) => {
             if (res) {
                this.modal.confirm()
                    .className(<VEXBuiltInThemes>'default')
                    .isBlocking(true)
                    .keyboard(27)
                    .message("You are trying to remove required parameter. Proper work of entity is not garanteed. Proceed?")
                    .open()
                    .then((resultPromise) => {
                        return (<Promise<boolean>>resultPromise.result)
                          .then((response) => {
                            this.removeParameter(parameter);
                            return response;
                          })
                          .catch(() =>  false);
                      });
            } else {
                this.removeParameter(parameter);
            }
        });
    }

    addNewParameter():void {
        let paramKey:string = "";
        let paramValue:string = this.stubValue;
        if (this.selectedParam == undefined) {
            paramKey = this.newParamElement.nativeElement.value;
            paramValue = this.customParamValue.nativeElement.value;
        } else {
            paramKey = this.selectedParam.name;
            paramValue = this.listParamValue.nativeElement.value;
        }
        if (this.entity.getParameter(paramKey) != undefined) {
            this.modal.confirm()
                .className(<VEXBuiltInThemes>'default')
                .isBlocking(true)
                .keyboard(27)
                .message("Appending existing parameter. Edit dialog for parameter will display instead. Proceed?")
                .open()
                .then((resultPromise) => {
                    return (<Promise<boolean>>resultPromise.result)
                      .then((response) => {
                            this.editComponents
                                .filter((entry:InlineEditComponent)=> entry.uniqueKey == paramKey)
                                .forEach(function(found:InlineEditComponent){found.edit(found.value)});
                            return response;
                      })
                      .catch(() =>  false);
                  });
        } else {
            this.saveParameter(new KeyValue(paramKey, paramValue));
        }
    }

    flushSelected():void {
        this.selectedParam = undefined;
    }

    clear():void {
        if (!isNullOrUndefined(this.listParamValue)) {
            this.listParamValue.nativeElement.value = "";
        }
        if (!isNullOrUndefined(this.customParamValue)) {
            this.customParamValue.nativeElement.value = this.stubValue;
        }
        if (!isNullOrUndefined(this.newParamElement)) {
            this.newParamElement.nativeElement.value = "";
        }
    }

    isOverriddable():boolean {
        return (this.entity instanceof Resource)
            && (!isNullOrUndefined((<Resource>this.entity).groupName))
            && ((<Resource>this.entity).groupName.length > 0);
    }

    triggerOverride(event:any, param:string):void {
        console.log("They trigger for the entity " + this.entity.name + " and param with name " + param + " is now " + event);
        (<Resource>this.entity).toggleOverridden(param);
        this.http.put(REST.OVERRIDES_BY_NAME(this.entity.name), (<Resource>this.entity).overriddenProperties)
            .map((res:Response) => res.text())
            .subscribe((data)=> {
                console.log("Saved overrides")
            });
    }

    isOverridden(paramName:string):boolean {
        return ((<Resource>this.entity).overriddenProperties.indexOf(paramName) >= 0);
    }
}
