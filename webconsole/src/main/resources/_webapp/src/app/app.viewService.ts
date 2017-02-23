import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Response } from '@angular/http';
import { Subject } from 'rxjs/Subject';
import { ApiClient, REST } from './app.restClient';

import { Dashboard } from './analysis/model/dashboard';
import { E2EView } from './analysis/model/abstract.e2e.view';
import { Factory } from './analysis/model/objectFactory';

import 'rxjs/add/operator/publishLast';
import 'rxjs/add/operator/cache';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/of';


@Injectable()
export class ViewService {
    private _dashboard:Dashboard;
    private viewNames:Subject<string[]> = new Subject();

    constructor(private _http:ApiClient) {
          this.loadDashboard();
    }

    public getViews():E2EView[] {
        return this._dashboard.views;
    }


    public getViewNames():Observable<string[]> {
        return this.viewNames.asObservable().share();
    }

    private loadDashboard():void {
        console.log("Loading some dashboard for views...");
        let _res:any = this._http.get(REST.VIEWS_DASHBOARD)
            .map((res:Response) => {
                console.log("Result of dashboard request is: ", res);
                return res.json();
            }).publishLast().refCount();

        _res.subscribe(data => {
            this._dashboard = new Dashboard();
            this.viewNames.next(data.views.map(_d => _d.name));
            if (data.views.length > 0) {
                for (let i = 0; i < data.views.length; i++) {
                    let _currentView:E2EView = Factory.viewFromJSON(data.views[i]);
                    this._dashboard.views.push(_currentView);
                }
            }
            console.log(this._dashboard);
        });
    }

    public saveDashboard():void {
        console.log("Saving some dashboard... ");
         this._http.put(REST.VIEWS_DASHBOARD, JSON.stringify(this._dashboard.toJSON()))
            .subscribe(data => {
                console.log("Dashboard has been saved successfully");
            });
    }

    newView(view:E2EView):void {
        if (this.hasViewWithName(view.name)) {
            throw new Error("View with that name already exists!");
        } else {
            console.log("New created view is: ", view);
            this._dashboard.views.push(view);
            this.viewNames.next(this._dashboard.views.map(data => data.name));
            this.saveDashboard();
        }
    }

    removeView(viewName:string):void {
        for (let i = 0; i < this._dashboard.views.length; i++ ) {
            if (this._dashboard.views[i].name == viewName) {

                // remove the view from the dashboard
                this._dashboard.views.splice(i, 1);

                // save the dashboard
                this.saveDashboard();
                return;
            }
        }
        throw new Error("Could not find a view " + viewName);
    }

    getDataForView(view:E2EView):Observable<any> {
        return this._http.post(REST.COMPUTE_VIEW, view.toJSON())
            .map((data:Response) => data.json());
    }

    resetView(view:E2EView):Observable<any> {
        return this._http.post(REST.RESET_VIEW, view.toJSON())
            .map((data:Response) => data.text());
    }

    getViewByName(name:string):E2EView {
        let result:E2EView = undefined;
        for (let i = 0; i < this._dashboard.views.length; i++) {
            if (this._dashboard.views[i].name == name) {
                result = this._dashboard.views[i];
                break;
            }
        }
        if (result == undefined) {
            throw new Error("Could not find a view with name" + name);
        } else {
            return result;
        }
    }

    hasViewWithName(name:string):boolean {
        let _value:boolean = false;
        for (let i = 0; i < this._dashboard.views.length; i++) {
            if (this._dashboard.views[i].name == name) {
                _value = true;
                break;
            }
        }
        return _value;
    }
}
