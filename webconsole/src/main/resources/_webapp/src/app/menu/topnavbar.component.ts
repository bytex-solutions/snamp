import { Component, ChangeDetectorRef, ViewEncapsulation, ViewContainerRef } from '@angular/core';
import { CookieService } from 'angular2-cookie/core';
import { SnampLogService } from '../services/app.logService';

import {
  Modal,
  OneButtonPresetBuilder,
  TwoButtonPresetBuilder,
  PromptPresetBuilder
} from 'angular2-modal/plugins/bootstrap/index';

import { Overlay } from 'angular2-modal';
import { AbstractNotification } from "../services/model/notifications/abstract.notification";

@Component({
  selector: 'topnav-bar',
  providers: [ CookieService ],
  styleUrls: [ '../app.style.css' ],
  templateUrl: './topnavbar.component.html',
  encapsulation: ViewEncapsulation.None
})
export class TopNavBar {

    public logs:AbstractNotification[] = [];

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

    clickDetails(logEntry:AbstractNotification) {
       this.modal.alert()
           .size('lg')
           .title("Details for notification")
           .body(logEntry.htmlDetails())
           .isBlocking(false)
           .keyboard(27)
           .open();
    }

    removeMessage(log:AbstractNotification) {
        var liElement = $("#" + log.id);
        let _thisReference = this;
        liElement.slideUp("slow", function() {
          for (let i = 0; i < _thisReference.logs.length; i++) {
              if (_thisReference.logs[i].id == log.id) {
                  _thisReference.logs.splice(i, 1);
                  break;
              }
          }
        });
    }

    ngAfterViewInit() {
       this._snampLogService.getLogObs()
          .subscribe((newLog:AbstractNotification) => {
            this.logs.unshift(newLog);
            this.cd.detectChanges();
        });
    }
}
