import { Component} from '@angular/core';
import { ApiClient } from './app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

@Component({
  selector: 'app-header',
  template: 'Hello, {{username}}!'
})
export class Header  {
   username:string;
   constructor(private apiClient: ApiClient) {
        apiClient.get('/snamp/console/username')
            .map((res: Response) => res.text())
            .subscribe(res => this.username = res)
   }
}