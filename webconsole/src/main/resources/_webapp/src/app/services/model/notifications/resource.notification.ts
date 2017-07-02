import { AbstractNotification } from "./abstract.notification";

export class ResourceNotification extends AbstractNotification {

    private sequenceNumber:number;
    private source:string;
    private sourceObjectName:string;
    private serverTime:number;
    private userData:any;

    constructor() {
        super();
        this.sequenceNumber = 0;
        this.source = "n/a";
        this.sourceObjectName = "n/a";
        this.serverTime = new Date().getTime();
        this.userData = {};
    }

    /**
     * 1) example:
     *    "{"@messageType":"resourceNotification","notification":{"source":"paypal","type":"com.bytex.snamp.measurement.span","sequenceNumber":232,"timeStamp":1491912868821,"message":"Span detected","userData":{}}}"
     * fields (jmx type_
     *     new ObjectStreamField("message", String.class),
     *     new ObjectStreamField("sequenceNumber", Long.TYPE),
     *     new ObjectStreamField("source", Object.class),
     *     new ObjectStreamField("sourceObjectName", ObjectName.class),
     *     new ObjectStreamField("timeStamp", Long.TYPE),
     *     new ObjectStreamField("type", String.class),
     *     new ObjectStreamField("userData", Object.class)
     */
    htmlDetails(): string {
        let _details:string = "";
        _details += "<strong>Type: </strong>" + this.type + "<br/>";
        _details += "<strong>Message: </strong>" + this.message + "<br/>";
        _details += "<strong>Timestamp: </strong>" + this.timestamp + "<br/>";
        _details += "<strong>Server time: </strong>" + this.serverTime + "<br/>";
        _details += "<strong>Sequence number: </strong>" + this.sequenceNumber + "<br/>";
        if (this.sourceObjectName != "n/a") {
            _details += "<strong>Source object name: </strong>" + this.sourceObjectName + "<br/>";
        }
        if (this.source !+ "n/a") {
            _details += "<strong>Source: </strong>" + this.source + "<br/>";
        }
        if (!$.isEmptyObject(this.userData)) {
            _details += "<strong>User data: </strong>" + this.userData + "<br/>";
        }
        return _details;
    }

    shortDescription(): string {
        return this.message;
    }

    fillFromJson(_json: any): void {
        if (_json["message"] != undefined) {
            this.message = _json["message"];
        }
        if (_json["timestamp"] != undefined) {
            this.serverTime = _json["timestamp"];
        }
        if (_json["level"] != undefined) {
            this.level = _json["level"];
        }
        if (_json["source"] != undefined) {
            this.source = _json["source"];
        }
        if (_json["sourceObjectName"] != undefined) {
            this.sourceObjectName = _json["sourceObjectName"];
        }
        if (_json["type"] != undefined) {
            this.type = _json["type"];
        }
        if (_json["sequenceNumber"] != undefined) {
            this.sequenceNumber = _json["sequenceNumber"];
        }
        if (_json["userData"] != undefined) {
            this.userData = _json["userData"];
        }
    }

}