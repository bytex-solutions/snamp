import { SubEntity } from './model.subEntity';
import { ApiClient } from '../../services/app.restClient';
import * as moment from 'moment/moment'

export class Attribute extends SubEntity {
    public rwto:number = 0; // read/write timeout
    constructor(http:ApiClient, resourceType:string, name:string, rwto:any, jsonObject:any) {
        super(http, name, resourceType, jsonObject);
        // if we pass there number - we should recognize it as a number (ms)
        // otherwise - we parse it as a duration ISO8601
        this.rwto = (!isNaN(parseFloat(rwto)) && isFinite(rwto)) ? rwto :  moment.duration(rwto).milliseconds();
    }

    public stringifyFullObject():string {
        let resultValue:{ [key:string]:string; } = {};
        // see https://momentjs.com/docs/#/durations/as-json/
        resultValue["readWriteTimeout"] = moment.duration(this.rwto).toJSON();
        resultValue["parameters"] = this.stringifyParameters();
        return JSON.stringify(resultValue, null, 4);
    }
}
