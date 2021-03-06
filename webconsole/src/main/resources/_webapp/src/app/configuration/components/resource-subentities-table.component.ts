import { Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';

import { ApiClient, REST } from '../../services/app.restClient';
import { KeyValue } from '../model/model.entity';
import { SubEntity } from '../model/model.subEntity';
import { Attribute } from '../model/model.attribute';
import { Event } from '../model/model.event';
import { Operation } from '../model/model.operation';
import { Response } from '@angular/http';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

import 'smartwizard';
import 'select2';
const Prism = require('prismjs');

import { VEXBuiltInThemes, Modal } from 'angular2-modal/plugins/vex';
import { Resource } from "../model/model.resource";
import { EntityWithSub } from "../model/model.entityWithSub";

@Component({
  moduleId: module.id,
  selector: 'resourceEntity',
  templateUrl: './templates/resource-subentities-table.component.html',
  styleUrls: [ './templates/css/prism.css', './templates/css/main.css' ]
})
export class ResourceEntitiesTable implements OnInit {
    @Input() resource:EntityWithSub;
    @Input() entityType:string;
    @Input() entities: SubEntity[];

    activeEntity:SubEntity;
    readyForSave:boolean = false;
    currentNewParam:KeyValue = new KeyValue("", "");

    discoveredEntities:SubEntity[] = undefined;
    selectedEntity:SubEntity = undefined;
    selectedEntityName:string = "";

    isNewEntity:boolean = false;

    constructor(private http:ApiClient, private modal: Modal, private cd: ChangeDetectorRef,) {}

    private makeEmptyEntity():SubEntity {
        if (this.entityType == "attribute") {
            return new Attribute(this.http, this.resource.type, "", 0);
        } else if (this.entityType == "event") {
            return new Event(this.http, this.resource.type, "");
        } else if (this.entityType == "operation") {
            return new Operation(this.http, this.resource.type, "", 0,);
        }
    }

    ngOnInit():void {
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

    ngAfterViewInit():void {
       let _this = this;
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

    setEntity(entity:SubEntity):void {
        if (this.resource instanceof Resource && !entity.override && (<Resource>this.resource).groupName != undefined && (<Resource>this.resource).groupName.length > 0) {
            let rgname:string = (<Resource>this.resource).groupName;
            if (rgname != undefined && rgname.length > 0) {
                this.modal.confirm()
                    .isBlocking(true)
                    .className(<VEXBuiltInThemes>'default')
                    .keyboard(27)
                    .message("This resource belongs to group " + rgname + ". Group settings have priority. You want to override this entity here?")
                    .open()
                    .then((resultPromise) => {
                        return (<Promise<boolean>>resultPromise.result)
                            .then(() => {
                                entity.override = true;
                                this.prepareEditEntityModal(entity);
                            })
                    }).catch(() => {});
            }
        } else {
            this.prepareEditEntityModal(entity);
        }
    }

    private prepareEditEntityModal(entity:SubEntity) {
        this.activeEntity = Object.create(entity);
        this.isNewEntity = false;
        // see http://disq.us/p/1es8nau (might be 4.1.2 version incoming)
        $(this.getSmartWizardIdentifier()).smartWizard("reset");
        $('#editEntity' + this.entityType).modal("show");
    }

    addNewParameter():void {
         let _thisReference = this;
         $(_thisReference.PARAM_TABLE_DIV()).toggle("fast", function(){
             $(_thisReference.PARAM_APPEND_DIV()).toggle("fast");
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
                    let markup = "<div class='select2-result-repository clearfix'>" +
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

    addNewEntity():void {
        this.activeEntity = this.makeEmptyEntity();
        this.isNewEntity = true;
        $(this.getSmartWizardIdentifier()).smartWizard("reset");
        $('#editEntity' + this.entityType).modal("show");
    }

    cancelAppendingParam():void {
        let _thisReference = this;
         $(_thisReference.PARAM_TABLE_DIV()).toggle("fast", function(){
              $(_thisReference.PARAM_APPEND_DIV()).toggle("fast");
          });
         $(this.PARAM_SELECT_ID()).select2("destroy");
    }

    appendParameter():void {
        this.saveParameter(new KeyValue(this.currentNewParam.key, this.currentNewParam.value));
        this.cancelAppendingParam();
    }

    addNewParameterManually():void {
        this.modal.prompt()
            .className(<VEXBuiltInThemes>'default')
            .message('New manual parameter')
            .placeholder('Please set the name for a new parameter')
            .open()
            .then(dialog => dialog.result)
            .then(result => {
                this.saveParameter(new KeyValue(result, "value"));
                this.cd.markForCheck();
            })
            .catch(() => {});
    }

    htmlViewForEntity():any {
        return Prism.highlight(this.activeEntity.stringifyFullObject(), Prism.languages.javascript);
    }

    remove(entity:SubEntity):void {
         this.modal.confirm()
            .isBlocking(true)
            .className(<VEXBuiltInThemes>'default')
            .keyboard(27)
            .message("Remove " + entity.getName() + " " + entity.name + "?")
            .open()
            .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                  .then(() => {
                    this.http.delete(REST.RESOURCE_ENTITY_BY_NAME(this.resource.getName(), this.resource.name, this.entityType + "s", entity.name))
                        .subscribe(() => {
                            for (let i = 0; i < this.entities.length; i++) {
                                if (this.entities[i].name == entity.name) {
                                    this.entities.splice(i, 1);
                                    break;
                                }
                            }
                        });
                    })
            }).catch(() => {});
    }

    saveParameter(parameter:KeyValue):void {
       this.activeEntity.setParameter(parameter);
    }

    removeParameter(parameter:KeyValue):void {
       this.activeEntity.removeParameter(parameter.key);
    }

    checkAndRemoveParameter(parameter:KeyValue):void {
        this.activeEntity.isParamRequired(parameter.key).subscribe((res:boolean) => {
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

    saveEntity():void {
        this.http.put(REST.RESOURCE_ENTITY_BY_NAME(this.resource.getName(), this.resource.name, this.entityType + "s", this.activeEntity.name), this.activeEntity.stringifyFullObject())
            .map((res:Response) => res.text())
            .subscribe(() => {
                console.debug("Entity " + this.activeEntity.name + " has been saved");
                switch (this.entityType) {
                    case "attribute":
                        if (this.isNewEntity) {
                            this.resource.attributes.push(<Attribute>this.activeEntity);
                        } else {
                            for (let i = 0; i < this.resource.attributes.length; i++) {
                                if (this.resource.attributes[i].guid == this.activeEntity.guid) {
                                    if (this.resource.attributes[i].name != this.activeEntity.name) {
                                        // if we rename the entity - remove the original one
                                        this.http.delete(REST.RESOURCE_ENTITY_BY_NAME(this.resource.getName(), this.resource.name, this.entityType + "s", this.resource.attributes[i].name)).subscribe(() => {});
                                    }
                                    this.resource.attributes[i] = <Attribute>this.activeEntity;
                                    break;
                                }
                            }
                        }
                        break;
                    case "event":
                        if (this.isNewEntity) {
                            this.resource.events.push(<Event>this.activeEntity);
                        } else {
                            for (let i = 0; i < this.resource.events.length; i++) {
                                if (this.resource.events[i].guid == this.activeEntity.guid) {
                                    if (this.resource.events[i].name != this.activeEntity.name) {
                                        // if we rename the entity - remove the original one
                                        this.http.delete(REST.RESOURCE_ENTITY_BY_NAME(this.resource.getName(), this.resource.name, this.entityType + "s", this.resource.events[i].name)).subscribe(() => {});
                                    }
                                    this.resource.events[i] = <Event>this.activeEntity;
                                    break;
                                }
                            }
                        }
                        break;
                    case "operation":
                        if (this.isNewEntity) {
                            this.resource.operations.push(<Operation>this.activeEntity);
                        } else {
                            for (let i = 0; i < this.resource.operations.length; i++) {
                                if (this.resource.operations[i].guid == this.activeEntity.guid) {
                                    if (this.resource.operations[i].name != this.activeEntity.name) {
                                        // if we rename the entity - remove the original one
                                        this.http.delete(REST.RESOURCE_ENTITY_BY_NAME(this.resource.getName(), this.resource.name, this.entityType + "s", this.resource.operations[i].name)).subscribe(() => {});
                                    }
                                    this.resource.operations[i] = <Operation>this.activeEntity;
                                    break;
                                }
                            }
                        }
                        break;
                    default:
                        throw new Error("Could not recognize the entity type: " + this.entityType);
                }
                this.cd.detectChanges();
            });
        $('#editEntity' + this.entityType).modal("hide");
    }

    isResourceType():boolean {
        return (this.resource instanceof Resource);
    }

    addEntityFromList():void {
        (<Resource>this.resource).discovery(this.entityType + "s")
            .map((res:Response) => res.json())
            .subscribe((data:any) => {
                this.discoveredEntities = [];
                for (let key in data) {
                    let _entity:SubEntity;
                    switch (this.entityType) {
                        case "attribute":
                            _entity = new Attribute(this.http, this.resource.type, key, data[key]["readWriteTimeout"], data[key]["override"], data[key]["parameters"]);
                            break;
                        case "event":
                            _entity = new Event(this.http, this.resource.type, key, data[key]["override"], data[key]["parameters"]);
                            break;
                        case "operation":
                            _entity = new Operation(this.http, this.resource.type, key, data[key]["invocationTimeout"], data[key]["override"], data[key]["parameters"]);
                            break;
                        default:
                            throw new Error("Could not recognize the entity type: " + this.entityType);
                    }
                    this.discoveredEntities.push(_entity);
                }
                this.selectedEntity = undefined;
                this.selectedEntityName = "";
                this.cd.detectChanges();
                $('#addExistentEntity' + this.entityType).modal("show");
            })
    }

    formatParams(entity:SubEntity):string {
        let _result:string = "";
        if (entity.parameters.length > 0) {
            _result += "<dl class='row'>";
            for (let i = 0; i < entity.parameters.length; i++) {
                _result += '<dt class="col-sm-3">' + entity.parameters[i].key + '</dt>';
                _result += '<dd class="col-sm-9">' + entity.parameters[i].value + '</dd>';
            }
            _result += "</dl>";
        } else {
            _result += "No params set";
        }
        return _result;
    }

    setActiveEntity(entity:SubEntity):void {
        this.selectedEntity = entity;
        this.selectedEntityName = entity.name + "_new_" + this.entityType;
        this.cd.detectChanges();
    }

    isAttributeSet(attributeName:string):boolean {
        let _result:boolean = false;
        let _attributes:Attribute[] = (<Resource>this.resource).attributes;
        for (let i = 0; i < _attributes.length; i++) {
            if (_attributes[i].getParameter("name") != undefined &&
                _attributes[i].getParameter("name").value == attributeName) {
                _result = true;
                break;
            }
        }
        return _result;
    }

    addSelectedEntityToResource():void {
        this.selectedEntity.parameters.push(new KeyValue("name", this.selectedEntity.name));
        this.selectedEntity.name = this.selectedEntityName;
        this.http.put(REST.RESOURCE_ENTITY_BY_NAME(this.resource.getName(), this.resource.name, this.entityType + "s", this.selectedEntityName), this.selectedEntity.stringifyFullObject())
            .map((res:Response) => res.text())
            .subscribe(() => {
                switch (this.entityType) {
                    case "attribute":
                        this.resource.attributes.push(<Attribute>this.selectedEntity);
                        break;
                    case "event":
                        this.resource.events.push(<Event>this.selectedEntity);
                        break;
                    case "operation":
                        this.resource.operations.push(<Operation>this.selectedEntity);
                        break;
                    default:
                        throw new Error("Could not recognize the entity type: " + this.entityType);
                }
                this.cd.detectChanges();
                $('#addExistentEntity' + this.entityType).modal("hide");
                this.selectedEntity.override = true;
                this.isNewEntity = false;
                this.prepareEditEntityModal(this.selectedEntity);
            });

    }

    cancelEntitySelection():void {
        this.selectedEntity = undefined;
        this.selectedEntityName = "";
        $('#addExistentEntity' + this.entityType).modal("hide");
    }
}