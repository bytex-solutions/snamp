export abstract class Entity {
    protected parameters : KeyValue[] = [];
    constructor(parameters: { [key:string]:string; }) {
        for (let key in parameters) {
            this.parameters.push(new KeyValue(key, parameters[key]));
        }
    }

    public getAttribute(key:string):KeyValue {
        this.parameters.forEach(function(obj:KeyValue) {
            if (obj.key == key) {
                return obj;
            }
        });
        return null;
    }

    public setAttribute(attribute:KeyValue) {
        this.parameters.forEach(function(obj:KeyValue) {
            if (obj.key == attribute.key) {
                obj.value = attribute.value;
            }
        });
    }
}

export class KeyValue {
    public key:string;
    public value:string;
    constructor(key:string, value:string){
        this.key = key;
        this.value = value;
    };
}