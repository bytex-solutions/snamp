import { Component} from '@angular/core';
import { ApiClient } from './app.restClient';
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
   gateways:any;
   http:ApiClient;
   constructor(apiClient: ApiClient) {
        this.http = apiClient;
        this.prefillData();
   }

    prefillData() {
        this.http.get('/snamp/console/gateway')
            .map((res: Response) => res.json())
            .do(data => console.log('All: ' +  JSON.stringify(data)))
            .subscribe(res => this.gateways = res);
    }
}
