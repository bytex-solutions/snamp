import { Component, OnInit, ViewContainerRef, ViewEncapsulation } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Overlay } from 'angular2-modal';
import { Modal } from 'angular2-modal/plugins/vex';


@Component({
  moduleId: module.id,
  templateUrl: './templates/dashboard.html',
  styleUrls: ['./templates/css/dashboard.css']
})
export class Dashboard {

  private http:ApiClient;

  constructor(apiClient: ApiClient,
              overlay: Overlay,
              vcRef: ViewContainerRef,
              private modal: Modal) {
        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;
   }
}

