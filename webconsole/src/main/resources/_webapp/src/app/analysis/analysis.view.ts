import { Component, OnInit, ViewContainerRef, ViewEncapsulation } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Overlay } from 'angular2-modal';
import { Modal } from 'angular2-modal/plugins/vex';

import { ActivatedRoute } from '@angular/router';

import 'rxjs/add/operator/publishLast';
import 'rxjs/add/operator/cache';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/of';

import 'smartwizard';
import 'select2';


@Component({
  moduleId: module.id,
  templateUrl: './templates/view.html',
  styleUrls: [ './templates/css/view.css' ]
})
export class MainView {

    private http:ApiClient;
    private mainView:string = "";

    constructor(apiClient: ApiClient, overlay: Overlay, vcRef: ViewContainerRef, private modal: Modal,
        private route: ActivatedRoute) {

        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;
   }

   ngOnInit():void {
      this.route.params
          .map(params => params['id'])
          .subscribe((id) => {
             this.mainView = id;
          });
}

   ngAfterViewInit():void {
        var _thisReference = this;
        $(document).ready(function(){

        });
   }
}

