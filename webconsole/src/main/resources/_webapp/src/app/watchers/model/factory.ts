import { Watcher } from './watcher';
import { ScriptletDataObject } from './scriptlet.data.object';

import { HealthStatus } from './health.status';
import { OkStatus } from './ok.status';
import { ConnectionProblem } from './connection.problem.status';
import { ResourceIsNotAvailable } from './resource.na.status';
import { InvalidAttributeValue } from './invalid.attribute.value.status';
import { MalfunctionStatus } from './malfunction.status';

export class Factory {

    public static watcherFromJSON(name:string, json:any):Watcher {
        let _watcher:Watcher = new Watcher(name, json["parameters"]);
        if (json["attributeCheckers"] != undefined) {
            for (let key in json["attributeCheckers"]) {
                _watcher.attributeCheckers[key] = ScriptletDataObject.fromJSON(json["attributeCheckers"][key]);
            }
        }
        if (json["trigger"] != undefined) {
            _watcher.trigger = ScriptletDataObject.fromJSON(json["trigger"]);
        }
        return _watcher;
    }

    public static watchersArrayFromJSON(json:any):Watcher[] {
        let result:Watcher[] = [];
        for (let key in json) {
            result.push(Factory.watcherFromJSON(key, json[key]));
        }
        return result;
    }

    public static parseAllStatuses(json:any):HealthStatus[] {
        let _value:HealthStatus[] = [];
        for (let key in json) {
            _value.push(Factory.healthStatusFromJSON(key, json[key]));
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
        _value.resourceName = json["resourceName"];
        if (_value instanceof MalfunctionStatus) {
            (<MalfunctionStatus>_value).critical = (json["critical"] === "true");
        }
        return _value;
    }
}