import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { LocalStorageService } from 'angular-2-local-storage';
import { Subject } from 'rxjs/Subject';
import { AbstractNotification } from "./model/notifications/abstract.notification";
import { LogNotification } from "./model/notifications/log.notification";

@Injectable()
export class SnampLogService {
    private MAX_SIZE:number = 300;
    private SPLICE_COUNT:number = 30; // how many elements will we delete from the end of the array
    private RECENT_COUNT:number = 15; // default count of the recent message
    private KEY:string = "snampLogs";
    private logObs:Subject<AbstractNotification>;

    constructor(private localStorageService: LocalStorageService) {
          let welcomeMessage:AbstractNotification = new LogNotification();
          welcomeMessage.message = "SNAMP WEB UI has started successfully";
          this.logObs = new Subject<AbstractNotification>();
    }

    public getLogObs():Observable<AbstractNotification> {
        return this.logObs.asObservable().share();
    }

    private getArray():AbstractNotification[] {
         let logArray:AbstractNotification[] = <AbstractNotification[]>this.localStorageService.get(this.KEY);
         if (logArray == undefined || logArray.length == 0) {
              this.localStorageService.set(this.KEY, []);
              logArray = [];
         }
         if (logArray.length >= this.MAX_SIZE) {
               logArray.splice((-1) * this.SPLICE_COUNT, this.SPLICE_COUNT);
               this.localStorageService.set(this.KEY, logArray);
         }
         return logArray;
    }

    ngOnInit() {
        let welcomeMessage:AbstractNotification = new LogNotification();
        welcomeMessage.message = "SNAMP WEB UI has started successfully";
        this.pushLog(welcomeMessage);
    }

    public pushLog(log:AbstractNotification) {
        let logArray:AbstractNotification[] = this.getArray();
        logArray.unshift(log);
        this.localStorageService.set(this.KEY, logArray);
        this.logObs.next(log);
    }

    public getLastLogs(count?:number):AbstractNotification[] {
        let _count:number = count ? count : this.RECENT_COUNT;
        let logArray:AbstractNotification[] = this.getArray();
        if (logArray.length < _count) {
            return logArray;
        } else {
            return logArray.slice((-1) * _count);
        }
    }

    public getAllLogsJSON():any {
        return this.getArray().reverse();
    }

    public clear() {
        this.localStorageService.clearAll();
    }
}
