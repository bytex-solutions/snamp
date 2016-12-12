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
import { Modal } from 'angular2-modal/plugins/vex';

@Component({
  templateUrl: 'app/configuration/templates/resources.html'
})
export class ResourcesComponent implements OnInit {

   resources:Resource[] = [];
   activeResource:Resource;
   oldTypeValue:string = "";
   http:ApiClient;
   availableResources :any[] = [];

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
                    this.resources.push(new Resource(this.http, key,
                        Resource.removeGroupNameFromParametersIfExists(data[key]["parameters"]), data[key]))
                }
                this.activeResource = (this.resources.length > 0) ? this.resources[0] : this.activeResource;
                this.oldTypeValue = this.activeResource.type;
            });

        // Get all the available bundles that belong to Resources
        this.http.get(REST.AVAILABLE_RESOURCE_LIST)
            .map((res: Response) => res.json())
            .subscribe(data => this.availableResources = data);
    }

    setActiveResource(resource:Resource) {
        this.activeResource = resource;
        this.oldTypeValue = resource.type;
    }

    changeType(event:any) {
         let dialog = this.modal.confirm()
            .isBlocking(true)
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
}
