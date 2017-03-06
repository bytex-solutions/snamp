import { Component, OnInit } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Factory } from './model/factory';

import { Router } from '@angular/router';

@Component({
  moduleId: module.id,
  templateUrl: './templates/main.html',
  styleUrls: [ './templates/css/main.css' ]
})
export class MainComponent implements OnInit {

    private http:ApiClient;

    constructor(apiClient: ApiClient, private _router: Router) {
        this.http = apiClient;
   }

   ngOnInit():void {
        this.http.get(REST.WATCHERS_LIST)
            .map((res:Response) => res.json())
            .subscribe((data) => {
                console.log("Watchers list is: ", data, Factory.watchersArrayFromJSON(data));
            });

   }

}