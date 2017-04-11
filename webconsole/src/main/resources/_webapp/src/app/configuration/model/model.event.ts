import { SubEntity } from './model.subEntity';
import { ApiClient } from '../../services/app.restClient';

export class Event extends SubEntity {
    constructor(http:ApiClient, resourceType:string, name:string, jsonObject:any) {
        super(http, name, resourceType, jsonObject);
    }

    public stringifyFullObject():string {
        let resultValue:{ [key:string]:string; } = {};
        resultValue["parameters"] = this.stringifyParameters();
        return JSON.stringify(resultValue, null, 4);
    }
}
