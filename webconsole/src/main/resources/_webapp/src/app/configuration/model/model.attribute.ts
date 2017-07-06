import { SubEntity } from './model.subEntity';
import { ApiClient } from '../../services/app.restClient';
import { SnampUtils } from "../../services/app.utils";
import { isNullOrUndefined } from "util";

export class Attribute extends SubEntity {
    public rwto:number = 0; // read/write timeout
    public isInfiniteDuration:boolean = true;
    constructor(http:ApiClient, resourceType:string, name:string, rwto:any, override?:boolean, jsonObject?:any) {
        super(http, name, resourceType, override, jsonObject);
        if (isNullOrUndefined(rwto)) {
            this.rwto = 0;
            this.isInfiniteDuration = true;
        } else {
            this.rwto = SnampUtils.parseDuration(rwto);
        }
    }

    public stringifyFullObject():string {
        let resultValue:{ [key:string]:any; } = {};
        if (!this.isInfiniteDuration) {
            resultValue["readWriteTimeout"] = SnampUtils.toDurationString(this.rwto);
        }
        resultValue["override"] = this.override;
        resultValue["parameters"] = this.stringifyParameters();
        return JSON.stringify(resultValue, null, 4);
    }
}
