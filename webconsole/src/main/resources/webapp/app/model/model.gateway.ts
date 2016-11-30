import { TypedEntity } from './model.typedEntity';

export class Gateway extends TypedEntity {
    name:string;
    constructor(name:string, gtype:string, parameters: { [key:string]:string; }) {
        super(gtype, parameters);
        this.name = name;
    }
}