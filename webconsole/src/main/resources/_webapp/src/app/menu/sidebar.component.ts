import { Component } from '@angular/core';
import { ApiClient } from '../app.restClient';
import { Observable } from 'rxjs/Observable';

import 'rxjs/add/observable/of';

@Component({
  selector: 'side-bar',
  styleUrls: [ '../app.style.css' ],
  templateUrl: './sidebar.component.html'
})

export class Sidebar {
    constructor(private http:ApiClient) {}

    private views:Observable<Array<string>>;

    ngOnInit() {
        this.views = Observable.of(new Array<string>()).map((data) => { return ["View_1", "View_2", "View_3"]});
    }

    anchorClicked(event: MouseEvent) {
        var _thisReference = this;
        var $li = $('#' + event.srcElement.id.replace("chevron","li")).parent();
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
