import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { LocalStorageService } from 'angular-2-local-storage';
import { Subject } from 'rxjs/Subject';

import { Dashboard } from './charts/model/dashboard';

@Injectable()
export class ChartService {
    private MAX_SIZE:number = 1000;
    private SPLICE_COUNT:number = 30; // how many elements will we delete from the end of the array
    private RECENT_COUNT:number = 15; // default count of the recent message
    private KEY:string = "snampCharts";
    private chartObs:Subject<Dashboard>;

    constructor(private localStorageService: LocalStorageService) {
          this.chartObs = new Subject<Dashboard>();
    }

    public getChartObs():Observable<Dashboard> {
        return this.chartObs.asObservable().share();
    }

    ngOnInit() {
    }

    pushNewChartData(_data:any):void {
        console.log("New chart data is: ", _data);
    }
}
