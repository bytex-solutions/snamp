import { Component, OnInit } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Factory } from './model/factory';
import { Watcher } from './model/watcher';

import { Router } from '@angular/router';

import 'rxjs/add/operator/publishLast';

@Component({
  moduleId: module.id,
  templateUrl: './templates/main.html',
  styleUrls: [ './templates/css/main.css' ]
})
export class MainComponent implements OnInit {

    private http:ApiClient;
    components:Observable<string[]>;
    private watchers:Watcher[] = [];
    activeWatcher:Watcher = new Watcher(undefined, {});
    isNewEntity:boolean = true;

    constructor(apiClient: ApiClient, private _router: Router) {
        this.http = apiClient;
   }

   ngOnInit():void {
        this.http.get(REST.WATCHERS_LIST)
            .map((res:Response) => res.json())
            .subscribe((data) => {
                this.watchers = Factory.watchersArrayFromJSON(data);
                console.log("Watchers list is: ", data, this.watchers);
            });

        this.components = this.http.get(REST.CHART_COMPONENTS)
            .map((res:Response) => { return <string[]>res.json()})
            .publishLast().refCount();

   }

    public getPanelHeader():string {
        return this.isNewEntity ? "Add new watcher" : ("Edit watcher " + this.activeWatcher.name);
    }

}