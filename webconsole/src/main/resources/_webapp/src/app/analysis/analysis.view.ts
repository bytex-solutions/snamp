import { Component, ViewEncapsulation, ViewContainerRef } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ActivatedRoute, Router } from '@angular/router';

import { E2EView } from './model/abstract.e2e.view';
import { ViewService } from '../services/app.viewService';

import 'rxjs/add/operator/publishLast';
import 'rxjs/add/operator/cache';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/of';

import { Overlay } from "angular2-modal";
import { VEXBuiltInThemes, Modal, DialogFormModal } from 'angular2-modal/plugins/vex';
import {Description} from "./analysis.add.view";

@Component({
    moduleId: module.id,
    templateUrl: './templates/view.html',
    styleUrls: ['./templates/css/view.css'],
    encapsulation: ViewEncapsulation.None,
    entryComponents: [
        DialogFormModal
    ]
})
export class MainView {

    currentViewObs: Observable<E2EView> = undefined;
    currentView: E2EView = undefined;
    metadata: any = undefined;
    currentNodeId: string = undefined;
    _cyObject: any = undefined;
    nodeSelected: boolean = false;
    selectedLayout: string = "";
    textSize: string = "";
    textColor: string = "";
    backgroundColor: string = "";
    textOutlineColor: string = "";
    textOutlineWidth: number = 0;
    textWeight: number = 0;
    edgeWidth: number = 0;
    edgeLineColor: string = "";
    edgeArrowColor: string = "";
    edgeArrowShape: string = "";
    showSettings:boolean = false;

    shelfLife:number = 1;
    useShelfLife:boolean = false;
    oldValueShelfLife:number = 1;

    shelfLifeChanged:boolean = false;

    periods = Description.createPeriodsTypes();

    timerId: any = undefined;

    constructor(private route: ActivatedRoute, overlay: Overlay, private router: Router,
                private _viewService: ViewService, private modal: Modal, vcRef: ViewContainerRef) {
        overlay.defaultViewContainer = vcRef;
    }

    ngAfterViewInit(): void {
        this.currentViewObs = this.route.params
            .map(params => {
                return this._viewService.getViewByName(params['id']);
            });
        this.currentViewObs.publishLast().refCount();
        this.currentViewObs.subscribe((_view: E2EView) => {
            this.currentView = _view;
            this.selectedLayout = _view.getLayout();
            this.textSize = _view.getTextSize();
            this.textColor = _view.getTextColor();
            this.backgroundColor = _view.getBackgroundColor();
            this.textOutlineColor = _view.getTextOutlineColor();
            this.textOutlineWidth = _view.getTextOutlineWidth();
            this.textWeight = _view.getTextWeight();

            this.edgeWidth = _view.getEdgeWidth();
            this.edgeLineColor = _view.getEdgeLineColor();
            this.edgeArrowColor = _view.getEdgeArrowColor();
            this.edgeArrowShape = _view.getEdgeArrowShape();

            this.shelfLife = _view.shelfLife;
            this.useShelfLife = _view.isShelfLifeSet;
            this.oldValueShelfLife = _view.shelfLife;

            console.debug(this.selectedLayout, this.textSize, this.textColor, this.backgroundColor, this.textOutlineColor,
                this.textOutlineWidth, this.textWeight, this.edgeWidth, this.edgeLineColor, this.edgeArrowColor, this.edgeArrowShape);


            // set checkboxes according to preferences
            let _chbx: string[] = _view.getDisplayedMetadata();
            for (let ij = 0; ij < _chbx.length; ij++) {
                $("#myTabContent2 input[type='checkbox'][name='" + _chbx[ij] + "']").prop('checked', true);
            }

            this._viewService.getDataForView(_view).subscribe((_data: any) => {
                this._cyObject = _view.draw(_data);
                this.handleCy(this._cyObject);
                var _thisReference = this;
                this.timerId = setInterval(function () {
                    if (!document.hidden) {
                        _thisReference._viewService.getDataForView(_view).subscribe(updateData => {
                            _view.updateData(updateData);
                            if (_thisReference.currentNodeId != undefined) {
                                _thisReference.metadata = _thisReference._cyObject.$('#' + _thisReference.currentNodeId).data('arrival');
                            }
                        })
                    }
                }, 3000);

            });
        });
    }

    public saveCheckboxStatus(): void {
        let _cb = $("#myTabContent2 input[type=checkbox]:checked");
        let _array: string[] = $.map(_cb, function (element) {
            return $(element).attr("name")
        });
        if (this.currentView != undefined) {
            this.currentView.setDisplayedMetadata(_array);
            this._viewService.saveDashboard();
        }
    }

    public onChangeLayout(event: any): void {
        this.currentView.changeLayout(event);
        this._viewService.saveDashboard();
    }

    public onChangeTextSize(event: any): void {
        this.currentView.changeTextSize(event);
        this._viewService.saveDashboard();
    }

    public onChangeTextColor(event: any): void {
        this.currentView.changeTextColor(event);
        this._viewService.saveDashboard();
    }

    public onChangeBackgroundColor(event: any): void {
        this.currentView.changeBackgroundColor(event);
        this._viewService.saveDashboard();
    }

    public onChangeTextOutlineColor(event: any): void {
        this.currentView.changeTextOutlineColor(event);
        this._viewService.saveDashboard();
    }

    public onChangeTextOutlineWidth(event: any): void {
        this.currentView.changeTextOutlineWidth(event);
        this._viewService.saveDashboard();
    }

    public onChangeTextWeight(event: any): void {
        this.currentView.changeTextWeight(event);
        this._viewService.saveDashboard();
    }

    public onChangeEdgeWidth(event: any): void {
        this.currentView.changeEdgeWidth(event);
        this._viewService.saveDashboard();
    }

    public onChangeEdgeLineColor(event: any): void {
        this.currentView.changeEdgeLineColor(event);
        this._viewService.saveDashboard();
    }

    public onChangeEdgeArrowColor(event: any): void {
        this.currentView.changeEdgeArrowColor(event);
        this._viewService.saveDashboard();
    }

    public onChangeEdgeArrowShape(event: any): void {
        this.currentView.changeEdgeArrowShape(event);
        this._viewService.saveDashboard();
    }

    public onChangeShelfLife(event: any): void {
        this.currentView.shelfLife = event;
        this.shelfLifeChanged = false;
        this._viewService.saveDashboard();
    }

    public saveShelfLife():void {
        this.onChangeShelfLife(this.shelfLife);
    }

    public triggerShelfLifeChanged(value:number):void {
        this.shelfLifeChanged = (this.oldValueShelfLife != value);
    }

    public triggerUseShelfLife(event:boolean):void {
        this.currentView.isShelfLifeSet = event;
        console.debug("Current value from event: ", event, this.currentView.isShelfLifeSet);
        this._viewService.saveDashboard();
    }

    public resetView(): void {
        this.modal.confirm()
            .className(<VEXBuiltInThemes>'default')
            .message('View will be reset to all users. Proceed?')
            .open()
            .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                    .then((response) => {
                        this._viewService.resetView(this.currentView).subscribe(data => {
                            console.debug("view has been reset: ", data);
                        });
                        return response;
                    })
                    .catch(() => {
                        console.debug("user preferred to decline view reset");
                    });
            }).catch(() => {});
    }

    private handleCy(_cy: any): void {
        let _thisReference = this;
        _cy.on('tap', function (event) {
            // cyTarget holds a reference to the originator
            // of the event (core or element)
            let evtTarget = event.cyTarget;
            _thisReference.nodeSelected = (evtTarget != _cy);
            if (evtTarget === _cy) {
                _thisReference.metadata = evtTarget;
                _thisReference.currentNodeId = undefined;
            } else {
                _thisReference.currentNodeId = evtTarget.data('id');
                _thisReference.metadata = _cy.$('#' + evtTarget.data('id')).data('arrival');
            }
        });
    }

    ngOnDestroy() {
        clearInterval(this.timerId);
    }

    removeView(): void {
        this.modal.confirm()
            .className(<VEXBuiltInThemes>'default')
            .message('View will be removed. Proceed?')
            .open()
            .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                    .then((response) => {
                        this._viewService.removeView(this.currentView.name);
                        let _arr:E2EView[] = this._viewService.getViews();
                        if (_arr.length > 0) {
                            this.router.navigate(['view', _arr[0].name]);
                        } else {
                            this.router.navigate(['view']);
                        }
                        return response;
                    })
                    .catch(() => {
                        console.debug("user preferred to decline view removing");
                    });
            }).catch(() => {});
    }

    toggleShowSettings():void {
        this.showSettings = !this.showSettings;
        let _btn:any = $("#btnSettings");
        let _menuBlock:any = $("#viewMenu");
        _menuBlock.css("top", (_btn.offset().top + _btn.height() + 150) + "px");
        _menuBlock.height(($("footer").offset().top - _menuBlock.offset().top - 150) + "px");
    }
}

