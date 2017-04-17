import { AbstractNotification } from "./abstract.notification";
import { LogNotification } from "./log.notification";
import { HealthStatusNotification } from "./health.status.notification";
import { ResourceNotification } from "./resource.notification";
import { StatusFactory } from "../healtstatus/factory";

export class NotificationFactory {
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

    public static makeFromInnerObject(_json:any):AbstractNotification {
        let _notification:AbstractNotification;
        switch (_json['_type']) {
            case "log":
                _notification = Object.assign(new LogNotification(), _json);
                break;
            case "healthStatusChanged":
                _notification = Object.assign(new HealthStatusNotification(), _json);
                (<HealthStatusNotification>_notification).prevStatus = StatusFactory.healthStatusFromObject(_json['_prevStatus']);
                (<HealthStatusNotification>_notification).currentStatus = StatusFactory.healthStatusFromObject(_json['_currentStatus']);
                break;
            case "resourceNotification":
                _notification = Object.assign(new ResourceNotification(), _json);
                break;
            default:
                throw new Error("Could not recognize notification of type: " + _json['_type']);
        }
        // restoring Date object from its string representation
        _notification.timestamp = new Date(_json['_timestamp']);
        // saving call results for usage by 3rd party's components
        _notification.savedMessage = _notification.shortDescription();
        _notification.savedDetails = _notification.htmlDetails();
        _notification.savedTimestamp = _notification.timestamp.getTime();
        return _notification;
    }
}