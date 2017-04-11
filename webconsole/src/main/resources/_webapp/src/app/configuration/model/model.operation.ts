import { SubEntity } from './model.subEntity';
import { ApiClient } from '../../services/app.restClient';

export class Operation extends SubEntity {
    public invokto:number = 0; // invocation timeout
    constructor(http:ApiClient, resourceType:string, name:string, invokto:number, jsonObject:any) {
        super(http, name, resourceType, jsonObject);
        this.invokto = invokto;
    }

    public stringifyFullObject():string {
        let resultValue:{ [key:string]:string; } = {};
        resultValue["invocationTimeout"] = String(this.invokto);
        resultValue["parameters"] = this.stringifyParameters();
        return JSON.stringify(resultValue, null, 4);
    }
}
