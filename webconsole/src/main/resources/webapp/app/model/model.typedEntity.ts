import { Entity } from './model.entity';

export class TypedEntity extends Entity {
    protected gtype:string;
    constructor(gtype:string, parameters: { [key:string]:string; }) {
        super(parameters);
        this.gtype = gtype;
    }
}