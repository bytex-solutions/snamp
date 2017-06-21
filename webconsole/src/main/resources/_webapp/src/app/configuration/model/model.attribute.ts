import { SubEntity } from './model.subEntity';
import { ApiClient } from '../../services/app.restClient';
import * as moment from 'moment/moment'

export class Attribute extends SubEntity {
    public rwto:number = 0; // read/write timeout
    constructor(http:ApiClient, resourceType:string, name:string, rwto:any, override?:boolean, jsonObject?:any) {
        super(http, name, resourceType, override, jsonObject);
        // if we pass there number - we should recognize it as a number (ms)
        // otherwise - we parse it as a duration ISO8601
        this.rwto = (!isNaN(parseFloat(rwto)) && isFinite(rwto)) ? rwto :  moment.duration(rwto).asMilliseconds();
        //console.log("Rwto for attribute " + name + ": " + rwto + " and " + this.rwto,  moment.duration(rwto));
    }

    public stringifyFullObject():string {
        let resultValue:{ [key:string]:any; } = {};
        // see https://momentjs.com/docs/#/durations/as-json/
        //console.log("Here! ", this.rwto, moment.duration({ milliseconds: this.rwto}));
        resultValue["readWriteTimeout"] = moment.duration({ milliseconds: this.rwto}).toISOString();
        resultValue["override"] = this.override;
        resultValue["parameters"] = this.stringifyParameters();
        return JSON.stringify(resultValue, null, 4);
    }
}
