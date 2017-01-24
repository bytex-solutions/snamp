import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Response } from '@angular/http';
import { LocalStorageService } from 'angular-2-local-storage';
import { Subject } from 'rxjs/Subject';
import { ApiClient, REST } from './app.restClient';

import { Dashboard } from './charts/model/dashboard';
import { AbstractChart } from './charts/model/abstract.chart';
import { Factory } from './charts/model/objectFactory';

@Injectable()
export class ChartService {
    private MAX_SIZE:number = 1000;
    private SPLICE_COUNT:number = 30; // how many elements will we delete from the end of the array
    private RECENT_COUNT:number = 15; // default count of the recent message
    private KEY:string = "snampCharts";
    private chartObs:Subject<Dashboard>;
    private _dashboard:Dashboard;

    private loadDashboard():void {
        console.log("Loading some dashboard...");
        this._http.get(REST.CHART_DASHBOARD)
            .map((res:Response) => {
                console.log("Result of dashboard request is: ", res);
                return res.json();
            })
            .subscribe(data => {
                console.log(data);
                this._dashboard = new Dashboard();
                if (data["charts"] != undefined && data["charts"].length > 0) {
                    for (let i = 0; i < data["charts"]; i++) {
                        this._dashboard.charts.push(Factory.chartFromJSON(data["charts"][i]));
                    }
                }
                console.log(this._dashboard);
            });
    }

    private saveDashboard():void {
        console.log("Saving some dashboard... ");
         this._http.put(REST.CHART_DASHBOARD, JSON.stringify(this._dashboard.toJSON()))
            .map((res:Response) => {
                console.log("Result of dashboard request is: ", res);
                return res.json();
            })
            .subscribe(data => {
                console.log("Dashboard has been saved successfully");
            });
    }

    constructor(private localStorageService: LocalStorageService, private _http:ApiClient) {
          this.chartObs = new Subject<Dashboard>();
          this.loadDashboard();
    }

    getChartObs():Observable<Dashboard> {
        return this.chartObs.asObservable().share();
    }

    pushNewChartData(_data:any):void {
        console.log("New chart data is: ", _data);
    }

    hasChartWithName(name:string):boolean {
        let _value:boolean = false;
        for (let i = 0; i < this._dashboard.charts.length; i++) {
            if (this._dashboard.charts[i].name == name) {
                _value = true;
                break;
            }
        }
        return _value;
    }

    newChart(chart:AbstractChart):void {
        if (this.hasChartWithName) {
            throw new Error("Chart with that name already exists!");
        } else {
            this._dashboard.charts.push(chart);
            this.saveDashboard();
        }
    }
}
