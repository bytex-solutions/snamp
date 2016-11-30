import { TypedEntity } from './model.typedEntity';

export class Gateway extends TypedEntity {
    name:string;
    constructor(name:string, type:string, parameters: { [key:string]:string; }) {
        super(type, parameters);
        this.name = name;
    }
}