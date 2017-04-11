import { Component, ViewEncapsulation } from '@angular/core';
import { ViewService } from '../services/app.viewService';
import { ChartService } from '../services/app.chartService';
import { Observable } from 'rxjs/Observable';
import { Router } from '@angular/router';
import 'rxjs/add/observable/of';
import { overlayConfigFactory } from "angular2-modal";
import {
  VEXBuiltInThemes,
  Modal,
  DialogFormModal
} from 'angular2-modal/plugins/vex';

@Component({
  selector: 'side-bar',
  styleUrls: [ '../app.style.css', './vex.css'],
  templateUrl: './sidebar.component.html',
  encapsulation: ViewEncapsulation.None,
  entryComponents: [
    DialogFormModal
  ]
})

export class Sidebar {
    constructor(private _viewService:ViewService, private _chartService:ChartService,
        private modal: Modal, private _router: Router) {}

    private views:string[] = [];
    private groupNames:string[] = [];

    ngOnInit() {
        this._viewService.getViewNames().subscribe((data:string[]) => {
            this.views = data;
        });

        this._chartService.getGroups().subscribe((data:string[]) => {
            this.groupNames = data;
        });
    }

    ngAfterViewInit() {
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
                this._chartService.addNewGroup(result);
                this._router.navigateByUrl('/charts/' + result);
             })
    }
}
