import { AbstractNotification } from "./abstract.notification";
import { HealthStatus } from "../healthstatus/health.status";
import { OkStatus } from "../healthstatus/ok.status";
import { StatusFactory } from "../healthstatus/factory";

export class HealthStatusNotification extends AbstractNotification {

    /**
     1) example:
        {"@messageType":"healthStatusChanged","previousStatus":{"@type":"InvalidAttributeValue","resourceName":"node#1","critical":true,"attributeName":"CPU","attributeValue":89.4655},"newStatus":{"@type":"OK"}}
     2) example:
        {"@messageType":"healthStatusChanged","previousStatus":{"@type":"OK"},"newStatus":{"@type":"InvalidAttributeValue","resourceName":"node#1","critical":true,"attributeName":"CPU","attributeValue":89.4655}}
     */

    private _prevStatus:HealthStatus;
    private _currentStatus:HealthStatus;
    private _groupName:string;
    private _mostProblematicResource:string;

    constructor() {
        super();
        this.prevStatus = new OkStatus;
        this.currentStatus = new OkStatus;
    }

    htmlDetails(): string {
        let _details:string = "<strong>Group name:</strong>" + this.groupName + "<br/>";
        if (this.mostProblematicResource != undefined) {
            _details += "<strong>Most problematic resource:</strong>" + this._mostProblematicResource + "<br/>";
        }
        _details += " <hr/>";
        _details += "<strong>The status before: </strong><br/>";
        _details += this.prevStatus.htmlDetails();
        _details += " <hr/>";
        _details += "<strong>Current status: </strong><br/>";
        _details += this.currentStatus.htmlDetails();
        return _details;
    }

    get prevStatus(): HealthStatus {
        return this._prevStatus;
    }

    set prevStatus(value: HealthStatus) {
        this._prevStatus = value;
    }

    get currentStatus(): HealthStatus {
        return this._currentStatus;
    }

    set currentStatus(value: HealthStatus) {
        this._currentStatus = value;
    }

    get groupName(): string {
        return this._groupName;
    }

    set groupName(value: string) {
        this._groupName = value;
    }

    get mostProblematicResource(): string {
        return this._mostProblematicResource;
    }

    set mostProblematicResource(value: string) {
        this._mostProblematicResource = value;
    }

    shortDescription(): string {
        return "Group: " + this.groupName +  ".Previous status: " + this.prevStatus.innerType + " , current status: " + this.currentStatus.innerType;
    }

    fillFromJson(_json: any): void {
        if (_json["previousStatus"] != undefined) {
            this.prevStatus = StatusFactory.healthStatusFromJSON(_json["previousStatus"]['@type'], _json["previousStatus"]);
        }
        if (_json["newStatus"] != undefined) {
            this.currentStatus = StatusFactory.healthStatusFromJSON(_json["newStatus"]['@type'], _json["newStatus"]);
        }
        if (_json["groupName"] != undefined) {
            this.groupName = _json["groupName"];
        }

        if (_json["mostProblematicResource"] != undefined) {
            this.mostProblematicResource = _json["mostProblematicResource"];
        }
        this.level = this.currentStatus.getNotificationLevel() == "ok" ? "info" : "warn";
    }
}