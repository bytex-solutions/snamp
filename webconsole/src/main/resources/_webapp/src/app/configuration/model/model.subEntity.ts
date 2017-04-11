import { Entity, KeyValue } from './model.entity';
import { TypedEntity } from './model.typedEntity';
import { ApiClient, REST } from '../../services/app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Binding } from './model.binding';
import { ParamDescriptor } from './model.paramDescriptor';

export abstract class SubEntity extends Entity {
    public type:string;
    http:ApiClient;
    public paramDescriptors:Observable<ParamDescriptor[]>;
    constructor(http:ApiClient, name:string, type:string, parameters: { [key:string]:string; }) {
        super(name, parameters);
        this.http = http;
        this.type = type;
        this.name = name;

        this.paramDescriptors = this.http.get(REST.SUBENTITY_PARAMETERS_DESCRIPTION(this.type, this.getName()))
            .map((res: Response) => {
                let data = res.json();
                let returnValue:ParamDescriptor[] = [];
                for (let obj in data) {
                   returnValue.push(new ParamDescriptor(data[obj]));
                }
                return returnValue;
            });
    }

    public isParamRequired(name:string):Observable<boolean> {
        return this.getParamDescriptor(name).map((res:ParamDescriptor) => res != undefined && res.required);
    }

    public getParamDescriptor(name:string):Observable<ParamDescriptor> {
        return this.paramDescriptors
            .map((descriptors:ParamDescriptor[]) => ParamDescriptor.getDescriptorByName(descriptors, name));
    }

    public isReadyToBeSaved():Observable<boolean> {
        return this.paramDescriptors.map((res:ParamDescriptor[]) => {
            return TypedEntity.checkForRequiredFilled(this.parameters, res);
        })
    }

    public stringify():string {
        let resultValue:{ [key:string]:string; } = {};
        resultValue["parameters"] = this.stringifyParameters();
        return JSON.stringify(resultValue);
    }

    public abstract stringifyFullObject():string;
}
