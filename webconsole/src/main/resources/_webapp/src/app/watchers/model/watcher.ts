import { Entity } from './entity';
import { ScriptletDataObject } from './scriptlet.data.object';

export class Watcher extends Entity {
    public attributeCheckers:{ [key:string]:ScriptletDataObject; } = {};
    public trigger:ScriptletDataObject = undefined;

    public toJSON():any {
         let _value:any = {};
        _value["attributeCheckers"] = {};
        for (let key in this.attributeCheckers) {
            _value["attributeCheckers"][key] = this.attributeCheckers[key].toJSON();
        }
        _value["trigger"] = this.trigger.toJSON();
        _value["parameters"] = this.stringifyParameters();
        return _value;
    }
}