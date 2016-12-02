import { Component, Input ,ViewChild, ElementRef } from '@angular/core';
import { ApiClient } from '../app.restClient';
import { KeyValue } from '../model/model.entity';
import { TypedEntity } from '../model/model.typedEntity';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/toPromise';

@Component({
  selector: 'parameters',
  templateUrl: 'app/components/templates/parameters-table.component.html'
})
export class ParametersTable {
    @Input() entity: TypedEntity;
    @ViewChild('newParam') newParamElement:ElementRef;
    http:ApiClient;

    constructor(http:ApiClient) {
        this.http = http;
    }

    getUrlForParameter(key:string):string {
        return "/snamp/console/" + this.entity.getName() + "/" + this.entity.name + "/parameters/" + key;
    }

    saveParameter(parameter:KeyValue) {
        this.http.put(this.getUrlForParameter(parameter.key), parameter.value)
            .map((res: Response) => res.text())
            .subscribe(data => this.entity.setParameter(parameter));
    }

    removeParameter(parameter:KeyValue) {
        this.http.delete(this.getUrlForParameter(parameter.key))
            .map((res: Response) => res.text())
            .subscribe(data => this.entity.removeParameter(parameter.key));
    }

    addNewParameter() {
        this.saveParameter(new KeyValue(this.newParamElement.nativeElement.value, "value"));
    }
}