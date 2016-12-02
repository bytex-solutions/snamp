import { Entity } from './model.entity';

export class Binding extends Entity {
    public name:string;
    public resourceName:string;
    public mappedType:string;
    constructor(name:string, resourceName:string, jsonObject:any) {
        super(jsonObject["bindingProperties"]);
        this.name = name;
        this.resourceName = resourceName;
        this.mappedType = jsonObject["mappedType"];
    }
}