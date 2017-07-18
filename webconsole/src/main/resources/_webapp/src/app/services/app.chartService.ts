import { HostListener, Injectable, OnDestroy } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Response } from '@angular/http';
import { Subject } from 'rxjs/Subject';
import { ApiClient, REST } from './app.restClient';

import { Dashboard } from '../charts/model/dashboard';
import { AbstractChart } from '../charts/model/abstract.chart';
import { Factory } from '../charts/model/objectFactory';
import { ChartData } from "../charts/model/data/abstract.data";

import { ChartDataFabric } from "../charts/model/data/fabric";
import { isNullOrUndefined } from "util";
import { ChartWithGroupName } from "../charts/model/charts/group.name.based.chart";
import { ResourceGroupHealthStatusChart } from "../charts/model/charts/resource.group.health.status";
import { BehaviorSubject } from "rxjs/BehaviorSubject";

import 'rxjs/add/operator/publishLast';
import 'rxjs/add/operator/cache';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/of';

@Injectable()
export class ChartService implements OnDestroy {

    @HostListener('window:beforeunload', ['$event'])
    beforeunloadHandler(event) {
        if (!isNullOrUndefined(this._dashboard) && !isNullOrUndefined(this._dashboard.groups)
                                                            &&!isNullOrUndefined(this.groupTimers)) {
            this._dashboard.groups.forEach((gn: string) => {
                if (!isNullOrUndefined(this.groupTimers[gn])) {
                    clearInterval(this.groupTimers[gn]);
                }
            });
        }
    }

    private _dashboard:Dashboard;
    private chartSubjects:{ [key:string]: BehaviorSubject<ChartData[]> } = {};
    private computeSubscriber:any = undefined;
    private saveSubscriber:any = undefined;

    private groups:Subject<string[]> = new Subject<string[]>();
    private charts:BehaviorSubject<AbstractChart[]>;

    public groupSubjects:{ [key:string]: BehaviorSubject<AbstractChart[]>} = {};
    public groupTimers: {[key:string]: any} = {};

    constructor(private _http:ApiClient) {
          this.loadDashboard();
    }

    public getSimpleGroupName():string[] {
        return this._dashboard.groups;
    }

    public getChartsByGroupName(groupName:string):Observable<AbstractChart[]> {
        return this.groupSubjects[groupName].asObservable();
    }

    public removeChartsByGroupName(groupName:string):void {
        for (let i = 0; i < this._dashboard.groups.length; i++) {
            if (this._dashboard.groups[i] == groupName) {
                this._dashboard.groups.splice(i, 1);
                break;
            }
        }
        for (let i = 0; i < this._dashboard.charts.length; i++) {
            if (this._dashboard.charts[i].getGroupName() == groupName) {
                if (!isNullOrUndefined(this._dashboard.charts[i].subscriber)) {
                    this._dashboard.charts[i].subscriber.unsubscribe();
                    this._dashboard.charts[i].subscriber = undefined;
                }
                if (!isNullOrUndefined(this.chartSubjects[this._dashboard.charts[i].name])) {
                    this.chartSubjects[this._dashboard.charts[i].name].unsubscribe();
                    this.chartSubjects[this._dashboard.charts[i].name] = undefined;
                }
                this._dashboard.charts.splice(i, 1);
            }
        }
        this.groupSubjects[groupName].unsubscribe();
        this.groupSubjects[groupName] = undefined;
        this.saveDashboard();
    }

    public getChartByName(chartName:string):AbstractChart {
        return this._dashboard.charts.find(_ch => (_ch.name == chartName));
    }

    public getGroups():Observable<string[]> {
        return this.groups.asObservable().share();
    }

    public addNewGroup(groupName:string):void {
        this._dashboard.groups.push(groupName);
        this.groupSubjects[groupName] = new BehaviorSubject<AbstractChart[]>([]);
        this.saveDashboard();
    }

    private static stringifyArray(_chs:AbstractChart[]):any {
        let _chArrJson:any[] = [];
        for (let i = 0; i < _chs.length; i++) {
            _chArrJson.push(_chs[i].toJSON());
        }
        return _chArrJson;
    }

    public saveChartsPreferences(chartName:string, preferences:any):void {
        this.getChartByName(chartName).preferences["gridcfg"] = preferences;
        this.saveDashboard();
    }

    public receiveDataForCharts(_chs:AbstractChart[]):void {
        this.computeSubscriber = this._http.post(REST.CHARTS_COMPUTE, ChartService.stringifyArray(_chs))
            .map((res:Response) => res.json())
            .subscribe(data => {
                this.pushNewChartData(data);
            });
    }

    private loadDashboard():void {
        this._http.get(REST.CHART_DASHBOARD).map((res:Response) => res.json()).subscribe(data => {
            this.groups.next(isNullOrUndefined(data["groups"]) ? [] : data["groups"]);
            this._dashboard = new Dashboard();
            this.chartSubjects = {};
            if (data.charts.length > 0) {
                for (let i = 0; i < data.charts.length; i++) {
                    let _currentChart:AbstractChart = Factory.chartFromJSON(data.charts[i]);
                    this.chartSubjects[_currentChart.name] = new BehaviorSubject<ChartData[]>([]);
                    _currentChart.subscribeToSubject(this.chartSubjects[_currentChart.name]);
                    this._dashboard.charts.push(_currentChart);
                }
                this.charts = new BehaviorSubject<AbstractChart[]>(this._dashboard.charts);
            } else {
                this.charts = new BehaviorSubject<AbstractChart[]>([]);
            }
            this._dashboard.groups = data.groups;

            if (!isNullOrUndefined(data["groups"]) && data.groups.length > 0) {
                this._dashboard.groups.forEach((element:string) => {
                    let _groupCharts:AbstractChart[] = [];
                    this._dashboard.charts.forEach((chart:AbstractChart) => {
                        if (chart.getGroupName() == element) {
                            _groupCharts.push(chart);
                        }
                    });
                    this.groupSubjects[element] = new BehaviorSubject<AbstractChart[]>(_groupCharts);
                })
            }
        });
    }

    public saveDashboard():void {
        console.debug("Saving chart dashboard: ", REST.CHART_DASHBOARD, JSON.stringify(this._dashboard.toJSON()));
         this.saveSubscriber = this._http.put(REST.CHART_DASHBOARD, JSON.stringify(this._dashboard.toJSON()))
            .subscribe(() => {
                console.debug("Dashboard has been saved successfully");
                this.groups.next(this._dashboard.groups);
                this.charts.next(this._dashboard.charts);
                this._dashboard.groups.forEach((element:string) => {
                    this.groupSubjects[element].next(this._dashboard.charts.filter((chart:AbstractChart) => (chart.getGroupName() == element)));
                })

         });
    }

    pushNewChartData(_data:any):void {
        // loop through all the data we have received
        for (let _currentChartName in _data) {
            // create a chart data instances
            let _d:any[] = _data[_currentChartName];
            let _allChartData:ChartData[] = [];
            for (let i = 0; i < _d.length; i++) {
                _allChartData.push(ChartDataFabric.chartDataFromJSON(this.getChartByName(_currentChartName).type, _d[i]));
                // append this data for this data array
            }
            // notify all the components that something has changed
            if (this.chartSubjects[_currentChartName] != undefined) {
                this.chartSubjects[_currentChartName].next(_allChartData);
            }

        }
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
        if (this.hasChartWithName(chart.name)) {
            throw new Error("Chart with that name already exists!");
        } else {
            this._dashboard.charts.push(chart);
            this.chartSubjects[chart.name] = new BehaviorSubject<ChartData[]>([]);
            chart.subscribeToSubject(this.chartSubjects[chart.name]);
            this.saveDashboard();
        }
    }

    removeChart(chartName:string):void {
        for (let i = 0; i < this._dashboard.charts.length; i++ ) {
            if (this._dashboard.charts[i].name == chartName) {

                if (!isNullOrUndefined(this._dashboard.charts[i].subscriber)) {
                    this._dashboard.charts[i].subscriber.unsubscribe();
                    this._dashboard.charts[i].subscriber = undefined;
                }
                // remove the chart from the dashboard
                this._dashboard.charts.splice(i, 1);

                // nullify the corresppnding subject
                this.chartSubjects[chartName] = undefined;

                // save the dashboard
                this.saveDashboard();
                return;
            }
        }
        throw new Error("Could not find a chart " + chartName);
    }

    resetChart(chart:ChartWithGroupName):void {
        console.debug("reseting the chart: ", chart.group); // @todo replace instance of to enum
        this._http.post(REST.RESET_ELASTICITY(chart.group, chart instanceof ResourceGroupHealthStatusChart ? "groupStatus" : "elasticity"))
            .subscribe(() => console.debug("Elasticity state has been reset for ", chart.group))
    }

    ngOnDestroy():void {
        this.charts.subscribe((data:AbstractChart[]) => {
            for (let i = 0; i < data.length; i++) {
                if (!isNullOrUndefined(data[i].subscriber)) {
                    data[i].subscriber.unsubscribe();
                }
            }
        }).unsubscribe();
        if (!isNullOrUndefined(this.computeSubscriber)) {
            this.computeSubscriber.unsubscribe();
        }
        if (!isNullOrUndefined(this.saveSubscriber)) {
            this.saveSubscriber.unsubscribe();
        }
    }
}
