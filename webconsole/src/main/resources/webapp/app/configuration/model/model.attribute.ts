import { Entity, Guid } from './model.entity';

export class Attribute extends Entity {
    public name:string;
    public guid:string;
    public rwto:number = 0; // read/write timoute
    constructor(name:string, rwto:number, jsonObject:any) {
        super(jsonObject);
        this.name = name;
        this.rwto = rwto;
        this.guid = Guid.newGuid();
    }
}