import { Component, Input ,ViewChild, ElementRef, OnInit } from '@angular/core';
import { TypedEntity } from '../model/model.typedEntity';

import { ApiClient } from '../app.restClient';
import { KeyValue, Entity } from '../model/model.entity';
import { ParamDescriptor } from '../model/model.paramDescriptor';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Gateway } from '../model/model.gateway';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

import 'rxjs/add/operator/toPromise';

@Component({
  selector: 'newEntity',
  templateUrl: 'app/components/templates/add-entity.component.html'
})
export class AddEntity implements OnInit{
    @Input() entities: TypedEntity[];
    @Input() type:string;
    selectedType:EntityDescriptor = undefined;
    selectedName:string = undefined;
    paramDescriptors:Observable<ParamDescriptor[]> ;
    params:KeyValue[] = [];
    containsRequiredParam:boolean = false;
    availableEntities:EntityDescriptor[] = [];

    constructor(private http:ApiClient, private elementRef:ElementRef){};

    ngOnInit() {
        this.http.get("/snamp/console/management/" + this.type + "s")
            .map((res:Response) => res.json())
            .subscribe(data => {
                for (let key in data) {
                    this.availableEntities.push(new EntityDescriptor(key, data[key]));
                }
                if (this.availableEntities.length > 0) {
                    this.selectedType = this.availableEntities[0];
                }
            })
    }

    ngAfterViewInit() {
      var s = document.createElement("script");
      s.type = "text/javascript";
      s.src = "../templates/js/isotope.pkgd.js";
      this.elementRef.nativeElement.appendChild(s);
    }

    nameSelected():boolean {
        return this.selectedName != undefined && this.selectedName.length > 0;
    }

    selectType(selected:EntityDescriptor) {
        this.selectedType = selected;
         this.paramDescriptors = this.http.get("/snamp/console/management/"+ this.type + "/" + selected.type + "/configuration")
            .map((res: Response) => {
                let data = res.json();
                let returnValue:ParamDescriptor[] = [];
                for (let obj in data) {
                   returnValue.push(new ParamDescriptor(data[obj]));
                }
                return returnValue;
            });
         this.paramDescriptors
            .filter((res:ParamDescriptor[], index:number) => res[index].required)
            .do((res:ParamDescriptor[]) => {
                res.forEach(function(obj:ParamDescriptor){
                    this.containsRequiredParam = true;
                    let paramValue:string = ParamDescriptor.stubValue;
                    if (obj.defaultValue != undefined && obj.defaultValue.length > 0) {
                        paramValue = obj.defaultValue;
                    }
                    this.params.push(new KeyValue(obj.name, paramValue));
                });
             });
    }

    typeSelected():boolean {
        return this.selectedType != undefined && this.containsRequiredParam;
    }

    addEntity() {
        if (this.type == "gateway") {
            let newGateway:TypedEntity = new Gateway(
                this.http,
                this.selectedName,
                this.selectedType.type,
                Entity.stringifyParametersStatic(this.params)
            );
            this.http.put("/snamp/console/gateway/" + newGateway.name, newGateway.stringify())
                .map((res:Response) => res.json())
                .subscribe(res => {
                    this.entities.push(newGateway);
                })
        } // else for further entities type please
    }
}

class EntityDescriptor {
    name:string = "";
    description:string = "";
    isActive:boolean = false;
    version:string = "0.0";
    type:string = "";
    constructor(name:string, parameters: { [key:string]:string; }) {
        this.name = name;
        this.description = parameters["description"];
        this.isActive = parameters["state"] === "ACTIVE";
        this.version = parameters["version"];
        this.type = parameters["type"];
    }
}