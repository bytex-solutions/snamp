import { Injectable, HostListener } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { LocalStorageService } from 'angular-2-local-storage';
import { Subject } from 'rxjs/Subject';
import { AbstractNotification } from "./model/notifications/abstract.notification";
import { LogNotification } from "./model/notifications/log.notification";
import { NotificationFactory } from "./model/notifications/factory";

@Injectable()
export class SnampLogService {
    // Flush the buffer if the user is closing browser
    @HostListener('window:beforeunload', ['$event'])
    beforeunloadHandler(event) {
       this.flushBuffer();
    }

    private MAX_SIZE:number = 500;
    private SPLICE_COUNT:number = 30; // how many elements will we delete from the end of the array
    private RECENT_COUNT:number = 15; // default count of the recent message
    private buffer:AbstractNotification[] = []; // buffer to write logs on before setting it back to the storage
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

    public flushBuffer():void {
        if (this.buffer.length > 0) {
            this.localStorageService.set(this.KEY, this.buffer.concat(this.buffer, this.getArray()));
            this.buffer = [];
        }
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
         // we should make real js object from its json representation, because local storage contains serialized data
         let _retArray:AbstractNotification[] = [];
         for (let i = 0; i < logArray.length; i++) {
             _retArray.push(NotificationFactory.makeFromInnerObject(logArray[i]));
         }
         return _retArray;
    }

    ngOnInit() {
        let welcomeMessage:AbstractNotification = new LogNotification();
        welcomeMessage.message = "SNAMP WEB UI has started successfully";
        this.pushLog(welcomeMessage);
    }

    public pushLog(log:AbstractNotification) {
        this.buffer.unshift(log);
        if (this.buffer.length > this.SPLICE_COUNT) {
          this.flushBuffer();
        }
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

    public getAllLogs():AbstractNotification[] {
        return this.getArray();
    }

    public getAllLogsJSON():any {
        return this.getArray().reverse();
    }

    public clear() {
        this.localStorageService.clearAll();
    }
}
