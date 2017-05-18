import { AbstractNotification } from "./abstract.notification";

export class ScalingNotification extends AbstractNotification {

    private serverTime:number;
    private userData:any;
    private castingVoteWeight:number;
    private evaluationResult:any;
    private action:string;
    private groupName:string;

    constructor() {
        super();
        this.serverTime = new Date().getTime();
        this.userData = {};
        this.castingVoteWeight = -1;
        this.evaluationResult = {};
        this.action = undefined;
        this.groupName = undefined;
    }

    htmlDetails(): string {
        let _details:string = "";
        _details += "<strong>Type: </strong>" + this.type + "<br/>";
        _details += "<strong>Message: </strong>" + this.message + "<br/>";
        _details += "<strong>Timestamp: </strong>" + this.timestamp + "<br/>";
        _details += "<strong>Server time: </strong>" + this.serverTime + "<br/>";

        if (this.groupName != undefined && this.groupName.length > 0) {
            _details += "<strong>Group name: </strong>" + this.groupName + "<br/>";
        }
        if (!$.isEmptyObject(this.userData)) {
            _details += "<strong>User data: </strong>" + this.userData + "<br/>";
        }
        if (this.action != undefined && this.action.length > 0) {
            _details += "<strong>Action: </strong>" + this.action + "<br/>";
        }
        if (this.castingVoteWeight >= 0) {
            _details += "<strong>Casting vote weight: </strong>" + this.castingVoteWeight + "<br/>";
        }
        if (!$.isEmptyObject(this.evaluationResult)) {
            _details += "<strong>Evaluation result: </strong><br/>";
            for (let key in this.evaluationResult) {
                _details += "<strong>" + key +"</strong>" + this.evaluationResult[key] + "<br/>";
            }
        }
        return _details;
    }

    shortDescription(): string {
        return "Scaling action " + this.action + " happened for group " + this.groupName;
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
        if (_json["type"] != undefined) {
            this.type = _json["type"];
        }
        if (_json["userData"] != undefined) {
            this.userData = _json["userData"];
        }
        if (_json["action"] != undefined) {
            this.action = _json["action"];
        }
        if (_json["evaluationResult"] != undefined) {
            this.evaluationResult = _json["evaluationResult"];
        }
        if (_json["castingVoteWeight"] != undefined) {
            this.castingVoteWeight = _json["castingVoteWeight"];
        }
        if (_json["groupName"] != undefined) {
            this.groupName = _json["groupName"];
        }
    }

}