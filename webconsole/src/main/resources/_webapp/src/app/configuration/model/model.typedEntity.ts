import { Entity, KeyValue } from './model.entity';
import { ApiClient, REST } from '../../services/app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { ParamDescriptor } from './model.paramDescriptor';

export abstract class TypedEntity extends Entity {
    public type:string;
    http:ApiClient;
    constructor(http:ApiClient, name:string, type:string, parameters: { [key:string]:string; }) {
        super(name, parameters);
        this.http = http;
        this.type = type;
        this.name = name;
    }

    public getDescriptionType():string {
        return this.getName();
    }

    public static checkForRequiredFilled(inputValue:KeyValue[], res:ParamDescriptor[]):boolean {
         let result:boolean = true;
         for (let i = 0; i < res.length; i++) {
            if (res[i].required) {
                let _param = KeyValue.getParameterByName(inputValue, res[i].name);
                if (_param != undefined && _param.value == ParamDescriptor.stubValue) {
                    result = false;
                }
            }
         }
         return result;
    }

    public stringify():string {
        let resultValue:{ [key:string]:string; } = {};
        resultValue["type"] = this.type;
        resultValue["parameters"] = this.stringifyParameters();
        return JSON.stringify(resultValue);
    }
}
