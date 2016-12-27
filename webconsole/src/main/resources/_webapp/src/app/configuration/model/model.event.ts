import { SubEntity } from './model.subEntity';
import { ApiClient } from '../../app.restClient';

export class Event extends SubEntity {
    constructor(http:ApiClient, resourceType:string, name:string, jsonObject:any) {
        super(http, name, resourceType, jsonObject);
    }
}
