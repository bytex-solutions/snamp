import { Component, OnInit } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Factory } from './model/factory';
import { Watcher } from './model/watcher';

import { Router } from '@angular/router';

import 'rxjs/add/operator/publishLast';
import 'select2';

@Component({
  moduleId: module.id,
  templateUrl: './templates/main.html',
  styleUrls: [ './templates/css/main.css' ]
})
export class MainComponent implements OnInit {

    private http:ApiClient;
    components:string[] = [];
    private watchers:Watcher[] = [];
    activeWatcher:Watcher = new Watcher(undefined, {});
    isNewEntity:boolean = true;

    selectedComponent:string = undefined;

    constructor(apiClient: ApiClient, private _router: Router) {
        this.http = apiClient;
   }

   ngOnInit():void {
        // load the list of watchers
        this.http.get(REST.WATCHERS_LIST)
            .map((res:Response) => res.json())
            .subscribe((data) => {
                this.watchers = Factory.watchersArrayFromJSON(data);
            });

         // find all the components
        this.http.get(REST.CHART_COMPONENTS)
            .map((res:Response) => { return <string[]>res.json()})
            .subscribe((data) => {
                this.components = data;
                if (this.components.length > 0) {
                    this.selectedComponent = this.components[0];
                }
            });

        var _thisReference = this;
        // initialize select2 logic
        $(document).ready(function() {
             $("#componentSelection").select2();
             $("#componentSelection").on('change', (e) => {
                  _thisReference.selectCurrentComponent($(e.target).val());
             });
        });
   }

   private selectCurrentComponent(component:string):void {
        this.selectedComponent = component;
   }

   public getAvailableComponents():string[] {
        return this.components.filter((element) => {
            let _available:boolean = true;
            for (let i = 0; i < this.watchers.length; i++) {
                if (this.watchers[i].name == element) {
                    _available = false;
                    break;
                }
            }
            return _available;
        });
   }

    public getPanelHeader():string {
        return this.isNewEntity ? "Add new watcher" : ("Edit watcher " + this.activeWatcher.name);
    }

}