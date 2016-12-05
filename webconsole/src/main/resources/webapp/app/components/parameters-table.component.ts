import { Component, Input ,ViewChild, ElementRef, OnInit, ViewContainerRef, ViewEncapsulation } from '@angular/core';
import { ApiClient } from '../app.restClient';
import { KeyValue } from '../model/model.entity';
import { TypedEntity } from '../model/model.typedEntity';
import { ParamDescriptor } from '../model/model.paramDescriptor';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

import { Overlay } from 'angular2-modal';
import { Modal } from 'angular2-modal/plugins/bootstrap';

@Component({
  selector: 'parameters',
  templateUrl: 'app/components/templates/parameters-table.component.html'
})
export class ParametersTable implements OnInit {
    @Input() entity: TypedEntity;
    @ViewChild('newParam') newParamElement:ElementRef;
    @ViewChild('listParamValue') listParamValue:ElementRef;
    @ViewChild('customParamValue') customParamValue:ElementRef;
    selectedParam:ParamDescriptor = undefined;
    http:ApiClient;
    stabValue:string = ParamDescriptor.stubValue;

    containsRequired:boolean = false;
    containsOptional:boolean = false;

    constructor(http:ApiClient, overlay: Overlay, vcRef: ViewContainerRef, public modal: Modal) {
        this.http = http;
        overlay.defaultViewContainer = vcRef;
    }

    ngOnInit() {
        this.entity.paramDescriptors.subscribe((descriptors:ParamDescriptor[]) => {
            for (let i in descriptors) {
               if (descriptors[i].required) {
                   this.containsRequired = true;
                   if (this.selectedParam == undefined) {
                     this.selectedParam = descriptors[i];
                   }
               } else {
                   this.containsOptional = true;
                   if (this.selectedParam == undefined) {
                     this.selectedParam = descriptors[i];
                   }
               }
            }
        });
    }

    getUrlForParameter(key:string):string {
        return "/snamp/console/" + this.entity.getName() + "/" + this.entity.name + "/parameters/" + key;
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
                .size('sm')
                .isBlocking(true)
                .keyboard(27)
                .showClose(true)
                .title("Removing required parameter")
                .body("You are trying to remove required parameter. Proper work of entity is not garanteed. Proceed?")
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
        this.saveParameter(new KeyValue(paramKey, paramValue));
    }

    flushSelected() {
        this.selectedParam = undefined;
    }
}