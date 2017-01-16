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
import 'select2';
const Prism = require('prismjs');
require('prismjs/plugins/line-numbers/prism-line-numbers.js');

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
    currentNewParam:KeyValue = new KeyValue("", "");
    customKey:string = "";

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

    private PARAM_SELECT_ID():string {
        return "#newParamSelect" + this.entityType;
    }

    private PARAM_APPEND_DIV():string {
        return "#newParamRow" + this.entityType;
    }

    private PARAM_TABLE_DIV():string {
        return "#tableParamsRow" + this.entityType;
    }

    setEntity(entity:SubEntity) {
        this.activeEntity = entity;
        // see http://disq.us/p/1es8nau (might be 4.1.2 version incoming)
        $(this.getSmartWizardIdentifier()).smartWizard("reset");
    }

    addNewParameter() {
         let _thisReference = this;
         $(_thisReference.PARAM_TABLE_DIV()).slideToggle("fast", function(){
             $(_thisReference.PARAM_APPEND_DIV()).slideToggle("fast");
         });

         this.currentNewParam = new KeyValue("", "");
         $(this.PARAM_SELECT_ID()).select2({
             escapeMarkup: function (markup) { return markup; },
             width: 'resolve',
             templateSelection:   function(param) {
               return param.text;
             },
             templateResult: function(param){
                    if (param.loading) return param.text;
                    if (param.element.nodeName == "OPTGROUP") return param.text;
                    if (param.id == "custom") return param.text;
                    var markup = "<div class='select2-result-repository clearfix'>" +
                      "<div class='select2-result-repository__meta'>" +
                        "<div class='select2-result-repository__title'>" + param.element.value + "</div>";

                    markup += "<div class='select2-result-repository__statistics'>";
                    if (param.element.hasAttribute("required") && param.element.getAttribute("required") == "true") {
                      markup += "<div class='select2-result-repository__forks' style='color: red !important;'>Required</div>";
                    }
                    if (_thisReference.isParamPresent(param.element.value)) {
                      markup += "<div class='select2-result-repository__forks' style='color: green !important;'>Already filled</div>";
                    }
                    if (param.element.hasAttribute("defaultValue") && param.element.getAttribute("defaultValue").length > 0) {
                      markup += "<div class='select2-result-repository__stargazers'>Default value: " + param.element.getAttribute("defaultValue") + "</div>";
                    }
                    if (param.element.hasAttribute("pattern") && param.element.getAttribute("pattern").length > 0) {
                      markup += "<div class='select2-result-repository__watchers'>Pattern: " + param.element.getAttribute("pattern") + "</div>";
                    }
                    markup += "</div></div></div>";
                    return markup;
             }
        });

        $(this.PARAM_SELECT_ID()).on('change', (e) => {
          _thisReference.currentNewParam.key =  $(e.target).val();
        });
    }

    private isParamPresent(paramName:string):boolean {
        let result:boolean = false;
        for (let i = 0; i < this.activeEntity.parameters.length; i++) {
            if (this.activeEntity.parameters[i].key == paramName) {
                result = true;
                break;
            }
        }
        return result;
    }

    addNewEntity() {
        this.activeEntity = this.makeEmptyEntity();
        $(this.getSmartWizardIdentifier()).smartWizard("reset");
    }

    cancelAppendingParam() {
        let _thisReference = this;
         $(_thisReference.PARAM_TABLE_DIV()).slideToggle("fast", function(){
              $(_thisReference.PARAM_APPEND_DIV()).slideToggle("fast");
          });
         $(this.PARAM_SELECT_ID()).select2("destroy");
    }

    appendParameter() {
        let key:string = "";
        let value:string = this.currentNewParam.value;
        if (this.currentNewParam.key == "custom") {
          key = this.customKey;
        } else {
          key = this.currentNewParam.key;
        }
        let finalValue:KeyValue = new KeyValue(key, value);
        this.saveParameter(finalValue);
        this.cancelAppendingParam();
    }

    htmlViewForEntity():any {
        return Prism.highlight(this.activeEntity.stringifyFullObject(), Prism.languages.javascript);
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
