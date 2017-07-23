import { KeyValue } from './model.entity';
import { ApiClient, REST } from '../../services/app.restClient';
import { ParamDescriptor } from './model.paramDescriptor';
import { Observable } from "rxjs/Observable";
import { EntityWithSub } from "./model.entityWithSub";
import {isNullOrUndefined} from "util";

export class Resource extends EntityWithSub {
    http:ApiClient;
    connectionString:string = "";
    smartMode:boolean = false;
    groupName:string = "";
    threadPool:string = "";
    overriddenProperties:string[] = [];
    constructor(http:ApiClient, name:string, parameters: any) {
        super(http, name, parameters);
        this.http = http;
        if (!isNullOrUndefined(parameters["overriddenProperties"]) && parameters["overriddenProperties"].length > 0) {
            this.overriddenProperties = parameters["overriddenProperties"];
        }

        // set right connection string
        this.connectionString = parameters["connectionString"];
        if (this.connectionString == undefined || this.connectionString.length < 4) {
            this.connectionString = ParamDescriptor.stubValue;
        }
        // set the smart mode
        if (this.contains("smartMode")) {
            this.smartMode = this.getParameter("smartMode").value === "true";
            this.removeParameter("smartMode");
        }
        // set the group
        if (parameters["groupName"] != undefined && parameters["groupName"].length > 0) {
            this.groupName = parameters["groupName"];
        }

        // set the group
        if (this.contains("threadPool")) {
            this.threadPool = this.getParameter("threadPool").value;
            this.removeParameter("threadPool");
        }
    }

    // used for receiving parameter descriptors
    public getDescriptionType():string {
        return "connector";
    }

    // used elsewhere
    public getName():string {
        return "resource";
    }

    public static toJSON(type:string, cstring:string, params: KeyValue[]):any {
        let returnValue:any = {};
        returnValue["type"] = type;
        returnValue["connectionString"] = cstring;
        returnValue["parameters"] = KeyValue.stringifyParametersStatic(params);
        return returnValue;
    }

    public discovery(type:string):Observable<any> {
        return this.http.getWithErrors(REST.RESOURCE_DISCOVERY(this.name, type));
    }

    public toggleOverridden(value:string):void {
        let index:number = this.overriddenProperties.indexOf(value);
        if (index >= 0) {
            this.overriddenProperties.splice(index, 1);
        } else {
            this.overriddenProperties.push(value);
        }
        console.debug("Overriddens for resource " + this.name + " are " + this.overriddenProperties);
    }
}
