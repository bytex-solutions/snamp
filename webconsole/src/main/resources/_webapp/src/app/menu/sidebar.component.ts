import { Component } from '@angular/core';

@Component({
  selector: 'side-bar',
  styleUrls: [ '../app.style.css' ],
  templateUrl: './sidebar.component.html'
})

export class Sidebar {
    constructor() {}

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
