import { Component, OnInit } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

@Component({
  moduleId: module.id,
  templateUrl: './templates/fullsave.html'.
  styleUrls: [ './templates/css/fullsave.css' ]
})
export class FullSaveComponent implements OnInit {

  private http:ApiClient;
  public currentConfiguration:Observable<any>;

  constructor(apiClient: ApiClient) {
        this.http = apiClient;
   }

   ngOnInit() {
        this.currentConfiguration = this.http.get(REST.CURRENT_CONFIG)
            .map((data:Response) => JSON.stringify(data.json(), null, 4));
   }

   save() {
   }

   load() {
   }

}