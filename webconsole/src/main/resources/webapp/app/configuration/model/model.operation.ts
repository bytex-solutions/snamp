import { Entity, Guid } from './model.entity';

export class Operation extends Entity {
    public name:string;
    public guid:string;
    public invokto:number = 0; // invocation timeout
    constructor(name:string, invokto:number, jsonObject:any) {
        super(jsonObject);
        this.name = name;
        this.invokto = invokto;
        this.guid = Guid.newGuid();
    }
}