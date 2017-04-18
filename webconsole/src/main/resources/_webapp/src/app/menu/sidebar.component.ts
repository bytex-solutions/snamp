import { Component, ViewEncapsulation } from '@angular/core';
import { ViewService } from '../services/app.viewService';
import { ChartService } from '../services/app.chartService';
import { Router } from '@angular/router';
import 'rxjs/add/observable/of';
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

    linkClicked(idAttr:string):void {
        let $li = $('#' + idAttr + "chevron").closest('li');
        let $sd = $('#sidebar-menu');
        if ($li.is('.active')) {
            $li.removeClass('active active-sm');
                $('ul:first', $li).slideUp();
            } else {
                // prevent closing menu if we are on child menu
                if (!$li.parent().is('.child_menu')) {
                    $sd.find('li').removeClass('active active-sm');
                    $sd.find('li ul').slideUp();
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
