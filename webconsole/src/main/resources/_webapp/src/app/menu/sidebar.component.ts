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

    ngAfterViewInit() {
        $(document).ready(function () {
            setTimeout(function () {
                // open the current active element on the left side panel
                let activeLi = $('li.activeLi');
                activeLi.parents('li').addClass('active active-sm');
                activeLi.parents("ul").slideDown();

                // handle click event for main menu buttons (with icons)
                $('a.clickableAnchor').click(function() {
                    $(this).parents('li').siblings().find('ul.child_menu').slideUp(); // close all li except this
                    $(this).parents('li').find('ul.child_menu').slideToggle();
                });

                // handle click event on the child menu nodes
                $('ul.child_menu li').click(function() {
                    if ($('body').hasClass('nav-sm')) {
                        $(this).parents('li').addClass('active-sm');
                        $(this).parents('li').siblings().removeClass('active-sm');
                        $(this).parents('li').find('ul').slideUp();
                    } else {
                        $(this).parents('li').siblings().removeClass('active');
                        $(this).parents('li').addClass('active');
                    }
                });
            }, 500);
        });
    }

    newDashboard():void {
        this.modal.prompt()
            .className(<VEXBuiltInThemes>'default')
            .message('New dashboard')
            .placeholder('Please set the name for a new dashboard')
            .open()
             .then(dialog => dialog.result)
             .then(result => {
                this._chartService.addNewGroup(result);
                this._router.navigateByUrl('/charts/' + result);
             })
            .catch(() => {});
    }
}
