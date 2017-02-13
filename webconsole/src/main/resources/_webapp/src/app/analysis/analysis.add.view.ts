import { Component, OnInit } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Factory } from './model/objectFactory';
import { E2EView } from './model/abstract.e2e.view';

import { ViewService } from '../app.viewService';

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
    types:ViewType[] = ViewType.createViewTypes();

    chosenComponent:string = undefined;
    viewType:ViewType = undefined;
    viewName:string = undefined;

    constructor(apiClient: ApiClient, private _viewService:ViewService) {
        this.http = apiClient;
   }

   ngOnInit():void {
        this.components = this.http.get(REST.CHART_COMPONENTS)
            .map((res:Response) => { return <string[]>res.json()})
            .publishLast().refCount();
   }

   saveView():void {
        let _view:E2EView = Factory.createView(this.viewName, this.viewType.id, this.chosenComponent);
        this._viewService.newView(_view);
        console.log("New view has been appended successfully");
   }
}

class ViewType {
    public name:string = "";
    public id:string = "";
    public description:string = "";

    constructor(name:string, id:string, description:string) {
        this.name = name;
        this.id = id;
        this.description = description;
    }

    public static createViewTypes():ViewType[] {
        let result:ViewType[] = [];
        result.push(new ViewType("Landscape view", E2EView.LANDSCAPE, "Represents E2E view of all components in IT landscape"));
        result.push(new ViewType("Child components view", E2EView.CHILD_COMPONENT, "Represents E2E view of child components"));
        result.push(new ViewType("Component modules view",  E2EView.COMPONENT_MODULES, "Represents communications scheme between the modules within the component"));
        return result;
    }
}

