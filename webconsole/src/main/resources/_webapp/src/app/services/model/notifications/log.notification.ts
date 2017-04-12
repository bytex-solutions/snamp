import { AbstractNotification } from "./abstract.notification";

export class LogNotification extends AbstractNotification {

    private stacktrace:string;
    private shortDetailsHtml:string;
    private details:any;
    private serverTimeStamp:string;

    constructor() {
        super();
        this.stacktrace = "No stacktrace is available";
        this.shortDetailsHtml = undefined;
        this.details = {};
        this.serverTimeStamp = undefined;
    }

    htmlDetails(): string {
        let _details:string = "";
        _details += "<strong>Message: </strong>" + this.message + "<br/>";
        _details += "<strong>Timestamp: </strong>" + this.timestamp + "<br/>";
        if (this.stacktrace != "No stacktrace is available") {
            _details += "<strong>Stacktrace: </strong>" + this.stacktrace + "<br/>";
        }
        _details += "<strong>Level: </strong>" + this.level + "<br/>";
        if (this.details && !$.isEmptyObject(this.details)) {
            _details += "<strong>Details</strong></br/>";
            _details += this.shortDetailsHtml;
        }
        return _details;
    }

    shortDescription(): string {
        return this.shortDetailsHtml;
    }

    fillFromJson(_json: any): void {
        if (_json["message"] != undefined) {
            this.message = _json["message"];
        }
        if (_json["timestamp"] != undefined) {
            this.serverTimeStamp = _json["timestamp"];
        }
        if (_json["level"] != undefined) {
            this.level = _json["level"];
        }
        if (_json["stacktrace"] != undefined) {
            this.stacktrace = _json["stacktrace"];
        }
        if (_json["details"] != undefined && !$.isEmptyObject(_json["details"])) {
            this.details = _json["details"];

            let _details:string = "";
            for (let key in _json.details) {
                _details += "<strong>" + key + ": </strong>" + _json.details[key] + "<br/>"
            }
            this.shortDetailsHtml = _details;
        }
    }

}