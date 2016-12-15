import { Entity } from './model.entity';

export class Event extends Entity {
    constructor(name:string, jsonObject:any) {
        super(name, jsonObject);
    }
}