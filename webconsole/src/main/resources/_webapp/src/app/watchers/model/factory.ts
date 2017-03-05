import { Watcher } from './watcher';

public export class Fabric {

    public static watcherFromJSON(json:any):Watcher {
        let _watcher:Watcher = new Watcher();
        if (json["trigger"] != undefined) {

        }
        return _watcher;
    }
}