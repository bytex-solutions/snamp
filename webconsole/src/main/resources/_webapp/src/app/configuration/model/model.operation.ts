import { SubEntity } from './model.subEntity';
import { ApiClient } from '../../app.restClient';

export class Operation extends SubEntity {
    public invokto:number = 0; // invocation timeout
    constructor(http:ApiClient, resourceType:string, name:string, invokto:number, jsonObject:any) {
        super(http, name, resourceType, jsonObject);
        this.invokto = invokto;
    }
}
