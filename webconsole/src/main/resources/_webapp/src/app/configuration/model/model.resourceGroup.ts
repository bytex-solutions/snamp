import { TypedEntity } from './model.typedEntity';
import { KeyValue } from './model.entity';
import { ApiClient } from '../../services/app.restClient';
import { Attribute } from './model.attribute';
import { ParamDescriptor } from './model.paramDescriptor';
import { Event } from './model.event';
import { Operation } from './model.operation';

export class ResourceGroup extends TypedEntity {
    http:ApiClient;
    smartMode:boolean = false;
    attributes:Attribute[] = [];
    events:Event[] = [];
    operations:Operation[] = [];
    constructor(http:ApiClient, name:string, parameters: any) {
        super(http, name, parameters["type"], parameters["parameters"]);
        this.http = http;

        // set the smart mode
        if (this.contains("smartMode")) {
            this.smartMode = this.getParameter("smartMode").value === "true";
            this.removeParameter("smartMode");
        }

        if (this.type && this.type != "") {

          // filling attributes
          if (parameters["attributes"] != undefined) {
              let attrs = parameters["attributes"];
              for (let key in attrs) {
                  let rwto:number = 0;
                  if  (attrs[key]["readWriteTimeout"] != undefined) {
                      rwto = attrs[key]["readWriteTimeout"];
                  }
                  this.attributes.push(new Attribute(http, this.type, key, rwto, attrs[key]["parameters"]));
              }
          }

          // filling events
          if (parameters["events"] != undefined) {
              let events = parameters["events"];
              for (let key in events) {
                  this.events.push(new Event(http, this.type, key, events[key]["parameters"]));
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
                  this.operations.push(new Operation(http, this.type, key, rwto, operations[key]["parameters"]));
              }
          }
        }
    }

    public static stringify(type:string, params: KeyValue[]):any {
        let returnValue:any = {};
        returnValue["type"] = type;
        returnValue["parameters"] = KeyValue.stringifyParametersStatic(params);
        return returnValue;
    }
}
