export abstract class Entity {
    protected parameters : KeyValue[] = [];
    constructor(parameters: { [key:string]:string; }) {
        for (let key in parameters) {
            this.parameters.push(new KeyValue(key, parameters[key]));
        }
    }

    public getParameter(key:string):KeyValue {
        this.parameters.forEach(function(obj:KeyValue) {
            if (obj.key == key) {
                return obj;
            }
        });
        return null;
    }

    public removeParameter(key:string) {
        for (let i = 0; i < this.parameters.length; i++) {
            if (this.parameters[i].key == key) {
                this.parameters.splice(i, 1);
                break;
            }
        }
    }

    public setParameter(parameter:KeyValue) {
        let found:boolean = false;
        this.parameters.forEach(function(obj:KeyValue) {
            if (obj.key === parameter.key) {
                obj.value = parameter.value;
                found = true;
                return;
            }
        });
        // if nothing is found - just push it into the array
        if (!found) {
            this.parameters.push(parameter);
        }
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