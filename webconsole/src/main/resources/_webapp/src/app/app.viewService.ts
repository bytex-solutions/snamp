import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Response } from '@angular/http';
import { LocalStorageService } from 'angular-2-local-storage';
import { Subject } from 'rxjs/Subject';
import { ApiClient, REST } from './app.restClient';

import { Dashboard } from './analysis/model/dashboard';
import { E2EView } from './analysis/model/abstract.e2e.view';
import { Factory } from './analysis/model/objectFactory';

@Injectable()
export class ViewService {
    private KEY_DATA:string = "snampViewData";
    private _dashboard:Dashboard;
    private viewSubjects:{ [key:string]: Subject<any> } = {};

    constructor(private localStorageService: LocalStorageService, private _http:ApiClient) {
          this.loadDashboard();
          if (this.localStorageService.get(this.KEY_DATA) == undefined) {
               this.localStorageService.set(this.KEY_DATA, {});
          }
    }

    public getViews():E2EView[] {
        return this._dashboard.views;
    }


    public getViewNames():string[] {
        return this._dashboard.views.map(element => element.name);
    }

    private loadDashboard():void {
        console.log("Loading some dashboard for views...");
        this._http.get(REST.VIEWS_DASHBOARD)
            .map((res:Response) => {
                console.log("Result of dashboard request is: ", res);
                return res.json();
            })
            .subscribe(data => {
                this._dashboard = new Dashboard();
                this.viewSubjects = {};
                if (data.views.length > 0) {
                    for (let i = 0; i < data.views.length; i++) {
                        let _currentView:E2EView = Factory.viewFromJSON(data.charts[i]);
                        this.viewSubjects[_currentView.name] = new Subject<any>();
                       // _currentView.subscribeToSubject(this.viewSubjects[_currentView.name]);
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
            this.viewSubjects[view.name] = new Subject<any>();
           // view.subscribeToSubject(this.viewSubjects[view.name]);
            this.saveDashboard();
        }
    }

    removeView(viewName:string):void {
        for (let i = 0; i < this._dashboard.views.length; i++ ) {
            if (this._dashboard.views[i].name == viewName) {

                // remove the view from the dashboard
                this._dashboard.views.splice(i, 1);

                // nullify the corresponding subject
                this.viewSubjects[viewName] = undefined;

                // save the dashboard
                this.saveDashboard();
                return;
            }
        }
        throw new Error("Could not find a view " + viewName);
    }

    getObservableForView(name:string):Observable<any> {
        if (this.viewSubjects[name] != undefined) {
            return this.viewSubjects[name].asObservable().share();
        } else {
            throw new Error("Cannot find any subject for view " + name);
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