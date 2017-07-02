import { KeyValue } from './model.entity';
import { ApiClient } from '../../services/app.restClient';
import { EntityWithSub } from "./model.entityWithSub";

export class ResourceGroup extends EntityWithSub {
    http:ApiClient;
    constructor(http:ApiClient, name:string, parameters: any) {
        super(http, name, parameters);
        this.http = http;
    }

    public getName():string {
        return "resourceGroup";
    }

    public getDescriptionType():string {
        return "connector";
    }

    public static stringify(type:string, params: KeyValue[]):any {
        let returnValue:any = {};
        returnValue["type"] = type;
        returnValue["parameters"] = KeyValue.stringifyParametersStatic(params);
        return returnValue;
    }
}
