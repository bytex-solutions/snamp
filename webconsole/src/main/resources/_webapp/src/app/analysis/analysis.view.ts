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
    private currentView:E2EView = undefined;

    constructor(apiClient: ApiClient, private route: ActivatedRoute, private _viewService:ViewService) {
        this.http = apiClient;
   }

   ngOnInit():void {

    }


   ngAfterViewInit():void {
      this.route.params
          .map(params => params['id'])
          .subscribe((id) => {
             this.currentView = this._viewService.getViewByName(id);
             var _thisReference = this;
             setInterval(function() {
                _thisReference._viewService.getDataForView(_thisReference.currentView).subscribe(data => {
                    console.log(data);
                });
              }, 3000);

              this.currentView.draw();
          });
      }

}

