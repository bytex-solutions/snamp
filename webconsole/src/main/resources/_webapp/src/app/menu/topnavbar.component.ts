import { Component, ChangeDetectorRef, ViewEncapsulation, ViewContainerRef } from '@angular/core';
import { CookieService } from 'angular2-cookie/core';
import { SnampLog, SnampLogService } from '..//app.logService';

import {
  Modal,
  OneButtonPresetBuilder,
  TwoButtonPresetBuilder,
  PromptPresetBuilder
} from 'angular2-modal/plugins/bootstrap/index';

import { Overlay } from 'angular2-modal';

@Component({
  selector: 'topnav-bar',
  providers: [ CookieService ],
  styleUrls: [ '../app.style.css' ],
  templateUrl: './topnavbar.component.html',
  encapsulation: ViewEncapsulation.None
})
export class TopNavBar {

    public logs:SnampLog[] = [];

    constructor(overlay: Overlay,
                vcRef: ViewContainerRef,
                private _cookieService:CookieService,
                private _snampLogService:SnampLogService,
                private cd: ChangeDetectorRef,
                public modal: Modal) {
      overlay.defaultViewContainer = vcRef;
    }

    public clearCookie() {
      this._cookieService.removeAll();
      this._snampLogService.clear();
    }

    toggleClicked(event: MouseEvent) {
        var target = event.srcElement.id;
        var body = $('body');
        var menu = $('#sidebar-menu');
        if (body.hasClass('nav-md')) {
            menu.find('li.active ul').hide();
            menu.find('li.active').addClass('active-sm').removeClass('active');
        } else {
            menu.find('li.active-sm ul').show();
            menu.find('li.active-sm').addClass('active').removeClass('active-sm');
        }
        body.toggleClass('nav-md nav-sm');

    }

    clearAlerts() {
      this.logs = [];
    }

    clickDetails(logEntry:SnampLog) {
       this.modal.alert()
           .size('lg')
           .title("Details for notification")
           .body(SnampLog.htmlDetails(logEntry))
           .isBlocking(false)
           .keyboard(27)
           .open();
    }

    removeMessage(log:SnampLog) {
        var liElement = $("#" + log.id);
        liElement.slideUp("slow");
        for (let i = 0; i < this.logs.length; i++) {
            if (this.logs[i].id == log.id) {
                this.logs.splice(i, 1);
                break;
            }
        }
        liElement.closest("li.dropdown.unclosable").addClass("open");
    }

    ngAfterViewInit() {
       this._snampLogService.getLogObs()
          .subscribe((newLog:SnampLog) => {
            this.logs.unshift(newLog);
            this.cd.detectChanges();
        });

        // http://stackoverflow.com/questions/25089297/twitter-bootstrap-avoid-dropdown-menu-close-on-click-inside
        $(document).ready(function(){
            $('li.dropdown.unclosable a.dropdown-toggle').off();
            $('li.dropdown.unclosable a.dropdown-toggle').on('click', function (event) {
                $(this).parent().toggleClass('open');
            });

            $('body').on('click', function (e) {
                if (!$('li.dropdown.unclosable').is(e.target)
                    && $('li.dropdown.unclosable').has(e.target).length === 0
                    && $('.open').has(e.target).length === 0
                ) {
                    $('li.dropdown.unclosable').removeClass('open');
                }
            });

        });
    }
}
