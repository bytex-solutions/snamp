import { Entity, Guid } from './model.entity';

export class Event extends Entity {
    public name:string;
    public guid:string;
    constructor(name:string, jsonObject:any) {
        super(jsonObject);
        this.name = name;
        this.guid = Guid.newGuid();
    }
}