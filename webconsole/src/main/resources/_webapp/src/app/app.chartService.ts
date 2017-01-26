import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Response } from '@angular/http';
import { LocalStorageService } from 'angular-2-local-storage';
import { Subject } from 'rxjs/Subject';
import { ApiClient, REST } from './app.restClient';

import { Dashboard } from './charts/model/dashboard';
import { AbstractChart } from './charts/model/abstract.chart';
import { Factory } from './charts/model/objectFactory';
import { ChartData } from './charts/model/chart.data';

@Injectable()
export class ChartService {
    private MAX_SIZE:number = 10000;
    private SPLICE_COUNT:number = 30; // how many elements will we delete from the end of the array
    private RECENT_COUNT:number = 15; // default count of the recent message
    private KEY_DATA:string = "snampChartData";
    private _dashboard:Dashboard;
    private chartSubjects:{ [key:string]: Subject<ChartData> } = {};

    private loadDashboard():void {
        console.log("Loading some dashboard...");
        this._http.get(REST.CHART_DASHBOARD)
            .map((res:Response) => {
                console.log("Result of dashboard request is: ", res);
                return res.json();
            })
            .subscribe(data => {
                this._dashboard = new Dashboard();
                this.chartSubjects = {};
                let _chartData:{ [key:string]: ChartData[] } = this.getEntireChartData();
                if (data.charts.length > 0) {
                    for (let i = 0; i < data.charts.length; i++) {
                        let _currentChart:AbstractChart = Factory.chartFromJSON(data.charts[i]);
                        this.chartSubjects[_currentChart.name] = new Subject<ChartData>();
                        // append the existent chart data from LC to chart from the backend
                        if (_chartData != undefined && _chartData[_currentChart.name] != undefined) {
                            _currentChart.chartData = _chartData[_currentChart.name];
                        }
                        _currentChart.subscribeToSubject(this.chartSubjects[_currentChart.name]);
                        this._dashboard.charts.push(_currentChart);
                    }
                }
                console.log(this._dashboard);
            });
    }

    private saveDashboard():void {
        console.log("Saving some dashboard... ");
         this._http.put(REST.CHART_DASHBOARD, JSON.stringify(this._dashboard.toJSON()))
            .subscribe(data => {
                console.log("Dashboard has been saved successfully");
            });
    }

    constructor(private localStorageService: LocalStorageService, private _http:ApiClient) {
          this.loadDashboard();
          if (this.localStorageService.get(this.KEY_DATA) == undefined) {
               this.localStorageService.set(this.KEY_DATA, {});
          }
    }

    pushNewChartData(_data:any):void {
        console.log("New chart data is: ", _data);
        // load data from localStorage, create one if no data exists
        let _dataNow:any = this.localStorageService.get(this.KEY_DATA);
        if (_dataNow == undefined) {
            _dataNow = {};
        }
        // loop through all the data we have received
        for (var _currentChartName in _data) {
            // create a chart
            let _chartData:ChartData = ChartData.fromJSON(_data[_currentChartName]);

            // notify all the components that something has changed
            if (this.chartSubjects[_currentChartName] != undefined) {
                this.chartSubjects[_currentChartName].next(_chartData);
            }

            // check if our localStorage contains the data for this chart
            if (_dataNow[_currentChartName] == undefined) {
                _dataNow[_currentChartName] = [];
            }

            // append this data for this data array
            _dataNow[_currentChartName].push(_chartData);
        }

        // save data back to localStorage
        this.localStorageService.set(this.KEY_DATA, _dataNow);
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
            console.log("New created chart is: ", chart);
            this._dashboard.charts.push(chart);
            this.chartSubjects[chart.name] = new Subject<ChartData>();
            chart.subscribeToSubject(this.chartSubjects[chart.name]);
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

    getObservableForChart(name:string):Observable<ChartData> {
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
            for (var _element in _object) {
                let _newChartDataArray:ChartData[] = [];
                if (_object[_element] instanceof Array) {
                    for (let i = 0; i < _object[_element].length; i++) {
                        _newChartDataArray.push(ChartData.fromJSON(_object[_element][i]));
                    }
                }
                _value[_element] = _newChartDataArray;
            }
        }
        return _value;
    }
}
