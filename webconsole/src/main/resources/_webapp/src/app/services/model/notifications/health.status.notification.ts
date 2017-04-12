import { AbstractNotification } from "./abstract.notification";
import { HealthStatus } from "../healtstatus/health.status";
import { OkStatus } from "../healtstatus/ok.status";
import { StatusFactory } from "../healtstatus/factory";

export class HealthStatusNotification extends AbstractNotification {

    /**
     1) example:
        {"@messageType":"healthStatusChanged","previousStatus":{"@type":"InvalidAttributeValue","resourceName":"node#1","critical":true,"attributeName":"CPU","attributeValue":89.4655},"newStatus":{"@type":"OK"}}
     2) example:
        {"@messageType":"healthStatusChanged","previousStatus":{"@type":"OK"},"newStatus":{"@type":"InvalidAttributeValue","resourceName":"node#1","critical":true,"attributeName":"CPU","attributeValue":89.4655}}
     */

    private prevStatus:HealthStatus;
    private currentStatus:HealthStatus;

    constructor() {
        super();
        this.prevStatus = new OkStatus;
        this.currentStatus = new OkStatus;
    }

    htmlDetails(): string {
        let _details:string =  "The status before: <br/>";
        _details += this.prevStatus.htmlDetails();
        _details += "Current status: <br/>";
        _details += this.currentStatus.htmlDetails();
        return _details;
    }

    shortDescription(): string {
        return "Previous status: " + this.prevStatus.innerType + " , current status: " + this.currentStatus.innerType;
    }

    fillFromJson(_json: any): void {
        if (_json["previousStatus"] != undefined) {
            this.prevStatus = StatusFactory.healthStatusFromJSON(_json["previousStatus"]['@type'], _json["previousStatus"]);
        }
        if (_json["newStatus"] != undefined) {
            this.currentStatus = StatusFactory.healthStatusFromJSON(_json["newStatus"]['@type'], _json["newStatus"]);
        }
        this.level = "WARN"; // always make it quite important (because no level is being received from backend)
    }
}