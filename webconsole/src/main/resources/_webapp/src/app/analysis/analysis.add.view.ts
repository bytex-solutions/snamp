import { Component, OnInit } from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Router } from '@angular/router';

import { Factory } from './model/objectFactory';
import { E2EView } from './model/abstract.e2e.view';

import { ViewService } from '../services/app.viewService';

import 'rxjs/add/operator/publishLast';
import 'rxjs/add/operator/cache';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/of';

@Component({
  moduleId: module.id,
  templateUrl: './templates/addView.html',
  styleUrls: [ './templates/css/addView.css' ]
})
export class AddView implements OnInit {

    private http:ApiClient;
    components:Observable<string[]>;
    types:Description[] = Description.createViewTypes();

    chosenComponent:string = undefined;
    viewType:Description = undefined;
    viewName:string = undefined;

    shelfLife:number = 1;
    useShelfLife:boolean = false;

    periods = Description.createPeriodsTypes();


    constructor(apiClient: ApiClient, private _viewService:ViewService, private _router: Router) {
        this.http = apiClient;
   }

   ngOnInit():void {
        this.components = this.http.get(REST.GROUPS_WEB_API)
            .map((res:Response) => { return <string[]>res.json()})
            .publishLast().refCount();
   }

   saveView():void {
        console.log("Trying to append following view: ", this.viewName, this.viewType.id, this.chosenComponent,
            this.useShelfLife ? this.shelfLife * 1000 : undefined);
        let _view:E2EView = Factory.createView(this.viewName, this.viewType.id, this.chosenComponent,
                this.useShelfLife ? this.shelfLife * 1000 : undefined);
        this._viewService.newView(_view);
        this._router.navigateByUrl('/view/' + _view.name);
   }
}

class Description {
    public name:string = "";
    public id:string = "";
    public description:string = "";
    public period:number = 0; //in seconds

    constructor(name:string, id:string, description:string, period?:number) {
        this.name = name;
        this.id = id;
        this.description = description;
        this.period = period;
    }

    public static createPeriodsTypes():Description[] {
        let result:Description[] = [];
        result.push(new Description("", "", "1 second", 1));
        result.push(new Description("", "", "1 minute", 60));
        result.push(new Description("", "", "5 minutes", 300));
        result.push(new Description("", "", "15 minutes", 900));
        result.push(new Description("", "", "1 hour", 3600));
        result.push(new Description("", "", "12 hours", 43200));
        result.push(new Description("", "", "24 hours", 86400));
        return result;
    }

    public static createViewTypes():Description[] {
        let result:Description[] = [];
        result.push(new Description("Landscape view", E2EView.LANDSCAPE, "Represents E2E view of all components in IT landscape"));
        result.push(new Description("Child components view", E2EView.CHILD_COMPONENT, "Represents E2E view of child components"));
        result.push(new Description("Component modules view",  E2EView.COMPONENT_MODULES, "Represents communications scheme between the modules within the component"));
        return result;
    }
}

