import { TypedEntity } from './model.typedEntity';
import { ApiClient, REST } from '../../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Binding } from './model.binding';
import { OnInit } from '@angular/core';

import 'rxjs/add/observable/throw';

export class Resource extends TypedEntity {
    http:ApiClient;

    constructor(http:ApiClient, name:string, type:string, parameters: { [key:string]:string; }) {
        super(http, name, type, parameters);
        this.http = http;
         if (name != "") {

         }
    }
}