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
    metadata:any = undefined;
    _cyObject:any = undefined;

    constructor(apiClient: ApiClient, private route: ActivatedRoute, private _viewService:ViewService) {
        this.http = apiClient;
   }

   ngOnInit():void {}

   ngAfterViewInit():void {
      $("#menu_toggle").trigger('click');
      this.currentViewObs = this.route.params
               .map(params => { return this._viewService.getViewByName(params['id']);});
      this.currentViewObs.publishLast().refCount();
      this.currentViewObs.subscribe((_view:E2EView) => {
            this._viewService.getDataForView(_view).subscribe((_data:any) => {
                  this._cyObject = _view.draw(_data);
                  this.handleCy(this._cyObject);
                  var _thisReference = this;
                  setInterval(function() {
                     _thisReference._viewService.getDataForView(_view).subscribe(updateData => {
                         _view.updateData(updateData);
                     });
                   }, 3000);
            });
      });
   }

   private handleCy(_cy:any):void {
        var _thisReference = this;
        _cy.on('tap', function(event){
              // cyTarget holds a reference to the originator
              // of the event (core or element)
              var evtTarget = event.cyTarget;

              if( evtTarget === _cy ){
                  console.log('tap on background');
                   _thisReference.metadata = evtTarget;
              } else {
                console.log('tap on some element', evtTarget);
                _thisReference.metadata = evtTarget.data('arrival');
              }
            });
    }

}

