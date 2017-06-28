import { Watcher } from './watcher';
import { ScriptletDataObject } from './scriptlet.data.object';
import * as moment from 'moment/moment';

export class Factory {

    public static watcherFromJSON(name:string, json:any):Watcher {
        console.log("Watcher: ", JSON.stringify(json));
        let _watcher:Watcher = new Watcher(name, json["parameters"]);
        if (json["attributeCheckers"] != undefined && !$.isEmptyObject(json["attributeCheckers"])) {
            for (let key in json["attributeCheckers"]) {
                if (json["attributeCheckers"][key]["language"] != undefined
                    && json["attributeCheckers"][key]["language"].length > 0) {
                    _watcher.attributeCheckers[key] = ScriptletDataObject.fromJSON(json["attributeCheckers"][key]);
                }
            }
        }
        if (json["trigger"] != undefined
            && !$.isEmptyObject(json["trigger"])
            && json["trigger"]["language"] != undefined
            && json["trigger"]["language"].length > 0) {
            _watcher.trigger = ScriptletDataObject.fromJSON(json["trigger"]);
        }

        if (json["connectionStringTemplate"] != undefined) {
            _watcher.connectionStringTemplate = json["connectionStringTemplate"];
        }
        if (json["scalingSize"] != undefined) {
            _watcher.scalingSize = json["scalingSize"];
        }
        if (json["maxClusterSize"] != undefined) {
            _watcher.maxClusterSize = json["maxClusterSize"];
        }
        if (json["minClusterSize"] != undefined) {
            _watcher.minClusterSize = json["minClusterSize"];
        }
        if (json["cooldownTime"] != undefined) {
            _watcher.cooldownTime =  (!isNaN(parseFloat(json["cooldownTime"])) && isFinite(json["cooldownTime"]))
                ? json["cooldownTime"] :  moment.duration(json["cooldownTime"]).asMilliseconds();
        }
        if (json["type"] != undefined) {
            _watcher.type = json["type"];
        }
        if (json["autoScaling"] != undefined) {
            _watcher.autoScaling = json["autoScaling"];
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
}