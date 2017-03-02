import { Component, Input, OnInit, EventEmitter, Output } from '@angular/core';
import { TypedEntity } from '../model/model.typedEntity';

import { ApiClient, REST } from '../../app.restClient';
import { KeyValue, Entity } from '../model/model.entity';
import { ParamDescriptor } from '../model/model.paramDescriptor';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Gateway } from '../model/model.gateway';
import { Resource } from '../model/model.resource';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

@Component({
  moduleId: module.id,
  selector: 'newEntity',
  templateUrl: './templates/add-entity.component.html',
  styleUrls: ['./templates/css/add-entity.component.css']
})
export class AddEntity implements OnInit {
    @Input() entities: TypedEntity[];
    @Input() type:string;
    @Output() public onSave:EventEmitter<any> = new EventEmitter();
    selectedType:EntityDescriptor = undefined;
    selectedName:string = undefined;
    selectedConnectionString:string = "";
    paramDescriptors:Observable<ParamDescriptor[]> = undefined ;
    params:KeyValue[] = [];
    containsRequiredParam:boolean = false;
    readyForSave:boolean = false;
    availableEntities:EntityDescriptor[] = [];
    _innerType:string;

    constructor(private http:ApiClient){

    };

    ngOnInit() {
         if (this.type == "resourceGroup") {
            this._innerType = "connectors";
          } else {
              this._innerType = this.type;
          }
        this.http.get(REST.AVAILABLE_ENTITIES_BY_TYPE(this._innerType))
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
         this.paramDescriptors = this.http.get(REST.ENTITY_PARAMETERS_DESCRIPTION(this._innerType, selected.type))
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
      let newEntity:TypedEntity;
        if (this.type == "gateway") {
            newEntity = new Gateway(
                this.http,
                this.selectedName,
                this.selectedType.type,
                KeyValue.stringifyParametersStatic(this.params)
            );
            this.http.put(REST.GATEWAY_BY_NAME(newEntity.name), newEntity.stringify())
                .subscribe(res => {
                    this.entities.push(newEntity);
                    this.onSave.emit(newEntity);
                });
        } else if(this.type == "resource") {
             let connectionString:string = this.selectedConnectionString;
             if (connectionString == undefined || connectionString.length < 4) {
                connectionString = ParamDescriptor.stubValue;
             }
             newEntity = new Resource(
                this.http,
                this.selectedName,
                Resource.stringify(this.selectedType.type, connectionString, this.params)
            );
            this.http.put(REST.RESOURCE_BY_NAME(newEntity.name), newEntity.stringify())
                .subscribe(res => {
                    this.entities.push(newEntity);
                    this.onSave.emit(newEntity);
                });
        }
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
        this.selectedConnectionString = "";
        this.paramDescriptors = undefined;
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
