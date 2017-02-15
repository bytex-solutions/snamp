import { Component, OnInit } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { ActivatedRoute } from '@angular/router';

import { E2EView } from './model/abstract.e2e.view';
import { ViewService } from '../app.viewService';

import 'rxjs/add/operator/publishLast';
import 'rxjs/add/operator/cache';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/of';

@Component({
  moduleId: module.id,
  templateUrl: './templates/view.html',
  styleUrls: [ './templates/css/view.css' ]
})
export class MainView implements OnInit {

    private http:ApiClient;
    currentViewObs:Observable<E2EView> = undefined;

    constructor(apiClient: ApiClient, private route: ActivatedRoute, private _viewService:ViewService) {
        this.http = apiClient;
   }

   ngOnInit():void {}

   ngAfterViewInit():void {
      this.currentViewObs = this.route.params
               .map(params => {
                        console.log("Retrieving some view from id: ", params['id']);
                        return this._viewService.getViewByName(params['id']);
                    }
                );
      this.currentViewObs.publishLast().refCount();
      this.currentViewObs.subscribe((_view:E2EView) => {
            console.log("trying to receive some data for view: ", _view);
            this._viewService.getDataForView(_view).subscribe((_data:any) => {
                  _view.draw(_data);
                  var _thisReference = this;
                  setInterval(function() {
                     _thisReference._viewService.getDataForView(_view).subscribe(updateData => {
                         _view.updateData(updateData);
                     });
                   }, 3000);
            });
      });
   }
}

