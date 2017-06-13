import { KeyValue } from './model.entity';
import { ApiClient, REST } from '../../services/app.restClient';
import { ParamDescriptor } from './model.paramDescriptor';
import { Observable } from "rxjs/Observable";
import { EntityWithSub } from "./model.entityWithSub";

export class Resource extends EntityWithSub {
    http:ApiClient;
    connectionString:string = "";
    smartMode:boolean = false;
    groupName:string = "";
    threadPool:string = "";
    constructor(http:ApiClient, name:string, parameters: any) {
        super(http, name, parameters);
        this.http = http;

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
        if (this.contains("group")) {
            this.groupName = this.getParameter("group").value;
            this.removeParameter("group");
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
}
