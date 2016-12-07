import { Component, Input, OnInit } from '@angular/core';
import { TypedEntity } from '../model/model.typedEntity';

import { ApiClient, REST } from '../app.restClient';
import { KeyValue, Entity } from '../model/model.entity';
import { ParamDescriptor } from '../model/model.paramDescriptor';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Gateway } from '../model/model.gateway';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

@Component({
  selector: 'newEntity',
  templateUrl: 'app/components/templates/add-entity.component.html',
  styleUrls: ['app/components/templates/css/add-entity.component.css']
})
export class AddEntity implements OnInit {
    @Input() entities: TypedEntity[];
    @Input() type:string;
    selectedType:EntityDescriptor = undefined;
    selectedName:string = undefined;
    paramDescriptors:Observable<ParamDescriptor[]> = undefined ;
    params:KeyValue[] = [];
    containsRequiredParam:boolean = false;
    readyForSave:boolean = false;
    availableEntities:EntityDescriptor[] = [];

    constructor(private http:ApiClient){};

    ngOnInit() {
        this.http.get(REST.AVAILABLE_ENTITIES_BY_TYPE(this.type))
            .map((res:Response) => res.json())
            .subscribe(data => {
                for (let key in data) {
                    this.availableEntities.push(new EntityDescriptor(data[key]));
                }
            });
    }
    nameSelected():boolean {
        return this.selectedName != undefined && this.selectedName.length > 3;
    }

    selectType(selected:EntityDescriptor) {
        this.selectedType = selected;
         this.paramDescriptors = this.http.get(REST.ENTITY_PARAMETERS_DESCRIPTION(this.type, selected.type))
            .map((res: Response) => {
                let data = res.json();
                let returnValue:ParamDescriptor[] = [];
                for (let obj in data) {
                   returnValue.push(new ParamDescriptor(data[obj]));
                }
                return returnValue;
            });
         this.paramDescriptors
            .subscribe((res:ParamDescriptor[]) => {
                this.params = [];
                this.containsRequiredParam = false;
                this.readyForSave = false;
                for (let i = 0; i < res.length; i++) {
                    let obj:ParamDescriptor = res[i];
                    if (obj.required) {
                        this.containsRequiredParam = true;
                        let paramValue:string = ParamDescriptor.stubValue;
                        if (obj.defaultValue != undefined && obj.defaultValue.length > 0) {
                            paramValue = obj.defaultValue;
                        }
                        this.params.push(new KeyValue(obj.name, paramValue));
                     }
                }
                this.readyForSave = TypedEntity.checkForRequiredFilled(this.params, res)
                    && this.nameSelected() && this.selectedType != undefined;
             });


    }

    typeSelected():boolean {
        return this.nameSelected() && this.selectedType != undefined && this.containsRequiredParam;
    }

    addEntity() {
        if (this.type == "gateway") {
            let newGateway:TypedEntity = new Gateway(
                this.http,
                this.selectedName,
                this.selectedType.type,
                Entity.stringifyParametersStatic(this.params)
            );
            this.http.put(REST.GATEWAY_BY_NAME(newGateway.name), newGateway.stringify())
                .map((res:Response) => res.json())
                .subscribe(res => {
                    this.entities.push(newGateway);
                });
        } // else for further entities type please
    }

    saveParameter(param:KeyValue) {
        this.params.forEach(function(obj:KeyValue) {
            if (obj.key === param.key) {
                obj.value = param.value;
            }
        });
    }

    clear() {
        this.selectedType = undefined;
        this.selectedName = undefined;
        this.paramDescriptors = undefined ;
        this.params = [];
        this.containsRequiredParam = false;
        this.readyForSave = false;
    }
}

class EntityDescriptor {
    name:string = "";
    description:string = "";
    isActive:boolean = false;
    version:string = "0.0";
    type:string = "";
    constructor(parameters: { [key:string]:string; }) {
        this.name = parameters["name"];
        this.description = parameters["description"];
        this.isActive = parameters["state"] === "ACTIVE";
        this.version = parameters["version"];
        this.type = parameters["type"];
    }
}