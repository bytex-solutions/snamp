import { Watcher } from './watcher';
import { ScriptletDataObject } from './scriptlet.data.object';

export class Factory {

    public static watcherFromJSON(name:string, json:any):Watcher {
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
        return _watcher;
    }

    public static watchersArrayFromJSON(json:any):Watcher[] {
        console.log("Whole watchers configuration is: ", json);
        let result:Watcher[] = [];
        for (let key in json) {
            result.push(Factory.watcherFromJSON(key, json[key]));
        }
        return result;
    }
}