import { Entity } from './model.entity';

export class Operation extends Entity {
    public invokto:number = 0; // invocation timeout
    constructor(name:string, invokto:number, jsonObject:any) {
        super(name, jsonObject);
        this.invokto = invokto;
    }
}