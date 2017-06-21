import { SubEntity } from './model.subEntity';
import { ApiClient } from '../../services/app.restClient';
import * as moment from 'moment/moment'

export class Operation extends SubEntity {
    public invokto:number = 0; // invocation timeout
    constructor(http:ApiClient, resourceType:string, name:string, invokto:any, override?:boolean, jsonObject?:any) {
        super(http, name, resourceType, override, jsonObject);
        // if we pass there number - we should recognize it as a number (ms)
        // otherwise - we parse it as a duration ISO8601
        this.invokto = (!isNaN(parseFloat(invokto)) && isFinite(invokto)) ? invokto :  moment.duration(invokto).asMilliseconds();
    }

    public stringifyFullObject():string {
        let resultValue:{ [key:string]:any; } = {};
        resultValue["invocationTimeout"] =  moment.duration({ milliseconds: this.invokto}).toISOString();
        resultValue["override"] = this.override;
        resultValue["parameters"] = this.stringifyParameters();
        return JSON.stringify(resultValue, null, 4);
    }
}
