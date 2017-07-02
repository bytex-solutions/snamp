import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { Resource } from './model/model.resource';
import { Response } from '@angular/http';
import { Overlay } from 'angular2-modal';
import { VEXBuiltInThemes, Modal } from 'angular2-modal/plugins/vex';
import { ThreadPool } from "./model/model.thread.pool";

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

    private static select2ElementId:string = "#resourceSelection";

    constructor(private http: ApiClient,
                private overlay: Overlay,
                private vcRef: ViewContainerRef,
                private modal: Modal) {
        overlay.defaultViewContainer = vcRef;
    }

    ngOnInit():void {
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
            .subscribe(data => this.availableGroups = data);

        // Get available thread pools
        this.http.get(REST.THREAD_POOL_CONFIG)
            .map((res:Response) => res.json())
            .subscribe(data => this.availableThreadPools = ThreadPool.makeBunchFromJson(data));
    }

    initSelectionComponent():void {
        $(ResourcesComponent.select2ElementId).select2('destroy');
        $(ResourcesComponent.select2ElementId).select2();
    }

    ngAfterViewInit():void {
        let _thisReference = this;
        $(document).ready(function() {
            $(ResourcesComponent.select2ElementId).select2();
            $(ResourcesComponent.select2ElementId).on('change', (e) => {
                _thisReference.selectCurrentlyActiveResource($(e.target).val());
            });
        });
    }

    selectCurrentlyActiveResource(resourceName:string):void {
        let selection:Resource;
        for (let i = 0; i < this.resources.length; i++) {
            if (this.resources[i].name == resourceName) {
                selection = this.resources[i];
            }
        }
        this.activeResource = selection;
        this.oldTypeValue = selection.type;
        this.oldGroupValue = selection.groupName;
        this.oldSmartMode = selection.smartMode;
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
