import { Component } from '@angular/core';
import { ApiClient } from './app.restClient';
import { Gateway } from './model/model.gateway';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

@Component({
  selector: 'gateways',
  templateUrl: 'app/templates/gateways.html'
})
export class Gateways  {
   gateways:Gateway[];
   activeGateway:Gateway;
   http:ApiClient;
   constructor(apiClient: ApiClient) {
        this.http = apiClient;
        this.gateways = [];
        this.prefillData();
        if (this.gateways.length > 0) {
            this.activeGateway = this.gateways[0];
        } else {
            this.activeGateway = new Gateway("", "", {});
        }
   }

    prefillData() {
        this.http.get('/snamp/console/gateway')
            .map((res: Response) => res.json())
            .do(data => console.log('All: ' +  JSON.stringify(data)))
            .subscribe(res => {
                for (let key in res) {
                    this.gateways.push(new Gateway(key, res[key]['type'], res[key]['parameters']))
                }
            });
    }

    setActiveGateway(gateway:any) {
        this.activeGateway = gateway;
    }
}
