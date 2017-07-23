import 'style!css!less!font-awesome-webpack/font-awesome-styles.loader!font-awesome-webpack/font-awesome.config.js';

import { Component, ViewEncapsulation, ViewContainerRef } from '@angular/core';
import { SnampLogService } from './services/app.logService';
import { Title }  from '@angular/platform-browser';
import { $WebSocket } from 'angular2-websocket/angular2-websocket';
import { Router } from '@angular/router';
import { Overlay } from 'angular2-modal';
import { Modal } from 'angular2-modal/plugins/bootstrap';
import { AbstractNotification } from "./services/model/notifications/abstract.notification";

const PNotify = require("pnotify/src/pnotify.js");
require("pnotify/src/pnotify.mobile.js");
require("pnotify/src/pnotify.buttons.js");
require("pnotify/src/pnotify.desktop.js");

@Component({
    selector: 'app',
    encapsulation: ViewEncapsulation.None,
    styleUrls: ['./app.style.css'],
    templateUrl: './app.component.html',
    providers: [ Title ]
})
export class App {
    ws: $WebSocket;
    constructor(overlay: Overlay,
                title:Title,
                vcRef: ViewContainerRef,
                private modal: Modal,
                private _snampLogService: SnampLogService,
                private _router: Router) {
        title.setTitle("SNAMP web console");
        overlay.defaultViewContainer = vcRef;
    }

    public notificationCount:number = 0;
    private stack_bottomright:any = {"dir1": "up", "dir2": "left", "firstpos1": 25, "firstpos2": 25};

    ngAfterViewInit() {
       this._snampLogService.getLogObs().subscribe((log:AbstractNotification) => {
           if (this._router.url.indexOf('/logview') < 0) {

               // limit the notifications maximum count
               if (this.notificationCount > 3) {
                   PNotify.removeAll();
                   this.notificationCount = 0;
               }
               if (!document.hidden && this._snampLogService.displayAlerts) {
                   let notice = new PNotify({
                       title: log.level,
                       text: log.shortDescription() + "<a class='details'>Details</a>",
                       type: log.level,
                       hide: false,
                       styling: 'bootstrap3',
                       addclass: "stack-bottomright",
                       animate_speed: "fast",
                       stack: this.stack_bottomright
                   });

                   let _thisReference = this;
                   notice.get().find('a.details').on('click', function () {
                       _thisReference.modal.alert()
                           .size('lg')
                           .title("Details for notification")
                           .body(log.htmlDetails())
                           .isBlocking(false)
                           .keyboard(27)
                           .open()
                   });

                   this.notificationCount++;
               }
           }
       });
    }
}
