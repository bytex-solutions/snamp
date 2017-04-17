import { HealthStatus } from "./health.status";
import { OkStatus } from "./ok.status";
import { ResourceIsNotAvailable } from "./resource.na.status";
import { ConnectionProblem } from "./connection.problem.status";
import { InvalidAttributeValue } from "./invalid.attribute.value.status";
import { MalfunctionStatus } from "./malfunction.status";

export class StatusFactory {

    public static parseAllStatuses(json:any):HealthStatus[] {
        let _value:HealthStatus[] = [];
        for (let key in json) {
            _value.push(StatusFactory.healthStatusFromJSON(key, json[key]));
        }
        return _value;
    }

    public static healthStatusFromJSON(name:string, json:any):HealthStatus {
        let _value:HealthStatus = undefined;
        switch(json["@type"]) {
            case HealthStatus.OK_TYPE:
                _value = new OkStatus();
                break;

            case HealthStatus.RESOURCE_NA_TYPE:
                _value = new ResourceIsNotAvailable();
                (<ResourceIsNotAvailable>_value).jmxError = json["error"];
                break;

            case HealthStatus.CONNECTION_PROBLEM_TYPE:
                _value = new ConnectionProblem();
                (<ConnectionProblem>_value).ioException = json["error"];
                break;

            case HealthStatus.ATTRIBUTE_VALUE_PROBLEM_TYPE:
                _value = new InvalidAttributeValue();
                (<InvalidAttributeValue>_value).attribute.name = json["attributeName"];
                (<InvalidAttributeValue>_value).attribute.value = json["attributeValue"];
                break;
            default:
                throw new Error("Cannot recognize type of health status: " + json["@type"]);
        }
        _value.name = name;
        _value.innerType = json["@type"];
        _value.resourceName = json["resourceName"];
        if (_value instanceof MalfunctionStatus) {
            (<MalfunctionStatus>_value).critical = json["critical"];
        }
        return _value;
    }

    public static healthStatusFromObject(json:any):HealthStatus {
        let _value:HealthStatus = undefined;
        switch(json["innerType"]) {
            case HealthStatus.OK_TYPE:
                _value = Object.assign(new OkStatus, json);
                break;
            case HealthStatus.RESOURCE_NA_TYPE:
                _value = Object.assign(new ResourceIsNotAvailable(), json);
                break;

            case HealthStatus.CONNECTION_PROBLEM_TYPE:
                _value = Object.assign(new ConnectionProblem(), json);
                break;

            case HealthStatus.ATTRIBUTE_VALUE_PROBLEM_TYPE:
                _value = Object.assign(new InvalidAttributeValue(), json);
                break;
            default:
                throw new Error("Cannot recognize type of health status: " + json["innerType"]);
        }
        return _value;
    }
}