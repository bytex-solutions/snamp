import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { Resource } from './model/model.resource';
import { Response } from '@angular/http';
import { Overlay } from 'angular2-modal';
import { VEXBuiltInThemes, Modal } from 'angular2-modal/plugins/vex';
import { ThreadPool } from "./model/model.thread.pool";
import {isNullOrUndefined} from "util";
import {Observable} from "rxjs/Observable";

@Component({
    moduleId: module.id,
    templateUrl: './templates/resources.html',
    styleUrls: [ './templates/css/checkbox.css' ]
})
export class ResourcesComponent implements OnInit {

    private resources:Resource[] = [];
    private activeResource:Resource;
    private oldTypeValue:string = "";
    private oldGroupValue:string = "";
    private availableResources :any[] = [];
    private availableGroups:string[] = [];
    private availableThreadPools:ThreadPool[] = [];
    private oldSmartMode = false;
    private groupSelection:boolean = false;

    private static select2ElementId:string = "#resourceSelection";
    private static selectionId:string = "#select2-resourceSelection-container";

    private static select2GroupId:string = "#resourceGroup";

    constructor(private http: ApiClient,
                private overlay: Overlay,
                private vcRef: ViewContainerRef,
                private modal: Modal) {
        overlay.defaultViewContainer = vcRef;
    }

    ngOnInit():void {
        // Get all configured resources from the server

        Observable.forkJoin(
            this.http.get(REST.RESOURCE_CONFIG).map((res: Response) => res.json()),
            this.http.get(REST.RGROUP_LIST).map((res: Response) => res.json())
        ).subscribe(data => {
            // filling the resources
            let resData = data[0];
            for (let key in resData) {
                this.resources.push(new Resource(this.http, key, resData[key]))
            }
            if (this.resources.length > 0) {
                this.activeResource = this.resources[0];
                this.oldTypeValue = this.activeResource.type;
                this.oldGroupValue = this.activeResource.groupName;
                this.oldSmartMode = this.activeResource.smartMode;

                let _thisReference = this;
                $(document).ready(function() {
                    $(ResourcesComponent.select2ElementId).select2();
                    $(ResourcesComponent.select2ElementId).on('change', (e) => {
                        _thisReference.selectCurrentlyActiveResource($(e.target).val());
                    });
                });
            }

            // filling the available rgroups
            this.availableGroups = data[1];

            // making the selectionGroup decision after all actions before were performed
            this.groupSelection = this.getGroupSelectionForActiveResource();

        });

        // Get all the available bundles that belong to Resources
        this.http.get(REST.AVAILABLE_RESOURCE_LIST)
            .map((res: Response) => res.json())
            .subscribe(data => this.availableResources = data);

        // Get available thread pools
        this.http.get(REST.THREAD_POOL_CONFIG)
            .map((res:Response) => res.json())
            .subscribe(data => this.availableThreadPools = ThreadPool.makeBunchFromJson(data));
    }

    dispatchNewResource(newResource:Resource):void {
        let _thisReference = this;
        if ($(ResourcesComponent.select2ElementId).data('select2')) {
            $(ResourcesComponent.select2ElementId).select2('destroy');
        }
        $(ResourcesComponent.select2ElementId).select2({
            placeholder: "Select gateway",
            width: '100%',
            allowClear: true
        });
        $(ResourcesComponent.select2ElementId).on('change', (e) => {
            _thisReference.selectCurrentlyActiveResource($(e.target).val());
        });

        if (this.resources.length > 0) {
            this.activeResource = newResource;
            this.oldTypeValue = newResource.type;
            this.oldGroupValue = newResource.groupName;
            this.oldSmartMode = newResource.smartMode;
            $(ResourcesComponent.selectionId).html(this.activeResource.name);
            this.groupSelection = this.getGroupSelectionForActiveResource();
        }
    }

    selectCurrentlyActiveResource(resourceName:string):void {
        for (let i = 0; i < this.resources.length; i++) {
            if (this.resources[i].name == resourceName) {
                this.activeResource = this.resources[i];
                this.oldTypeValue = this.resources[i].type;
                this.oldGroupValue = this.resources[i].groupName;
                this.oldSmartMode = this.resources[i].smartMode;
                this.groupSelection = this.getGroupSelectionForActiveResource();
                break;
            }
        }
    }

    private getGroupSelectionForActiveResource():boolean {
        if (this.availableGroups.length == 0) {
            return false;
        } else if (!isNullOrUndefined(this.activeResource)
                && !isNullOrUndefined(this.activeResource.groupName)
                && this.activeResource.groupName.length > 0) {
            for (let i = 0; i < this.availableGroups.length; i++) {
                if (this.availableGroups[i] == this.activeResource.groupName) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    saveManualGroupName():void {
        this.http.put(REST.RESOURCE_GROUP(this.activeResource.name), this.activeResource.groupName)
            .subscribe(() => console.log("Manual group name has been saved, no reload is required"));
    }

    changeType(event:any):void {
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
            });
    }

    changeGroup(event:any):void {
        this.modal.confirm()
            .isBlocking(true)
            .className(<VEXBuiltInThemes>'default')
            .keyboard(27)
            .message("Setting group for resource leads to appending all entities from the group to resource. " +
                "The page is going to be refreshed. Proceed?")
            .open()
            .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                    .then((response) => {
                        this.oldGroupValue = event.target.value;
                        this.http.put(REST.RESOURCE_GROUP(this.activeResource.name), event.target.value)
                            .subscribe(() => location.reload());
                        return response;
                    })
                    .catch(() => {
                        this.activeResource.groupName = this.oldGroupValue;
                        this.groupSelection = this.getGroupSelectionForActiveResource();
                        return false;
                    });
            });
    }

    changeThreadPool(event:any) {
        this.http.put(REST.RESOURCE_THREAD_POOL(this.activeResource.name), event.target.value)
            .subscribe(() => console.log("Resource thread pool has been changed to " + event.target.value));
    }

    triggerSmartMode():void {
        console.log("current state of smart mode is ", this.activeResource.smartMode, " old one is ", this.oldSmartMode);
        if (!this.oldSmartMode) {
            this.modal.confirm()
                .className(<VEXBuiltInThemes>'default')
                .isBlocking(true)
                .keyboard(27)
                .message("If you enable smartMode - all attributes, events and operations will be removed. Proceed?")
                .open()
                .then((resultPromise) => {
                    return (<Promise<boolean>>resultPromise.result)
                        .then((response) => {
                            this.http.put(REST.ENTITY_PARAMETERS("resource", this.activeResource.name, "smartMode"), true)
                                .subscribe(data => {
                                    console.log("setting to true result is ", data);
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
                .subscribe(() => this.oldSmartMode = false);
        }
    }

    saveConnectionString():void {
        this.http.put(REST.RESOURCE_CONNECTION_STRING(this.activeResource.name),
            this.activeResource.connectionString)
            .subscribe(res => console.log("connection string for " + this.activeResource.name +
                " has been changed to " + this.activeResource.connectionString + " with result " + res));
    }
}
