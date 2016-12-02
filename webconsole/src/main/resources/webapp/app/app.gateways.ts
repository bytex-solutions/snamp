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
import { Modal } from 'angular2-modal/plugins/bootstrap';

@Component({
  selector: 'gateways',
  templateUrl: 'app/templates/gateways.html'
})
export class Gateways implements OnInit {

   gateways:Gateway[] = [];
   activeGateway:Gateway;
   http:ApiClient;
   availableGateways :any[] = [];

   constructor(apiClient: ApiClient, overlay: Overlay, vcRef: ViewContainerRef, public modal: Modal) {
        this.http = apiClient;
        this.activeGateway = new Gateway(apiClient, "", "", {});
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
                console.log(this.activeGateway);
            });

        // Get all the available bundles that belong to Gateways
        this.http.get('/snamp/console/management/gateways')
            .map((res: Response) => res.json())
            .subscribe(data => this.availableGateways = data);
    }

    setActiveGateway(gateway:Gateway) {
        this.activeGateway = gateway;
    }

    showDetails(binding:Binding) {
        this.modal.alert()
            .size('lg')
            .showClose(true)
            .title("Binding information for entity " + binding.name)
            .body(`
                <h4>Binding name: </h4>
                <b>Configuration:</b>
                <ul>
                    <li>Non blocking (click anywhere outside to dismiss)</li>
                    <li>Size large</li>
                    <li>Dismissed with default keyboard key (ESC)</li>
                    <li>Close wth button click</li>
                    <li>HTML content</li>
                </ul>`)
            .open();
    }
}
