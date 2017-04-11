import { AbstractNotification } from "./abstract.notification";
import { LogNotification } from "./log.notification";
import { HealthStatusNotification } from "./health.status.notification";
import { ResourceNotification } from "./resource.notification";

export class Factory {
    public static makeFromJson(_json:any):AbstractNotification {
        let _notification:AbstractNotification;
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
        _notification.type = _json['@messageType'];
        return _notification;
    }
}