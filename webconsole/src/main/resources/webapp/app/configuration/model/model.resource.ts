import { TypedEntity } from './model.typedEntity';
import { ApiClient, REST } from '../../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Binding } from './model.binding';
import { OnInit } from '@angular/core';

import { Attribute } from './model.attribute';
import { Event } from './model.event';
import { Operation } from './model.operation';

import 'rxjs/add/observable/throw';

export class Resource extends TypedEntity {
    http:ApiClient;
    attributes:Attribute[] = [];
    events:Event[] = [];
    operations:Operation[] = [];
    constructor(http:ApiClient, name:string, type:string, parameters: { [key:string]:string; }) {
        super(http, name, type, parameters);
        this.http = http;
    }
}