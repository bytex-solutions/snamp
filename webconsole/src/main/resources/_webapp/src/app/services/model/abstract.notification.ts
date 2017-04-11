import { LogNotification } from "./log.notification";
import { HealthStatusNotification } from "./health.status.notification";
import { ResourceNotification } from "./resource.notification";

export abstract class AbstractNotification {

    private _level:string;
    private _timestamp:Date;
    private _message:string;
    private _id:string;
    private _type:string;

    abstract htmlDetails():string;
    abstract shortDescription():string;
    abstract fillFromJson(json: any):void;


    get level(): string {
        return this._level;
    }

    get timestamp(): Date {
        return this._timestamp;
    }

    get message(): string {
        return this._message;
    }

    get id(): string {
        return this._id;
    }

    get type(): string {
        return this._type;
    }

    set message(value: string) {
        this._message = value;
    }

    set level(value: string) {
        this._level = value;
    }

    constructor() {
        this._id = AbstractNotification.newGuid();
        this._message = "No message available";
        this._timestamp = new Date();
        this._level = "INFO";
    }

    public static fillFromJson(_json:any):AbstractNotification {
        let _notification:AbstractNotification = undefined;

        switch (_json['@messageType']) {
            case "log":
                _notification = new LogNotification();
                break;
            case "healthStatusChanged":
                _notification = new HealthStatusNotification();
                break;
            case "resourceNotification":
                _notification = new ResourceNotification();
                break;
            default:
                throw new Error("Could not recognize notification of type: " + _json['@messageType']);
        }
        _notification.fillFromJson(_json);
        return _notification;
    }

    static newGuid() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        });
    }
}
