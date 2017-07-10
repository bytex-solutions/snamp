import { Injectable, HostListener } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { LocalStorageService } from 'angular-2-local-storage';
import { Subject } from 'rxjs/Subject';
import { AbstractNotification } from "./model/notifications/abstract.notification";
import { LogNotification } from "./model/notifications/log.notification";
import { NotificationFactory } from "./model/notifications/factory";
import { isNullOrUndefined } from "util";
import { $WebSocket } from 'angular2-websocket/angular2-websocket';

@Injectable()
export class SnampLogService {
    // Flush the buffer if the user is closing browser
    @HostListener('window:beforeunload', ['$event'])
    beforeunloadHandler(event) {
       this.flushBuffer();
    }

    private ws: $WebSocket;
    private MAX_SIZE:number = 500;
    private SPLICE_COUNT:number = 30; // how many elements will we delete from the end of the array
    private RECENT_COUNT:number = 15; // default count of the recent message
    private buffer:AbstractNotification[] = []; // buffer to write logs on before setting it back to the storage
    private KEY:string = "snampLogs";
    private logObs:Subject<AbstractNotification>;
    private _displayAlerts:boolean;
    private keyToggleAlerts:string = "snampToggleAlerts";

    constructor(private localStorageService: LocalStorageService) {
          let welcomeMessage:AbstractNotification = new LogNotification();
          welcomeMessage.message = "SNAMP WEB UI has started successfully";
          this.logObs = new Subject<AbstractNotification>();
          let _tmp:any = this.localStorageService.get(this.keyToggleAlerts);
          if (isNullOrUndefined(_tmp)) {
              this.displayAlerts = true;
          } else {
              this.displayAlerts = (_tmp == 'true');
          }

        this.ws = new $WebSocket(SnampLogService.getWsAddress(), [],
            {initialTimeout: 500, maxTimeout: 300000, reconnectIfNotNormalClose: true});

        this.ws.getDataStream()
            .map((msg) => JSON.parse(msg.data))
            .subscribe(
                (msg)=> this.pushLog(NotificationFactory.makeFromJson(msg)),
                (msg)=> console.debug("Error occurred while listening to the socket: ", msg),
                ()=> console.debug("Socket connection has been completed")
            );
    }

    private static getWsAddress():string {
        let loc = window.location, new_uri;
        if (loc.protocol === "https:") {
            new_uri = "wss:";
        } else {
            new_uri = "ws:";
        }
        new_uri += "//" + loc.host;
        new_uri += loc.pathname + "console/events";
        return new_uri;
    }

    get displayAlerts(): boolean {
        return this._displayAlerts;
    }

    set displayAlerts(value: boolean) {
        this._displayAlerts = value;
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
             if (!isNullOrUndefined(logArray[i]["_type"]) && logArray[i]["_type"] == AbstractNotification.REST) {
             } else {
                 _retArray.push(NotificationFactory.makeFromInnerObject(logArray[i]));
             }
         }
         return _retArray;
    }

    ngOnInit():void {
        let welcomeMessage:AbstractNotification = new LogNotification();
        welcomeMessage.message = "SNAMP WEB UI has started successfully";
        this.pushLog(welcomeMessage);
    }

    public pushLog(log:AbstractNotification):void {
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

    public clear():void {
        this.localStorageService.clearAll();
    }

    public toggleDisplayAlerts():void {
        this.displayAlerts = !this.displayAlerts;
        this.localStorageService.set(this.keyToggleAlerts, this.displayAlerts);
    }
}
