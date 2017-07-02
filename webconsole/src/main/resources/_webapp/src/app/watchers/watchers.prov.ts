import { Component, OnInit, ViewContainerRef } from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { Response } from '@angular/http';
import { HealthStatus } from '../services/model/healthstatus/health.status';

import 'rxjs/add/operator/publishLast';

import { Overlay } from "angular2-modal";
import { Modal } from 'angular2-modal/plugins/bootstrap/index';
import {StatusFactory} from "../services/model/healthstatus/factory";


@Component({
  moduleId: module.id,
  templateUrl: './templates/prov.html',
  styleUrls: [ './templates/css/prov.css' ]
})
export class WatcherProvisioning implements OnInit {

    private http:ApiClient;

    constructor(apiClient: ApiClient, private modal: Modal,  overlay: Overlay, vcRef: ViewContainerRef) {
        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;
   }

   ngOnInit():void {
        console.log("Hello from WatcherProvisioning");
   }

}