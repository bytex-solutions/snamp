import { Component, ViewEncapsulation } from '@angular/core';
import { ViewService } from '../app.viewService';
import { ChartService } from '../app.chartService';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/observable/of';

import { overlayConfigFactory } from "angular2-modal";
import {
  VEXBuiltInThemes,
  Modal,
  DialogPreset,
  DialogFormModal,
  DialogPresetBuilder,
  VEXModalContext,
  VexModalModule,
  providers
} from 'angular2-modal/plugins/vex';

@Component({
  selector: 'side-bar',
  styleUrls: [ '../app.style.css', './vex.css'],
  templateUrl: './sidebar.component.html',
  providers: providers,
  encapsulation: ViewEncapsulation.None
})

export class Sidebar {
    constructor(private _viewService:ViewService, private _chartService:ChartService, private modal: Modal) {}

    private views:string[] = [];
    private groupNames:Observable<string[]>;

    ngOnInit() {
        this.views = [];//this._viewService.getViewNames();
    }

    ngAfterViewInit() {
        this.groupNames = this._chartService.getGroups();
    }

    anchorClicked(event: MouseEvent) {
        var target = event.target || event.srcElement || event.currentTarget;
        var idAttr = $(target).attr("id");
        var $li = $('#' + idAttr.replace("chevron","li")).parent();
        if ($li.is('.active')) {
            $li.removeClass('active active-sm');
                $('ul:first', $li).slideUp();
            } else {
                // prevent closing menu if we are on child menu
                if (!$li.parent().is('.child_menu')) {
                    $('#sidebar-menu').find('li').removeClass('active active-sm');
                    $('#sidebar-menu').find('li ul').slideUp();
                }
                $li.addClass('active');
                $('ul:first', $li).slideDown();
            }
    }

    newDashboard():void {
        this.modal.prompt()
            .className(<VEXBuiltInThemes>'default')
            .message('New dashboard')
            .placeholder('Please set the name for a new dashboard')
            .open()
             .then(dialog => dialog.result)
             .then(result => {
                console.log("result",result);
             })
    }
}
