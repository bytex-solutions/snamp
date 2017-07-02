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
  templateUrl: './templates/statuses.html',
  styleUrls: [ './templates/css/statuses.css' ]
})
export class WatcherDashboard implements OnInit {

    private http:ApiClient;
    timerId:any = undefined;
    statuses:HealthStatus[] = [];

    constructor(apiClient: ApiClient, private modal: Modal,  overlay: Overlay, vcRef: ViewContainerRef) {
        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;
   }

   ngOnInit():void {}

   ngAfterViewInit():void {
        var _thisReference = this;
        // load the list of watchers
         this.timerId = setInterval(function(){
              _thisReference.http.get(REST.SUPERVISORS_STATUS)
                 .map((res:Response) => res.json())
                 .subscribe((data) => {
                    _thisReference.statuses = StatusFactory.parseAllStatuses(data);
                 });
         }, 2000);
   }

    ngOnDestroy() {
        clearInterval(this.timerId);
    }

    showDetails(status:HealthStatus):void {
        this.modal.alert()
           .size('lg')
           .title("Details for health status")
           .body(status.htmlDetails())
           .isBlocking(false)
           .keyboard(27)
           .open()
    }

}