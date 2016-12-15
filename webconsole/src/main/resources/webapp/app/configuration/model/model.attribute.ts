import { Entity } from './model.entity';

export class Attribute extends Entity {
    public rwto:number = 0; // read/write timeout
    constructor(name:string, rwto:number, jsonObject:any) {
        super(name, jsonObject);
        this.rwto = rwto;
    }
}