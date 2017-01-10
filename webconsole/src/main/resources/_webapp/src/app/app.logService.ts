import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { LocalStorageService } from 'angular-2-local-storage';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';

@Injectable()
export class SnampLogService {
    private MAX_SIZE:number = 300;
    private SPLICE_COUNT:number = 30; // how many elements will we delete from the end of the array
    private RECENT_COUNT:number = 15; // default count of the recent message
    private KEY:string = "snampLogs";
    private logObs:BehaviorSubject<SnampLog>;

    constructor(private localStorageService: LocalStorageService) {
          let welcomeMessage:SnampLog = new SnampLog();
          welcomeMessage.message = "SNAMP WEB UI has started successfully";
          this.logObs = new BehaviorSubject<SnampLog>(welcomeMessage);
    }

    public getLogObs():BehaviorSubject<SnampLog> {
        return this.logObs;
    }

    private getArray():SnampLog[] {
         let logArray:SnampLog[] = <SnampLog[]>this.localStorageService.get(this.KEY);
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
        let welcomeMessage:SnampLog = new SnampLog();
        welcomeMessage.message = "SNAMP WEB UI has started successfully";
        this.pushLog(welcomeMessage);
    }

    public pushLog(log:SnampLog) {
        let logArray:SnampLog[] = this.getArray();
        logArray.push(log);
        this.localStorageService.set(this.KEY, logArray);
        console.log("Pushing: ", log);
        this.logObs.next(log);
    }

    public getLastLogs(count?:number):SnampLog[] {
        let _count:number = count ? count : this.RECENT_COUNT;
        let logArray:SnampLog[] = this.getArray();
        if (logArray.length < _count) {
            return logArray;
        } else {
            return logArray.slice((-1) * _count);
        }
    }

    public clear() {
        this.localStorageService.clearAll();
    }
}

export class SnampLog {
  public message:string = "No message available";
  public timestamp:string = (new Date()).toString();
  public localTime:Date = new Date();
  public level:string = "INFO";
  public details:any = {};
  public stacktrace:string = "No stacktrace is available";
  public id:string = SnampLog.newGuid();

  public static makeFromJson(_json:any):SnampLog {
     let _instance:SnampLog = new SnampLog();
     if (_json["message"] != undefined) {
        _instance.message = _json["message"];
     }
     if (_json["timestamp"] != undefined) {
        _instance.timestamp = _json["timestamp"];
     }
     if (_json["level"] != undefined) {
        _instance.level = _json["level"];
     }
     if (_json["stacktrace"] != undefined) {
        _instance.stacktrace = _json["stacktrace"];
     }
     if (_json["details"] != undefined) {
        _instance.details = _json["details"];
     }
     return _instance;
  }

  public htmlDetails():string {
    let _details:string = "";
     _details += "<strong>Message: </strong>" + this.message + "<br/>";
     _details += "<strong>Timestamp: </strong>" + this.timestamp + "<br/>";
     if (this.stacktrace != "No stacktrace is available") {
        _details += "<strong>Stacktrace: </strong>" + this.stacktrace + "<br/>";
     }
     _details += "<strong>Level: </strong>" + this.level + "<br/>";
     if (this.details) {
        _details += "<strong>Details</strong></br/>";
        for (let key in this.details) {
           _details += "<strong>" + key + ": </strong>" + this.details[key] + "<br/>"
        }
     }
     return _details;
  }

   static newGuid() {
      return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
          var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
          return v.toString(16);
      });
   }
}
