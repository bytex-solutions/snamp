import { Component, OnInit, ViewContainerRef, ViewEncapsulation } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Resource } from './model/model.resource';
import { KeyValue } from './model/model.entity';
import { Binding } from './model/model.binding';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

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
  templateUrl: 'app/configuration/templates/resources.html',
  styleUrls: [
    'app/configuration/templates/css/checkbox.css',
    'app/configuration/components/templates/css/vex.css',
    'app/configuration/components/templates/css/vex-theme-wireframe.css'],
})
export class ResourcesComponent implements OnInit {

   resources:Resource[] = [];
   activeResource:Resource;
   oldTypeValue:string = "";
   oldGroupValue:string = "";
   http:ApiClient;
   availableResources :any[] = [];
   availableGroups:string[] = [];
   oldSmartMode = false;

   constructor(apiClient: ApiClient, overlay: Overlay, vcRef: ViewContainerRef, public modal: Modal) {
        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;
   }

    ngOnInit() {
        // Get all configured resources from the server
        this.http.get(REST.RESOURCE_CONFIG)
            .map((res: Response) => res.json())
            .subscribe(data => {
                for (let key in data) {
                    this.resources.push(new Resource(this.http, key, data[key]))
                }
                this.activeResource = (this.resources.length > 0) ? this.resources[0] : this.activeResource;
                this.oldTypeValue = this.activeResource.type;
                this.oldGroupValue = this.activeResource.groupName;
                this.oldSmartMode = this.activeResource.smartMode;
            });

        // Get all the available bundles that belong to Resources
        this.http.get(REST.AVAILABLE_RESOURCE_LIST)
            .map((res: Response) => res.json())
            .subscribe(data => this.availableResources = data);

        // Get available group names for listing in the select element
        this.http.get(REST.RGROUP_LIST)
            .map((res: Response) => res.json())
            .subscribe(data => {
                for(let i = 0; i < data.length; i++){
                  this.availableGroups.push(data[i]);
                }
            });
    }

    setActiveResource(resource:Resource) {
        this.activeResource = resource;
        this.oldTypeValue = resource.type;
        this.oldGroupValue = resource.groupName;
        this.oldSmartMode = resource.smartMode;
    }

    changeType(event:any) {
      this.modal.confirm()
        .isBlocking(true)
        .className(<VEXBuiltInThemes>'wireframe')
        .keyboard(27)
        .message("If you change resource type - all resources will be removed. Are you sure?")
        .open()
        .then((resultPromise) => {
            return (<Promise<boolean>>resultPromise.result)
              .then((response) => {
                this.oldTypeValue = event.target.value;
                this.http.put(REST.RESOURCE_TYPE(this.activeResource.name), event.target.value);
                return response;
              })
              .catch(() => {
                this.activeResource.type = this.oldTypeValue;
                return false;
              });
      });
    }

    changeGroup(event:any) {
      this.modal.confirm()
        .isBlocking(true)
        .className(<VEXBuiltInThemes>'wireframe')
        .keyboard(27)
        .message("Setting group for resource leads to appending all entities from the group to resource. " +
            "The page is going to be refreshed. Proceed?")
        .open()
        .then((resultPromise) => {
            return (<Promise<boolean>>resultPromise.result)
              .then((response) => {
                this.oldGroupValue = event.target.value;
                this.http.put(REST.RESOURCE_GROUP(this.activeResource.name), event.target.value)
                    .subscribe(data => {
                        location.reload();
                    });
                return response;
              })
              .catch(() => {
                this.activeResource.groupName = this.oldGroupValue;
                return false;
              });
        });
    }

    triggerSmartMode(event:any) {
        if (this.activeResource.smartMode == false) {
          this.modal.confirm()
            .className(<VEXBuiltInThemes>'wireframe')
            .isBlocking(true)
            .keyboard(27)
            .message("If you enable smartMode - all attributes, events and operations will be removed. Proceed?")
            .open()
            .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                  .then((response) => {
                    this.http.put(REST.ENTITY_PARAMETERS("resource", this.activeResource.name, "smartMode"), true)
                        .subscribe(data => {
                           this.oldSmartMode = true;
                           this.activeResource.smartMode = true;
                           location.reload();
                        });
                    return response;
                  })
                  .catch(() => {
                    this.activeResource.smartMode = this.oldSmartMode;
                    return false;
                  });
             });
         } else {
            this.http.put(REST.ENTITY_PARAMETERS("resource", this.activeResource.name, "smartMode"), false)
                .subscribe(data => {
                   this.oldSmartMode = false;
                });
         }
    }

    saveConnectionString() {
            this.http.put(REST.RESOURCE_CONNECTION_STRING(this.activeResource.name),
                this.activeResource.connectionString)
                .subscribe(res => console.log("connection string for " + this.activeResource.name +
                    " has been changed to " + this.activeResource.connectionString + " with result " + res));
    }


}
