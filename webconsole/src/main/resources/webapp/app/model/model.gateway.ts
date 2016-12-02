import { TypedEntity } from './model.typedEntity';
import { ApiClient } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Binding } from './model.binding';

export class Gateway extends TypedEntity {
    http:ApiClient;
    attributes:Binding[] = [];
    constructor(http:ApiClient, name:string, type:string, parameters: { [key:string]:string; }) {
        super(name, type, parameters);
        this.http = http;
        if (name != "") {
            this.http.get("/snamp/console/gateway/" + name + "/attributes/bindings")
                .map((res: Response) => res.json())
                .subscribe(response => {
                    for (let resourceKey in response) {
                        for (let attributeKey in response[resourceKey]) {
                            this.attributes.push(new Binding(attributeKey, resourceKey, response[resourceKey][attributeKey]));
                        }
                    }
                });
        }
    }
}