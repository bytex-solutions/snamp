import { Entity } from './entity';
import { ScriptletDataObject } from './scriptlet.data.object';
import * as moment from 'moment/moment';

export class Watcher extends Entity {

    public attributeCheckers:{ [key:string]:ScriptletDataObject; } = {};
    public trigger:ScriptletDataObject = new ScriptletDataObject({});
    public scalingPolicies:{ [key:string]:ScriptletDataObject; } = {};
    public connectionStringTemplate:string = "";
    public scalingSize:number = 0;
    public maxClusterSize:number = 10;
    public minClusterSize:number = 0;
    public cooldownTime:number = 0;
    public type:string = "default";
    public autoScaling:boolean = false;

    public toJSON():any {
        // console.log("JSONify the watcher from the watcher class: ", this);
         let _value:any = {};
        _value["attributeCheckers"] = {};
        for (let key in this.attributeCheckers) {
            _value["attributeCheckers"][key] = this.attributeCheckers[key].toJSON();
        }
        _value["scalingPolicies"] = {};
        for (let key in this.scalingPolicies) {
            _value["scalingPolicies"][key] = this.scalingPolicies[key].toJSON();
        }
        _value["trigger"] = this.trigger.toJSON();
        _value["parameters"] = this.stringifyParameters();
        _value["connectionStringTemplate"] = this.connectionStringTemplate;
        _value["cooldownTime"] = moment.duration({ milliseconds: this.cooldownTime}).toISOString();
        _value["autoScaling"] = this.autoScaling;
        _value["scalingSize"] = this.scalingSize;
        _value["minClusterSize"] = this.minClusterSize;
        _value["maxClusterSize"] = this.maxClusterSize;
        _value["type"] = this.type;
        return _value;
    }

    checkerExists(attributeName:string):boolean {
        return this.attributeCheckers[attributeName] != undefined && this.attributeCheckers[attributeName].script.length > 0 ;
    }

    checkerTypeForAttributeName(attributeName:string):string {
        return this.checkerExists(attributeName) ? this.attributeCheckers[attributeName].language : "n/a";
    }
}