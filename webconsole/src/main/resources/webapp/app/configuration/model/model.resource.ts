import { TypedEntity } from './model.typedEntity';
import { KeyValue } from './model.entity';
import { ApiClient } from '../../app.restClient';
import { Attribute } from './model.attribute';
import { ParamDescriptor } from './model.paramDescriptor';
import { Event } from './model.event';
import { Operation } from './model.operation';

export class Resource extends TypedEntity {
    http:ApiClient;
    connectionString:string = "";
    smartMode:boolean = false;
    groupName:string = "";
    attributes:Attribute[] = [];
    events:Event[] = [];
    operations:Operation[] = [];
    constructor(http:ApiClient, name:string, parameters: any) {
        super(http, name, parameters["type"], parameters["parameters"]);
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

        // filling attributes
        if (parameters["attributes"] != undefined) {
            let attrs = parameters["attributes"];
            for (let key in attrs) {
                let rwto:number = 0;
                if  (attrs[key]["readWriteTimeout"] != undefined) {
                    rwto = attrs[key]["readWriteTimeout"];
                }
                this.attributes.push(new Attribute(key, rwto, attrs[key]["parameters"]));
            }
        }

        // filling events
        if (parameters["events"] != undefined) {
            let events = parameters["events"];
            for (let key in events) {
                this.events.push(new Event(key, events[key]["parameters"]));
            }
        }

        // filling operations
        if (parameters["operations"] != undefined) {
            let operations = parameters["operations"];
            for (let key in operations) {
                let rwto:number = 0;
                if  (operations[key]["invocationTimeout"] != undefined) {
                    rwto = operations[key]["invocationTimeout"];
                }
                this.operations.push(new Operation(key, rwto, operations[key]["parameters"]));
            }
        }
    }

    public static stringify(type:string, cstring:string, params: KeyValue[]):any {
        let returnValue:any = {};
        returnValue["type"] = type;
        returnValue["connectionString"] = cstring;
        returnValue["parameters"] = KeyValue.stringifyParametersStatic(params);
        return returnValue;
    }
}