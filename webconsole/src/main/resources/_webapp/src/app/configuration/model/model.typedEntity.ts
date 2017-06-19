import { Entity, KeyValue } from './model.entity';
import { ApiClient, REST } from '../../services/app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { ParamDescriptor } from './model.paramDescriptor';

export abstract class TypedEntity extends Entity {
    public type:string;
    http:ApiClient;
    private static readonly SMART_MODE = "smartMode";
    private static readonly GROUP = "group";
    public paramDescriptors:Observable<ParamDescriptor[]>;
    constructor(http:ApiClient, name:string, type:string, parameters: { [key:string]:string; }) {
        super(name, parameters);
        this.http = http;
        this.type = type;
        this.name = name;

        // retrieving parameters description - extract as static to explicit class please @todo
        this.paramDescriptors = this.http.get(REST.ENTITY_PARAMETERS_DESCRIPTION(this.getDescriptionType(), this.type))
            .map((res: Response) => {
                let data = res.json();
                let returnValue:ParamDescriptor[] = [];
                for (let obj in data) {
                   let newDescriptor:ParamDescriptor = new ParamDescriptor(data[obj]);
                   // remove group and smart mode descriptors because they are processed another way
                   if ((this.getName() == "resource" || this.getName() == "resourceGroup") && (newDescriptor.name == TypedEntity.SMART_MODE || newDescriptor.name == TypedEntity.GROUP)) {
                      let k:boolean = false;
                   } else
                       returnValue.push(newDescriptor);
                   }
                 return returnValue;
            });
    }

    public getDescriptionType():string {
        return this.getName();
    }

    public isParamRequired(name:string):Observable<boolean> {
        return this.getParamDescriptor(name).map((res:ParamDescriptor) => res != undefined && res.required);
    }

    public getParamDescriptor(name:string):Observable<ParamDescriptor> {
        return this.paramDescriptors
            .map((descriptors:ParamDescriptor[]) => ParamDescriptor.getDescriptorByName(descriptors, name));
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

    public isReadyToBeSaved():Observable<boolean> {
        return this.paramDescriptors.map((res:ParamDescriptor[]) => {
            return TypedEntity.checkForRequiredFilled(this.parameters, res);
        })
    }

    public stringify():string {
        let resultValue:{ [key:string]:string; } = {};
        resultValue["type"] = this.type;
        resultValue["parameters"] = this.stringifyParameters();
        return JSON.stringify(resultValue);
    }
}
