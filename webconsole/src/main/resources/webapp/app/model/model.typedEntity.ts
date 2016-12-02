import { Entity } from './model.entity';

export class TypedEntity extends Entity {
    public type:string;
    public name:string
    constructor(name:string, type:string, parameters: { [key:string]:string; }) {
        super(parameters);
        this.type = type;
        this.name = name;
    }
}