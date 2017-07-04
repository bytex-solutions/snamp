import { Component, OnInit, ViewContainerRef, ViewEncapsulation } from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { ResourceGroup } from './model/model.resourceGroup';
import { Response } from '@angular/http';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

import { Overlay } from 'angular2-modal';
import { VEXBuiltInThemes, Modal } from 'angular2-modal/plugins/vex';

@Component({
  moduleId: module.id,
  templateUrl: './templates/rgroups.html',
  styleUrls: [ './templates/css/checkbox.css' ]
})
export class RGroupsComponent implements OnInit {

   resources:ResourceGroup[] = [];
   activeResource:ResourceGroup;
   oldTypeValue:string = "";
   availableResources :any[] = [];

    private static select2ElementId:string = "#resourceSelection";
    private static selectionId:string = "#select2-resourceSelection-container";

   constructor(private http: ApiClient, overlay: Overlay, vcRef: ViewContainerRef, public modal: Modal) {
        overlay.defaultViewContainer = vcRef;
   }

    ngOnInit() {
        // Get all configured resources from the server
        this.http.get(REST.RGROUP_CONFIG)
            .map((res: Response) => res.json())
            .subscribe(data => {
                for (let key in data) {
                    this.resources.push(new ResourceGroup(this.http, key, data[key]))
                }
                if (this.resources.length > 0) {
                    this.activeResource = this.resources[0];
                    this.oldTypeValue = this.activeResource.type;
                    let _thisReference = this;
                    $(document).ready(function() {
                        $(RGroupsComponent.select2ElementId).select2();
                        $(RGroupsComponent.select2ElementId).on('change', (e) => {
                            _thisReference.selectCurrentlyActiveResource($(e.target).val());
                        });
                    });
                }
            });

        // Get all the available bundles that belong to Resources
        this.http.get(REST.AVAILABLE_RESOURCE_LIST)
            .map((res: Response) => res.json())
            .subscribe(data => this.availableResources = data);

    }

    dispatchNewResourceGroup(newResource:ResourceGroup):void {
        let _thisReference = this;
        if ($(RGroupsComponent.select2ElementId).data('select2')) {
            $(RGroupsComponent.select2ElementId).select2('destroy');
        }
        $(RGroupsComponent.select2ElementId).select2({
            placeholder: "Select gateway",
            width: '100%',
            allowClear: true
        });
        $(RGroupsComponent.select2ElementId).on('change', (e) => {
            _thisReference.selectCurrentlyActiveResource($(e.target).val());
        });

        if (this.resources.length > 0) {
            this.activeResource = newResource;
            this.oldTypeValue = newResource.type;
            $(RGroupsComponent.selectionId).html(this.activeResource.name);
        }
    }

    selectCurrentlyActiveResource(resourceName:string) {
          for (let i = 0; i < this.resources.length; i++) {
            if (this.resources[i].name == resourceName) {
                this.activeResource = this.resources[i];
                this.oldTypeValue = this.resources[i].type;
                break;
            }
          }
      }

    changeType(event:any) {
      this.modal.confirm()
        .isBlocking(true)
        .className(<VEXBuiltInThemes>'default')
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
      }).catch(() => {});
    }
}