import { Component, OnInit, ViewContainerRef, ViewEncapsulation } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Overlay } from 'angular2-modal';
import { Modal } from 'angular2-modal/plugins/vex';

import 'rxjs/add/operator/publishLast';
import 'rxjs/add/operator/cache';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/of';

import 'smartwizard';
import 'select2';


@Component({
  moduleId: module.id,
  templateUrl: './templates/addView.html',
  styleUrls: [ './templates/css/addView.css' ]
})
export class AddView {

        private http:ApiClient;

        constructor(apiClient: ApiClient,
              overlay: Overlay,
              vcRef: ViewContainerRef,
              private modal: Modal) {
        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;
   }

   ngOnInit():void {

   }

   ngAfterViewInit():void {
        var _thisReference = this;
        $(document).ready(function(){

        });
   }
}

