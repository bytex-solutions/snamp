import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ApiClient } from './app.restClient';
import { Gateway } from './model/model.gateway';
import { KeyValue } from './model/model.entity';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

@Component({
  selector: 'gateways',
  templateUrl: 'app/templates/gateways.html'
})
export class Gateways implements OnInit {

   @ViewChild('newParam') newParamElement:ElementRef;

   gateways:Gateway[] = [];
   activeGateway:Gateway = new Gateway("", "", {});
   http:ApiClient;
   availableGateways :any[] = [];
   constructor(apiClient: ApiClient) {
        this.http = apiClient;
   }

    ngOnInit() {
        // Get all configured gateways from the server
        this.http.get('/snamp/console/gateway')
            .map((res: Response) => res.json())
            .subscribe(data => {
                for (let key in data) {
                    this.gateways.push(new Gateway(key, data[key]['type'], data[key]['parameters']))
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

    saveParameter(parameter:KeyValue) {
        var url = "/snamp/console/gateway/" + this.activeGateway.name + "/parameters/" + parameter.key;
        this.http.put(url, parameter.value)
            .map((res: Response) => res.text())
            .subscribe(data => this.activeGateway.setParameter(parameter));
    }

    removeParameter(parameter:KeyValue) {
        var url = "/snamp/console/gateway/" + this.activeGateway.name + "/parameters/" + parameter.key;
        this.http.delete(url)
            .map((res: Response) => res.text())
            .subscribe(data => this.activeGateway.removeParameter(parameter.key));
    }

    addNewParameter() {
        this.saveParameter(new KeyValue(this.newParamElement.nativeElement.value, "value"));
    }
}
