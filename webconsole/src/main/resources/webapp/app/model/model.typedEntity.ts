import { Entity, KeyValue } from './model.entity';
import { ApiClient } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Binding } from './model.binding';
import { ParamDescriptor } from './model.paramDescriptor';

export abstract class TypedEntity extends Entity {
    public type:string;
    public name:string;
    http:ApiClient;
    public paramDescriptors:Observable<ParamDescriptor[]>;
    constructor(http:ApiClient, name:string, type:string, parameters: { [key:string]:string; }) {
        super(parameters);
        this.http = http;
        this.type = type;
        this.name = name;

        // retrieving parameters description
        this.paramDescriptors = this.http.get("/snamp/console/management/"+ this.getName() + "/" + this.type + "/configuration")
            .map((res: Response) => {
                let data = res.json();
                let returnValue:ParamDescriptor[] = [];
                for (let obj in data) {
                   returnValue.push(new ParamDescriptor(data[obj]));
                }
                return returnValue;
            });
    }

    public isParamRequired(name:string):boolean {
        let res:ParamDescriptor = this.getParamDescriptor(name);
        return res != undefined && res.required;
    }

    public getParamDescriptor(name:string):ParamDescriptor {
        let res:ParamDescriptor;
        this.paramDescriptors.subscribe((descriptors:ParamDescriptor[]) => {
            descriptors.forEach(function(current:ParamDescriptor) {
              if (current.name == name) {
                res = current;
                return;
              }
            });
        });
        return res;
    }

    public isReadyToBeSaved() {
        let res:boolean = true;
        this.parameters.forEach(function(param:KeyValue) {
            if (this.isParamRequired(param.key) && param.value == ParamDescriptor.stubValue) {
                res = false;
                return;
            }
        });
        return res;
    }
}