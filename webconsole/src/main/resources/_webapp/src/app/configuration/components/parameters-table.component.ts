import { Component, Input ,ViewChild, ElementRef, OnInit, ViewEncapsulation, ViewChildren, QueryList, ChangeDetectorRef } from '@angular/core';
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

    paramDescriptors:ParamDescriptor[] = [];

    constructor(public http:ApiClient, public modal: Modal, private cd: ChangeDetectorRef) {}

    ngOnInit():void {}

    private isParameterRequired(key:string):boolean {
        for (let i = 0; i < this.paramDescriptors.length; i++) {
            if (this.paramDescriptors[i].name == key && this.paramDescriptors[i].required) {
                return true;
            }
        }
        return false;
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
         if (this.isParameterRequired(parameter.key)) {
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
                  }).catch(() =>  false);
        } else {
            this.removeParameter(parameter);
        }
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
        this.http.getWithErrors(REST.ENTITY_PARAMETERS_DESCRIPTION(this.entity.getDescriptionType(), this.entity.type))
            .map((res:Response) => res.json())
            .subscribe((data:any) => {
                if (!isNullOrUndefined(this.listParamValue)) {
                    this.listParamValue.nativeElement.value = "";
                }
                if (!isNullOrUndefined(this.customParamValue)) {
                    this.customParamValue.nativeElement.value = this.stubValue;
                }
                if (!isNullOrUndefined(this.newParamElement)) {
                    this.newParamElement.nativeElement.value = "";
                }
                this.containsRequired = false;
                this.containsOptional = false;
                this.paramDescriptors = [];
                for (let i = 0; i < data.length; i++) {
                    let _tmp:ParamDescriptor = new ParamDescriptor(data[i]);
                    if (_tmp.name != "smartMode") { // filter smart mode
                        this.paramDescriptors.push(_tmp);
                        if (_tmp.required) {
                            this.containsRequired = true;
                        } else {
                            this.containsOptional = true;
                        }
                    }
                }
                if (this.selectedParam == undefined && this.paramDescriptors.length > 0) {
                    this.selectedParam = this.paramDescriptors[0];
                }
                console.debug("After all we got: ", this.paramDescriptors, this.containsRequired, this.containsOptional);
                this.cd.detectChanges();
                $("#addParam").modal("show");
            });
    }

    isOverriddable():boolean {
        return (this.entity instanceof Resource)
            && (!isNullOrUndefined((<Resource>this.entity).groupName))
            && ((<Resource>this.entity).groupName.length > 0);
    }

    triggerOverride(event:any, param:string):void {
        (<Resource>this.entity).toggleOverridden(param);
        this.http.put(REST.OVERRIDES_BY_NAME(this.entity.name), (<Resource>this.entity).overriddenProperties)
            .map((res:Response) => res.text())
            .subscribe(()=> {
                console.debug("Saved overrides")
            });
    }

    isOverridden(paramName:string):boolean {
        return ((<Resource>this.entity).overriddenProperties.indexOf(paramName) >= 0);
    }
}
