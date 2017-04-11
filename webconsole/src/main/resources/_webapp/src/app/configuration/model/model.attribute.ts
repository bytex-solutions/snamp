import { SubEntity } from './model.subEntity';
import { ApiClient } from '../../services/app.restClient';

export class Attribute extends SubEntity {
    public rwto:number = 0; // read/write timeout
    constructor(http:ApiClient, resourceType:string, name:string, rwto:number, jsonObject:any) {
        super(http, name, resourceType, jsonObject);
        this.rwto = rwto;
    }

    public stringifyFullObject():string {
        let resultValue:{ [key:string]:string; } = {};
        resultValue["readWriteTimeout"] = String(this.rwto);
        resultValue["parameters"] = this.stringifyParameters();
        return JSON.stringify(resultValue, null, 4);
    }
}
