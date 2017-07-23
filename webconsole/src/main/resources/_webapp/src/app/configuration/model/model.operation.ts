import { SubEntity } from './model.subEntity';
import { ApiClient } from '../../services/app.restClient';
import { isNullOrUndefined } from "util";
import { SnampUtils } from "../../services/app.utils";

export class Operation extends SubEntity {
    public invokto:number = 0; // invocation timeout
    public isInfiniteDuration:boolean = true;
    constructor(http:ApiClient, resourceType:string, name:string, invokto:any, override?:boolean, jsonObject?:any) {
        super(http, name, resourceType, override, jsonObject);
        if (isNullOrUndefined(invokto)) {
            this.invokto = 0;
            this.isInfiniteDuration = true;
        } else {
            this.invokto = SnampUtils.parseDuration(invokto);
        }
    }

    public stringifyFullObject():string {
        let resultValue:{ [key:string]:any; } = {};
        if (!this.isInfiniteDuration) {
            resultValue["invocationTimeout"] = SnampUtils.toDurationString(this.invokto);
        }
        resultValue["override"] = this.override;
        resultValue["parameters"] = this.stringifyParameters();
        return JSON.stringify(resultValue, null, 4);
    }
}
