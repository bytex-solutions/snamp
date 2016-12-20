import { Component} from '@angular/core';
import { ApiClient } from './app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

@Component({
  selector: 'username',
  template: '{{username}}'
})
export class UsernameComponent  {
   username:string;
   constructor(private apiClient: ApiClient) {
        apiClient.get('/snamp/security/login/username')
            .map((res: Response) => res.text())
            .subscribe(res => this.username = res, err => {if (err.status == 500) {window.location.href = "login.html?tokenExpired=true";}})
   }
}
