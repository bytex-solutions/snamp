export abstract class AbstractNotification {

    private _level:string;
    private _timestamp:Date;
    private _message:string;
    private _id:string;
    private _type:string;

    // meta fields for caching details (when there is no possibility to call the function)
    private _savedMessage:string;
    private _savedDetails:string;
    private _savedTimestamp:number;

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

    set type(value: string) {
        this._type = value;
    }

    constructor() {
        this._id = AbstractNotification.newGuid();
        this._message = "No message available";
        this._timestamp = new Date();
        this._level = "INFO";
    }

    get savedMessage(): string {
        return this._savedMessage;
    }

    set savedMessage(value: string) {
        this._savedMessage = value;
    }

    get savedDetails(): string {
        return this._savedDetails;
    }

    set savedDetails(value: string) {
        this._savedDetails = value;
    }

    get savedTimestamp(): number {
        return this._savedTimestamp;
    }

    set savedTimestamp(value: number) {
        this._savedTimestamp = value;
    }

    static newGuid() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        });
    }
}
