import { Component, OnInit, ViewEncapsulation, ViewContainerRef } from '@angular/core';
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

import { overlayConfigFactory, Overlay } from "angular2-modal";
import {
  VEXBuiltInThemes,
  Modal,
  DialogFormModal
} from 'angular2-modal/plugins/vex';


@Component({
  moduleId: module.id,
  templateUrl: './templates/view.html',
  styleUrls: [ './templates/css/view.css' ],
  encapsulation: ViewEncapsulation.None,
  entryComponents: [
    DialogFormModal
  ]
})
export class MainView implements OnInit {

    private http:ApiClient;
    currentViewObs:Observable<E2EView> = undefined;
    currentView:E2EView = undefined;
    metadata:any = undefined;
    currentNodeId:string = undefined;
    _cyObject:any = undefined;
    nodeSelected:boolean = false;
    selectedLayout:string = "";
    textSize:string = "";

    constructor(apiClient: ApiClient, private route: ActivatedRoute, overlay: Overlay,
            private _viewService:ViewService,  private modal: Modal, vcRef: ViewContainerRef) {
        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;
   }

   ngOnInit():void {}

   ngAfterViewInit():void {
      $("#menu_toggle").trigger('click');
      this.currentViewObs = this.route.params
               .map(params => { return this._viewService.getViewByName(params['id']);});
      this.currentViewObs.publishLast().refCount();
      this.currentViewObs.subscribe((_view:E2EView) => {
            this.currentView = _view;
            this.selectedLayout = _view.getLayout();
            this.textSize = _view.getTextSize();

            // set checkboxes according to preferences
            let _chbx:string[] = _view.getDisplayedMetadata();
            for (let ij = 0; ij < _chbx.length; ij++) {
                $("#myTabContent2 input[name='" + _chbx[ij] + "']").prop('checked', true);
            }

            this._viewService.getDataForView(_view).subscribe((_data:any) => {
                  this._cyObject = _view.draw(_data);
                  this.handleCy(this._cyObject);
                  var _thisReference = this;
                  setInterval(function() {
                     _thisReference._viewService.getDataForView(_view).subscribe(updateData => {
                         _view.updateData(updateData);
                         if (_thisReference.currentNodeId != undefined) {
                            _thisReference.metadata = _thisReference._cyObject.$('#' + _thisReference.currentNodeId).data('arrival');
                         }
                     });
                   }, 1000);
            });
      });
   }

   public saveCheckboxStatus():void {
        var _cb = $("#myTabContent2 input[type=checkbox]:checked");
        let _array:string[] = $.map(_cb, function(element){return $(element).attr("name")});
        if (this.currentView != undefined) {
            this.currentView.setDisplayedMetadata(_array);
            this._viewService.saveDashboard();
        }
   }

   public onChangeLayout(event:any):void {
        this.currentView.changeLayout(event);
        this._viewService.saveDashboard();
   }

   public onChangeTextSize(event:any):void {
        this.currentView.changeTextSize(event);
        this._viewService.saveDashboard();
   }

   public resetView():void {
     this.modal.confirm()
        .className(<VEXBuiltInThemes>'default')
        .message('View will be reset to all users. Proceed?')
        .open()
         .then((resultPromise) => {
            return (<Promise<boolean>>resultPromise.result)
              .then((response) => {
                 this._viewService.resetView(this.currentView).subscribe(data => {
                    console.log("view has been reset: ", data);
                 });
                 return response;
              })
              .catch(() => {
                console.log("user prefered to decline view reset");
              });
          });
   }

   private handleCy(_cy:any):void {
        var _thisReference = this;
        _cy.on('tap', function(event){
          // cyTarget holds a reference to the originator
          // of the event (core or element)
          var evtTarget = event.cyTarget;
          _thisReference.nodeSelected = (evtTarget != _cy);
          if( evtTarget === _cy ){
               _thisReference.metadata = evtTarget;
               _thisReference.currentNodeId = undefined;
          } else {
            _thisReference.currentNodeId = evtTarget.data('id');
            _thisReference.metadata = _cy.$('#' + evtTarget.data('id')).data('arrival');
          }
        });
    }

}

