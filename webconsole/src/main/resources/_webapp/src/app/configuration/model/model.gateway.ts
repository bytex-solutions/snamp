import { TypedEntity } from './model.typedEntity';
import { ApiClient, REST } from '../../services/app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Binding } from './model.binding';

import 'rxjs/add/observable/throw';

export class Gateway extends TypedEntity {
    http:ApiClient;
    attributes:Binding[] = [];
    events:Binding[] = [];
    operations:Binding[] = [];
    constructor(http:ApiClient, name:string, type:string, parameters: { [key:string]:string; }) {
        super(http, name, type, parameters);
        this.http = http;
         if (name != "") {
            // retrieving attributes bindings
            this.http.get(REST.BINDINGS(name, "attributes"))
                .map((res: Response) => res.json())
                .subscribe(response => {
                    for (let resourceKey in response) {
                        for (let attributeKey in response[resourceKey]) {
                            this.attributes.push(new Binding(attributeKey, resourceKey, response[resourceKey][attributeKey]));
                        }
                    }
                }, err => {if (err.status != 404) Observable.throw(err)});

            // retrieving events bindings
            this.http.get(REST.BINDINGS(name, "events"))
                .map((res: Response) => res.json())
                .subscribe(response => {
                    for (let resourceKey in response) {
                        for (let eventKey in response[resourceKey]) {
                            this.events.push(new Binding(eventKey, resourceKey, response[resourceKey][eventKey]));
                        }
                    }
                }, err => {if (err.status != 404) Observable.throw(err)});

            // retrieving operations bindings
            this.http.get(REST.BINDINGS(name, "operations"))
                .map((res: Response) => res.json())
                .subscribe(response => {
                    for (let resourceKey in response) {
                        for (let operationKey in response[resourceKey]) {
                            this.operations.push(new Binding(operationKey, resourceKey, response[resourceKey][operationKey]));
                        }
                    }
                }, err => {if (err.status != 404) Observable.throw(err)});
         }
    }
}