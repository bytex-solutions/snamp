import { Component, OnInit} from '@angular/core';
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
export class Gateways implements OnInit {
   gateways:Gateway[] = [];
   activeGateway:Gateway = new Gateway("", "", {});
   http:ApiClient;
   availableGateways :any[] = [];
   constructor(apiClient: ApiClient) {
        this.http = apiClient;
   }

    ngOnInit() {
        this.http.get('/snamp/console/gateway')
            .map((res: Response) => res.json())
            .do(data => console.log('All: ' + data))
            .subscribe(data => {
                for (let key in data) {
                    this.gateways.push(new Gateway(key, data[key]['type'], data[key]['parameters']))
                }
                this.activeGateway = (this.gateways.length > 0) ? this.gateways[0] : this.activeGateway;
                console.log(this.activeGateway);
            });

        this.http.get('/snamp/console/management/gateways')
            .map((res: Response) => res.json())
            .subscribe(data => this.availableGateways = data);
    }

    setActiveGateway(gateway:any) {
        this.activeGateway = gateway;
    }
}
