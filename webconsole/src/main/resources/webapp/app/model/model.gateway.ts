import { TypedEntity } from './model.typedEntity';
import { ApiClient } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Binding } from './model.binding';
import { OnInit } from '@angular/core';

export class Gateway extends TypedEntity implements OnInit {
    http:ApiClient;
    attributes:Binding[] = [];
    events:Binding[] = [];
    operations:Binding[] = [];
    constructor(http:ApiClient, name:string, type:string, parameters: { [key:string]:string; }) {
        super(http, name, type, parameters);
        this.http = http;
    }

    ngOnInit() {
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
    }
}