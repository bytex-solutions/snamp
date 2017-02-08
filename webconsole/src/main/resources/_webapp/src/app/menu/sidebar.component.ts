import { Component } from '@angular/core';
import { ViewService } from '../app.viewService';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/observable/of';

@Component({
  selector: 'side-bar',
  styleUrls: [ '../app.style.css' ],
  templateUrl: './sidebar.component.html'
})

export class Sidebar {
    constructor(private _viewService:ViewService) {}

    private views:string[] = [];

    ngOnInit() {
        this.views = [];//this._viewService.getViewNames();
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
}
