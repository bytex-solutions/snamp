import { Component, OnInit, ViewContainerRef, ViewEncapsulation } from '@angular/core';
import { ApiClient } from './app.restClient';
import { Gateway } from './model/model.gateway';
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
  selector: 'gateways',
  templateUrl: 'app/templates/gateways.html'
})
export class Gateways implements OnInit {

   gateways:Gateway[] = [];
   activeGateway:Gateway;
   oldTypeValue:string = "";
   http:ApiClient;
   availableGateways :any[] = [];

   constructor(apiClient: ApiClient, overlay: Overlay, vcRef: ViewContainerRef, public modal: Modal) {
        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;
   }

    ngOnInit() {
        // Get all configured gateways from the server
        this.http.get('/snamp/console/gateway')
            .map((res: Response) => res.json())
            .subscribe(data => {
                for (let key in data) {
                    this.gateways.push(new Gateway(this.http, key, data[key]['type'], data[key]['parameters']))
                }
                this.activeGateway = (this.gateways.length > 0) ? this.gateways[0] : this.activeGateway;
                this.oldTypeValue = this.activeGateway.type;
            });

        // Get all the available bundles that belong to Gateways
        this.http.get('/snamp/console/management/gateways')
            .map((res: Response) => res.json())
            .subscribe(data => this.availableGateways = data);
    }

    setActiveGateway(gateway:Gateway) {
        this.activeGateway = gateway;
        this.oldTypeValue = gateway.type;
    }

    changeType(event:any) {
         let dialog = this.modal.confirm()
            .isBlocking(true)
            .keyboard(27)
            .message("If you change gateway type - all resources will be removed. Are you sure?")
            .open()
            .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                  .then((response) => {
                    this.oldTypeValue = event.target.value;
                    this.http.put("/snamp/console/gateway/" + this.activeGateway.name + "/type", event.target.value);
                    return response;
                  })
                  .catch(() => {
                    this.activeGateway.type = this.oldTypeValue;
                    return false;
                  });
              });
    }
}
