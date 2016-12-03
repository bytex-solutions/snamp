import { TypedEntity } from './model.typedEntity';
import { ApiClient } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Binding } from './model.binding';
import { ParamDescriptor } from './model.paramDescriptor';

export class Gateway extends TypedEntity {
    http:ApiClient;
    attributes:Binding[] = [];
    events:Binding[] = [];
    operations:Binding[] = [];
    parametersDescription:ParamDescriptor[] = [];
    constructor(http:ApiClient, name:string, type:string, parameters: { [key:string]:string; }) {
        super(name, type, parameters);
        this.http = http;
        if (name != "") {
            // retrieving attributes bindings
            this.http.get("/snamp/console/gateway/" + name + "/attributes/bindings")
                .map((res: Response) => res.json())
                .subscribe(response => {
                    for (let resourceKey in response) {
                        for (let attributeKey in response[resourceKey]) {
                            this.attributes.push(new Binding(attributeKey, resourceKey, response[resourceKey][attributeKey]));
                        }
                    }
                });

            // retrieving events bindings
            this.http.get("/snamp/console/gateway/" + name + "/events/bindings")
                .map((res: Response) => res.json())
                .subscribe(response => {
                    for (let resourceKey in response) {
                        for (let eventKey in response[resourceKey]) {
                            this.events.push(new Binding(eventKey, resourceKey, response[resourceKey][eventKey]));
                        }
                    }
                });

            // retrieving operations bindings
            this.http.get("/snamp/console/gateway/" + name + "/operations/bindings")
                .map((res: Response) => res.json())
                .subscribe(response => {
                    for (let resourceKey in response) {
                        for (let operationKey in response[resourceKey]) {
                            this.operations.push(new Binding(operationKey, resourceKey, response[resourceKey][operationKey]));
                        }
                    }
                });

            // retrieving parameters description
            this.http.get("/snamp/console/management/gateways/" + type + "/configuration")
                .map((res: Response) => res.json())
                .subscribe(response => {
                   for (let obj in response) {
                      this.parametersDescription.push(new ParamDescriptor(obj));
                   }
                });
        }
    }
}