import { Component, OnInit, ViewEncapsulation, ViewContainerRef } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Factory } from './model/factory';
import { Watcher } from './model/watcher';

import { HealthStatus } from './model/health.status';

import 'rxjs/add/operator/publishLast';

import { overlayConfigFactory, Overlay } from "angular2-modal";
import {
  VEXBuiltInThemes,
  Modal,
  DialogFormModal
} from 'angular2-modal/plugins/vex';


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
              _thisReference.http.get(REST.WATCHERS_STATUS)
                 .map((res:Response) => res.json())
                 .subscribe((data) => {
                    _thisReference.statuses = Factory.parseAllStatuses(data);
                    console.log(_thisReference.statuses);
                 });
         }, 2000);
   }

    ngOnDestroy() {
        clearInterval(this.timerId);
    }

}