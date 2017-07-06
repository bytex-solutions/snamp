import { ChangeDetectorRef, Component, OnInit, ViewContainerRef } from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { ResourceGroup } from './model/model.resourceGroup';
import { Response } from '@angular/http';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

import { Overlay } from 'angular2-modal';
import { VEXBuiltInThemes, Modal } from 'angular2-modal/plugins/vex';
import { ActivatedRoute } from "@angular/router";
import { isNullOrUndefined } from "util";

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

   constructor(private http: ApiClient, overlay: Overlay, vcRef: ViewContainerRef, public modal: Modal,
               private route: ActivatedRoute,
               private cd: ChangeDetectorRef) {
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
                    this.setActiveResourceGroup(this.resources[0], true);
                    let _thisReference = this;
                    $(document).ready(function() {
                        $(RGroupsComponent.select2ElementId).select2();
                        $(RGroupsComponent.select2ElementId).on('change', (e) => {
                            _thisReference.selectCurrentlyActiveResource($(e.target).val());
                        });
                    });
                }

                this.route
                    .queryParams
                    .subscribe(params => {
                        // Defaults to 0 if no query param provided.
                        let resourceName:string = params['rg'] || "";
                        if (!isNullOrUndefined(this.activeResource) && resourceName.length > 0
                            && resourceName != this.activeResource.name && this.resources.length > 0) {
                            for (let i = 0; i < this.resources.length; i++) {
                                if (this.resources[i].name == resourceName) {
                                    this.setActiveResourceGroup(this.resources[i], true);
                                    break;
                                }
                            }
                        }
                    });
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

        if (this.resources.length > 0) {
            this.setActiveResourceGroup(newResource, true);

            this.cd.detectChanges(); // draw my select pls!

            $(RGroupsComponent.select2ElementId).select2({
                placeholder: "Select resource group",
                width: '100%'
            });
            $(RGroupsComponent.select2ElementId).on('change', (e) => {
                _thisReference.selectCurrentlyActiveResource($(e.target).val());
            });

            $(RGroupsComponent.select2ElementId).val(this.activeResource.name).trigger('change.select2');
        }
    }

    private setActiveResourceGroup(resource:ResourceGroup, setURL?:boolean):void {
        this.activeResource = resource;
        this.oldTypeValue = resource.type;
        if (history.pushState && setURL) {
            let newurl = window.location.protocol + "//" + window.location.host + window.location.pathname + window.location.hash.split("?")[0] + "?rg=" + resource.name;
            window.history.pushState({path:newurl},'',newurl);
        }
        $(RGroupsComponent.select2ElementId).val(this.activeResource.name).trigger('change.select2');
    }


    removeResourceGroup():void {
        this.modal.confirm()
            .isBlocking(true)
            .className(<VEXBuiltInThemes>'default')
            .keyboard(27)
            .message("Resource " + this.activeResource.name + " is being deleted. Are You sure?")
            .open()
            .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                    .then((response) => {
                        this.http.delete(REST.RGROUP_BY_NAME(this.activeResource.name))
                            .subscribe(() => {
                                for (let i = 0; i < this.resources.length; i++) {
                                    if (this.resources[i].name == this.activeResource.name) {
                                        this.resources.splice(i, 1);
                                        if (this.resources.length > 0) {
                                            this.setActiveResourceGroup(this.resources[0], true);

                                            let _thisReference = this;
                                            if ($(RGroupsComponent.select2ElementId).data('select2')) {
                                                $(RGroupsComponent.select2ElementId).select2('destroy');
                                            }
                                            // refresh select2
                                            this.cd.detectChanges(); // draw my select pls!

                                            $(RGroupsComponent.select2ElementId).select2({
                                                placeholder: "Select resource group",
                                                width: '100%'
                                            });
                                            $(RGroupsComponent.select2ElementId).on('change', (e) => {
                                                _thisReference.selectCurrentlyActiveResource($(e.target).val());
                                            });
                                            $(RGroupsComponent.select2ElementId).val(this.activeResource.name).trigger('change.select2');
                                        }
                                        break;
                                    }
                                }
                            });
                        return response;
                    })
                    .catch(() => {
                        return false;
                    });
            });
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