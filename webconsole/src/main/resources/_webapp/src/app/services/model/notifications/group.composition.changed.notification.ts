import { AbstractNotification } from "./abstract.notification";

export class GroupCompositionChangedMessage extends AbstractNotification {

    private modifier:string;
    private resourceName:string;
    private groupName:string;

    constructor() {
        super();
        this.modifier = "unknown";
        this.resourceName = "n/a";
        this.groupName = "n/a";
    }

    htmlDetails(): string {
        let _details:string = "<strong>Group composition has been changed</strong><br/>";
        _details += "<strong>Timestamp: </strong>" + this.timestamp + "<br/>";
        _details += "<strong>Modifier: </strong>" + this.modifier + "<br/>";
        _details += "<strong>Resource name: </strong>" + this.resourceName + "<br/>";
        _details += "<strong>Group name: </strong>" + this.groupName + "<br/>";
        return _details;
    }

    shortDescription(): string {
        return "Resource " + this.resourceName + " has been " + this.modifier;
    }

    fillFromJson(_json: any): void {
        if (_json["modifier"] != undefined) {
            this.modifier = _json["modifier"];
        }
        if (_json["resourceName"] != undefined) {
            this.resourceName = _json["resourceName"];
        }
        if (_json["groupName"] != undefined) {
            this.groupName = _json["groupName"];
        }
    }
}