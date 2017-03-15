import { Entity } from './entity';
import { ScriptletDataObject } from './scriptlet.data.object';

export class Watcher extends Entity {
    public attributeCheckers:{ [key:string]:ScriptletDataObject; } = {};
    public trigger:ScriptletDataObject = new ScriptletDataObject();

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

    checkerExists(attributeName:string):boolean {
        return this.attributeCheckers[attributeName] != undefined;
    }

    checkerTypeForAttributeName(attributeName:string):string {
        return this.checkerExists(attributeName) ? this.attributeCheckers[attributeName].language : "n/a";
    }
}