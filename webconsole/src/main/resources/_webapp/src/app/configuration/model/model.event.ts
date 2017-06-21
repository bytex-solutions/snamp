import { SubEntity } from './model.subEntity';
import { ApiClient } from '../../services/app.restClient';

export class Event extends SubEntity {
    constructor(http:ApiClient, resourceType:string, name:string, override?:boolean, jsonObject?:any) {
        super(http, name, resourceType, override, jsonObject);
    }

    public stringifyFullObject():string {
        let resultValue:{ [key:string]:string; } = {};
        resultValue["parameters"] = this.stringifyParameters();
        resultValue["override"] = this.override.toString();
        return JSON.stringify(resultValue, null, 4);
    }
}
