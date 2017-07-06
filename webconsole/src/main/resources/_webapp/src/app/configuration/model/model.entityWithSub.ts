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
                let rwto:number = null;
                let override:boolean = false;
                if  (attrs[key]["readWriteTimeout"] != undefined) {
                    rwto = attrs[key]["readWriteTimeout"];
                }
                if  (attrs[key]["override"] != undefined) {
                    override = attrs[key]["override"];
                }
                this.attributes.push(new Attribute(http, this.type, key, rwto, override, attrs[key]["parameters"]));
            }
        }

        // filling events
        if (parameters["events"] != undefined) {
            let events = parameters["events"];
            for (let key in events) {
                let override:boolean = false;
                if  (events[key]["override"] != undefined) {
                    override = events[key]["override"];
                }
                this.events.push(new Event(http, this.type, key, override, events[key]["parameters"]));
            }
        }

        // filling operations
        if (parameters["operations"] != undefined) {
            let operations = parameters["operations"];
            for (let key in operations) {
                let rwto:number = null;
                let override:boolean = false;
                if  (operations[key]["invocationTimeout"] != undefined) {
                    rwto = operations[key]["invocationTimeout"];
                }
                if  (operations[key]["override"] != undefined) {
                    override = operations[key]["override"];
                }
                this.operations.push(new Operation(http, this.type, key, rwto, override, operations[key]["parameters"]));
            }
        }
    }
}