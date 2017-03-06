import { Watcher } from './watcher';
import { ScriptletDataObject } from './scriptlet.data.object';

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
}