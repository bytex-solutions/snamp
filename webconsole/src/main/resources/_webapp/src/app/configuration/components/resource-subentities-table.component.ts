import { Component, Input ,ViewChild, ElementRef, OnInit } from '@angular/core';

import { ApiClient, REST } from '../../app.restClient';
import { KeyValue, Entity } from '../model/model.entity';
import { TypedEntity } from '../model/model.typedEntity';
import { SubEntity } from '../model/model.subEntity';
import { Attribute } from '../model/model.attribute';
import { Event } from '../model/model.event';
import { ParamDescriptor } from '../model/model.paramDescriptor';
import { Operation } from '../model/model.operation';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

import 'smartwizard';

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
  moduleId: module.id,
  selector: 'resourceEntity',
  templateUrl: './templates/resource-subentities-table.component.html',
  styleUrls: [
      './templates/css/vex.css',
      './templates/css/vex-theme-wireframe.css'
    ]
})
export class ResourceEntitiesTable implements OnInit {
    @Input() resource:TypedEntity;
    @Input() entityType:string;
    readyForSave:boolean = false;
    paramDescriptors:ParamDescriptor[] = [];
    @Input() entities: SubEntity[];
    activeEntity:SubEntity;

    constructor(private http:ApiClient, private modal: Modal) {}

    private makeEmptyEntity():SubEntity {
        if (this.entityType == "attribute") {
            return new Attribute(this.http, this.resource.type, "", 0, {});
        } else if (this.entityType == "event") {
            return new Event(this.http, this.resource.type, "", {});
        } else if (this.entityType == "operation") {
            return new Operation(this.http, this.resource.type, "", 0, {});
        }
    }

    ngOnInit() {
        // it might be overkill but let's set the first entity as an active one
        if (this.entities.length > 0) {
            this.activeEntity = this.entities[0];
        } else {
            // if we have no entities - we definitely are appending new one
            this.activeEntity = this.makeEmptyEntity();
        }
    }

    getSmartWizardIdentifier():string {
      return "#smartwizard" + this.entityType;
    }

    ngAfterViewInit() {
       var _this = this;
       let _hiddenSteps:number[] = [];
       if (this.entityType == "event") {
          _hiddenSteps.push(1);
       }
       $(document).ready(function() {
          $(_this.getSmartWizardIdentifier()).smartWizard({
               theme: 'arrows',
               hiddenSteps: _hiddenSteps,
               useURLhash: false,
               showStepURLhash: false,
               transitionEffect: 'fade'
           });
        });
    }

    setEntity(entity:SubEntity) {
        this.activeEntity = entity;
        // see http://disq.us/p/1es8nau (might be 4.1.2 version incoming)
        $(this.getSmartWizardIdentifier()).smartWizard("reset");
    }

    remove(entity:SubEntity) {
         this.modal.confirm()
            .isBlocking(true)
            .className(<VEXBuiltInThemes>'wireframe')
            .keyboard(27)
            .message("Remove " + entity.getName() + " " + entity.name + "?")
            .open()
            .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                  .then((response) => {
                    this.http.delete(REST.RESOURCE_ENTITY_BY_TYPE_AND_NAME(entity.getName() + "s", this.resource.name, entity.name))
                        .subscribe(data => {
                            for (let i = 0; i < this.entities.length; i++) {
                                if (this.entities[i].name == entity.name) {
                                    this.entities.splice(i, 1);
                                    break;
                                }
                            }
                        });
                    })
            });
    }

    saveParameter(parameter:KeyValue) {
       this.activeEntity.setParameter(parameter);
    }

    removeParameter(parameter:KeyValue) {
       this.activeEntity.removeParameter(parameter.key);
    }

    checkAndRemoveParameter(parameter:KeyValue) {
        this.activeEntity.isParamRequired(parameter.key).subscribe((res:boolean) => {
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

    saveEntity() {
      alert("Save button now clicked!");
    }
}
