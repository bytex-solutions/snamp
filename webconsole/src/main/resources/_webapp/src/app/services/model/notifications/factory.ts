import { AbstractNotification } from "./abstract.notification";
import { LogNotification } from "./log.notification";
import { HealthStatusNotification } from "./health.status.notification";
import { ResourceNotification } from "./resource.notification";
import { StatusFactory } from "../healthstatus/factory";
import { GroupCompositionChangedMessage } from "./group.composition.changed.notification";
import { ScalingNotification } from "./scaling.happened";

export class NotificationFactory {
    public static makeFromJson(_json:any):AbstractNotification {
        let _notification:AbstractNotification;
        switch (_json['@messageType']) {
            case AbstractNotification.LOG:
                _notification = new LogNotification();
                break;
            case AbstractNotification.HEALTH_STATUS:
                _notification = new HealthStatusNotification();
                break;
            case AbstractNotification.RESOURCE:
                _notification = new ResourceNotification();
                break;
            case AbstractNotification.COMPOSITION:
                _notification = new GroupCompositionChangedMessage();
                break;
            case AbstractNotification.SCALING:
                _notification = new ScalingNotification();
                break;
            default:
                console.debug("Whole the json object is: ", _json);
                throw new Error("Could not recognize notification of type: " + _json['@messageType']);
        }
        _notification.fillFromJson(_json);
        _notification.type = _json['@messageType'];
        return _notification;
    }

    public static makeFromInnerObject(_json:any):AbstractNotification {
        let _notification:AbstractNotification;
        switch (_json['_type']) {
            case AbstractNotification.LOG:
                _notification = Object.assign(new LogNotification(), _json);
                break;
            case AbstractNotification.HEALTH_STATUS:
                _notification = Object.assign(new HealthStatusNotification(), _json);
                (<HealthStatusNotification>_notification).prevStatus = StatusFactory.healthStatusFromObject(_json['_prevStatus']);
                (<HealthStatusNotification>_notification).currentStatus = StatusFactory.healthStatusFromObject(_json['_currentStatus']);
                _notification.level = (<HealthStatusNotification>_notification).currentStatus.getNotificationLevel() == "ok" ? "info" : "warn";
                break;
            case AbstractNotification.RESOURCE:
                _notification = Object.assign(new ResourceNotification(), _json);
                break;
            case AbstractNotification.COMPOSITION:
                _notification = Object.assign(new GroupCompositionChangedMessage(), _json);
                break;
            case AbstractNotification.SCALING:
                _notification = Object.assign(new ScalingNotification(), _json);
                break;
            default:
                console.debug("Whole the json object is: ", _json);
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