export abstract class Entity {
    public name:string;
    public guid:string;
    public parameters : KeyValue[] = [];
    constructor(name:string, parameters: { [key:string]:string; }) {
        this.name = name;
        this.guid = Guid.newGuid();
        for (let key in parameters) {
            this.parameters.push(new KeyValue(key, parameters[key]));
        }
    }

    public getParameter(key:string):KeyValue {
       return KeyValue.getParameterByName(this.parameters, key);
    }

    public contains(key:string):boolean {
        return this.getParameter(key) != undefined;
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

    public clearParameters() {
        this.parameters = [];
    }

    private decapitalizeFirstLetter(object:string):string {
        return object.charAt(0).toLowerCase() + object.slice(1);
    }

    // see https://www.stevefenton.co.uk/2013/04/obtaining-a-class-name-at-runtime-in-typescript/
    getName() {
        var funcNameRegex = /function (.{1,})\(/;
        var results = (funcNameRegex).exec((<any> this).constructor.toString());
        return (results && results.length > 1) ? this.decapitalizeFirstLetter(results[1]) : "";
    }

    public stringifyParameters():any {
        return KeyValue.stringifyParametersStatic(this.parameters);
    }
}

export class KeyValue {
    public key:string;
    public value:string;
    constructor(key:string, value:string){
        this.key = key;
        this.value = value;
    };

    public static getParameterByName(inputParams:KeyValue[], inputName:string):KeyValue {
        let result:KeyValue = undefined;
        if (inputParams != undefined) {
            for (let i = 0; i < inputParams.length; i++) {
                if (inputName === inputParams[i].key) {
                    result = inputParams[i];
                    break;
                }
            }
        }
        return result;
    }

    public static stringifyParametersStatic(value:KeyValue[]): any {
        let returnValue:{ [key:string]:string; } = {};
            value.forEach(function(obj){
                returnValue[obj.key] = obj.value;
            });
        return returnValue;
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