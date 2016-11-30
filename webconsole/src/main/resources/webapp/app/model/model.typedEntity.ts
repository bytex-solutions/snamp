import { Entity } from './model.entity';

export class TypedEntity extends Entity {
    protected type:string;
    constructor(type:string, parameters: { [key:string]:string; }) {
        super(parameters);
        this.type = type;
    }
}