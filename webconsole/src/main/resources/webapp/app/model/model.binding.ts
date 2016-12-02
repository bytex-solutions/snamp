import { Entity } from './model.entity';

export class Binding extends Entity {
    public name:string;
    public resourceName:string;
    public mappedType:string;
    public guid:string;
    constructor(name:string, resourceName:string, jsonObject:any) {
        super(jsonObject["bindingProperties"]);
        this.name = name;
        this.resourceName = resourceName;
        this.mappedType = jsonObject["mappedType"];
        this.guid = Guid.newGuid();
    }
}

// http://stackoverflow.com/questions/26501688/a-typescript-guid-class
class Guid {
    static newGuid() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        });
    }
}