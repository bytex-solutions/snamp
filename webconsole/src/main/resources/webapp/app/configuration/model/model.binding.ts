import { Entity } from './model.entity';

export class Binding extends Entity {
    public resourceName:string;
    public mappedType:string;
    constructor(name:string, resourceName:string, jsonObject:any) {
        super(name, jsonObject["bindingProperties"]);
        this.resourceName = resourceName;
        this.mappedType = jsonObject["mappedType"];
    }
}