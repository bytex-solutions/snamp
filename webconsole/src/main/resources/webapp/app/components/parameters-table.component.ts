import { Component, Input ,ViewChild, ElementRef, OnInit, ViewContainerRef, ViewEncapsulation, ViewChildren, QueryList } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { KeyValue } from '../model/model.entity';
import { TypedEntity } from '../model/model.typedEntity';
import { ParamDescriptor } from '../model/model.paramDescriptor';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { InlineEditComponent } from './inline-edit.component';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

import { Overlay } from 'angular2-modal';
import {
  VEXBuiltInThemes,
  Modal,
  DialogPreset,
  DialogFormModal,
  DialogPresetBuilder,
  VEXModalContext,
  VexModalModule
} from 'angular2-modal/plugins/vex';

@Component({
  selector: 'parameters',
  templateUrl: 'app/components/templates/parameters-table.component.html',
  styleUrls: ['app/components/templates/css/vex.css', 'app/components/templates/css/vex-theme-wireframe.css'],
  encapsulation: ViewEncapsulation.None
})
export class ParametersTable implements OnInit {
    @Input() entity: TypedEntity;
    @ViewChild('newParam') newParamElement:ElementRef;
    @ViewChild('listParamValue') listParamValue:ElementRef;
    @ViewChild('customParamValue') customParamValue:ElementRef;

    @ViewChildren(InlineEditComponent) editComponents: QueryList<InlineEditComponent>;

    selectedParam:ParamDescriptor = undefined;
    stabValue:string = ParamDescriptor.stubValue;

    containsRequired:boolean = false;
    containsOptional:boolean = false;

    constructor(public http:ApiClient, public modal: Modal) {}

    ngOnInit() {
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
        return REST.GATEWAY_PARAMETERS(this.entity.name, key);
    }

    saveParameter(parameter:KeyValue) {
        this.http.put(this.getUrlForParameter(parameter.key), parameter.value)
            .map((res: Response) => res.text())
            .subscribe(data => this.entity.setParameter(parameter));
    }

    removeParameter(parameter:KeyValue) {
        this.http.delete(this.getUrlForParameter(parameter.key))
                .map((res: Response) => res.text())
                .subscribe(data => this.entity.removeParameter(parameter.key));
    }

    checkAndRemoveParameter(parameter:KeyValue) {
        this.entity.isParamRequired(parameter.key).subscribe((res:boolean) => {
             if (res) {
                this.modal.confirm()
                    .className(<VEXBuiltInThemes>'wireframe')
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

    addNewParameter() {
        let paramKey:string = "";
        let paramValue:string = this.stabValue;
        if (this.selectedParam == undefined) {
            paramKey = this.newParamElement.nativeElement.value;
            paramValue = this.customParamValue.nativeElement.value;
        } else {
            paramKey = this.selectedParam.name;
            paramValue = this.listParamValue.nativeElement.value;
        }
        if (this.entity.getParameter(paramKey) != undefined) {
            this.modal.confirm()
                .className(<VEXBuiltInThemes>'wireframe')
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

    flushSelected() {
        this.selectedParam = undefined;
    }
}