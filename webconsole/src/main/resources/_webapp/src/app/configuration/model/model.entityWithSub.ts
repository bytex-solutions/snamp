import { TypedEntity } from "./model.typedEntity";
import { Attribute } from "./model.attribute";
import { Event } from "./model.event";
import { Operation } from "./model.operation";
import { ApiClient } from "../../services/app.restClient";

export abstract class EntityWithSub extends TypedEntity {
    attributes:Attribute[] = [];
    events:Event[] = [];
    operations:Operation[] = [];

    constructor(http:ApiClient, name:string, parameters: any) {
        super(http, name, parameters["type"], parameters["parameters"]);

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