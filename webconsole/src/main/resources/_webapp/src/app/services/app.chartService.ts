import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Response } from '@angular/http';
import { LocalStorageService } from 'angular-2-local-storage';
import { Subject } from 'rxjs/Subject';
import { ApiClient, REST } from './app.restClient';

import { Dashboard } from '../charts/model/dashboard';
import { AbstractChart } from '../charts/model/abstract.chart';
import { Factory } from '../charts/model/objectFactory';
import { ChartData } from "../charts/model/data/abstract.data";

import 'rxjs/add/operator/publishLast';
import 'rxjs/add/operator/cache';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/of';
import { ChartDataFabric } from "../charts/model/data/fabric";
import { isNullOrUndefined } from "util";

@Injectable()
export class ChartService {
    private KEY_DATA:string = "snampChartData";
    private _dashboard:Dashboard;
    private chartSubjects:{ [key:string]: Subject<ChartData[]> } = {};

    private groups:Subject<string[]> = new Subject<string[]>();

    constructor(private localStorageService: LocalStorageService, private _http:ApiClient) {
          this.loadDashboard();
          if (this.localStorageService.get(this.KEY_DATA) == undefined) {
               this.localStorageService.set(this.KEY_DATA, {});
          }
    }

    public getCharts():AbstractChart[] {
        return this._dashboard.charts;
    }

    public getSimpleGroupName():string[] {
        return this._dashboard.groups;
    }

    public getChartsByGroupName(groupName:string):AbstractChart[] {
        return this._dashboard.charts.filter(_ch => (_ch.getGroupName() == groupName));
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
                this._dashboard.charts.splice(i, 1);
            }
        }
        this.saveDashboard();
    }

    public getChartByName(chartName:string):AbstractChart {
        return this._dashboard.charts.filter(_ch => (_ch.name == chartName))[0];
    }

    public getGroups():Observable<string[]> {
        return this.groups.asObservable().share();
    }

    public addNewGroup(groupName:string):void {
        this._dashboard.groups.push(groupName);
        this.saveDashboard();
    }

    public receiveChartDataForCharts(_chs:AbstractChart[]):void {
         let _chArrJson:any[] = [];
         for (let i = 0; i < _chs.length; i++) {
            _chArrJson.push(_chs[i].toJSON());
         }
         this._http.post(REST.CHARTS_COMPUTE, _chArrJson)
            .map((res:Response) => res.json())
            .subscribe(data => {
                this.pushNewChartData(data);
            });
    }

    public receiveChartDataForGroupName(gn:string):void {
         this.receiveChartDataForCharts(this.getChartsByGroupName(gn));
    }

    private loadDashboard():void {
        let _res:any = this._http.get(REST.CHART_DASHBOARD).map((res:Response) => res.json()).publishLast().refCount();
        _res.subscribe(data => {
            this.groups.next(isNullOrUndefined(data["groups"]) ? [] : data["groups"]);
            this._dashboard = new Dashboard();
            this.chartSubjects = {};
            if (data.charts.length > 0) {
                for (let i = 0; i < data.charts.length; i++) {
                    let _currentChart:AbstractChart = Factory.chartFromJSON(data.charts[i]);
                    this.chartSubjects[_currentChart.name] = new Subject<ChartData[]>();
                    _currentChart.subscribeToSubject(this.chartSubjects[_currentChart.name]);
                    this._dashboard.charts.push(_currentChart);
                }
            }
            this._dashboard.groups = data.groups;
        });
    }

    public saveDashboard():void {
         this._http.put(REST.CHART_DASHBOARD, JSON.stringify(this._dashboard.toJSON()))
            .subscribe(() => {
                console.log("Dashboard has been saved successfully");
                this.groups.next(this._dashboard.groups);
         });
    }

    pushNewChartData(_data:any):void {
        // loop through all the data we have received
        for (let _currentChartName in _data) {
            // create a chart data instances
            let _d:any[] = _data[_currentChartName];
            let _allChartData:ChartData[] = [];
            for (let i = 0; i < _d.length; i++) {
                let _chartData:ChartData = ChartDataFabric.chartDataFromJSON(this.getChartByName(_currentChartName).type, _d[i]);
                _allChartData.push(_chartData);
                // append this data for this data array
                if (1 < 0) {
                    // load data from localStorage, create one if no data exists
                    let _dataNow:any = this.getEntireChartData();
                    if (_dataNow == undefined) {
                        _dataNow = {};
                    }
                    // check if our localStorage contains the data for this chart
                    if (_dataNow[_currentChartName] == undefined) {
                        _dataNow[_currentChartName] = [];
                    }
                    // in case of line - we just push the value
                    _dataNow[_currentChartName].push(_chartData);

                    // save data back to localStorage
                    this.localStorageService.set(this.KEY_DATA, _dataNow);
                }
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
            this.chartSubjects[chart.name] = new Subject<ChartData[]>();
            chart.subscribeToSubject(this.chartSubjects[chart.name]);
            this.saveDashboard();
        }
    }

    modifyChart(chart:AbstractChart):void {
        if (!this.hasChartWithName(chart.name)) {
            throw new Error("Trying to modify chart that does not exist within the active dashboard");
        } else {
            for (let i = 0; i < this._dashboard.charts.length; i++) {
                if (this._dashboard.charts[i].name == chart.name) {
                    this._dashboard.charts[i] = chart;
                    break;
                }
            }
            this.saveDashboard();
        }
    }

    removeChart(chartName:string):void {
        for (let i = 0; i < this._dashboard.charts.length; i++ ) {
            if (this._dashboard.charts[i].name == chartName) {

                // remove the chart from the dashboard
                this._dashboard.charts.splice(i, 1);

                // nullify the corresppnding subject
                this.chartSubjects[chartName] = undefined;

                // remove localStorage data for this chart
                let _dataLC:any = this.localStorageService.get(this.KEY_DATA);
                if (_dataLC != undefined && _dataLC[chartName] != undefined) {
                    _dataLC[chartName] = undefined;
                    this.localStorageService.set(this.KEY_DATA, _dataLC);
                }

                // save the dashboard
                this.saveDashboard();
                return;
            }
        }
        throw new Error("Could not find a chart " + chartName);
    }

    getObservableForChart(name:string):Observable<ChartData[]> {
        if (this.chartSubjects[name] != undefined) {
            return this.chartSubjects[name].asObservable().share();
        } else {
            throw new Error("Cannot find any subject for chart " + name);
        }
    }

    getEntireChartData():{ [key:string]: ChartData[] } {
        let _object:any = this.localStorageService.get(this.KEY_DATA);
        let _value:{ [key:string]: ChartData[] } = {};
        if (_object != undefined) {
            for (let _element in _object) {
                let _newChartDataArray:ChartData[] = [];
                if (_object[_element] instanceof Array) {
                    for (let i = 0; i < _object[_element].length; i++) {
                        _newChartDataArray.push(ChartDataFabric.chartDataFromJSON(this.getChartByName(_object).type,_object[_element][i]));
                    }
                }
                _value[_element] = _newChartDataArray;
            }
        }
        return _value;
    }
}
