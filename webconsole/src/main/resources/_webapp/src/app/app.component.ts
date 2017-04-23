import 'style!css!less!font-awesome-webpack/font-awesome-styles.loader!font-awesome-webpack/font-awesome.config.js';

import { Component, ViewEncapsulation, ViewContainerRef } from '@angular/core';
import { SnampLogService } from './services/app.logService';
import { Title }  from '@angular/platform-browser';
import { $WebSocket } from 'angular2-websocket/angular2-websocket';
import { Router } from '@angular/router';
import { Overlay } from 'angular2-modal';
import { Modal } from 'angular2-modal/plugins/bootstrap';
import { AbstractNotification } from "./services/model/notifications/abstract.notification";
import { NotificationFactory } from "./services/model/notifications/factory";
import { UserProfileService } from "./services/app.user.profile";

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
                private _profile:UserProfileService,
                private _router: Router) {
        title.setTitle("SNAMP web console");
        overlay.defaultViewContainer = vcRef;
    }

    public notificationCount:number = 0;
    private stack_bottomright:any = {"dir1": "up", "dir2": "left", "firstpos1": 25, "firstpos2": 25};

    ngAfterViewInit() {
        console.log("User profile decoded is: ", this._profile.decodeProfile());
        this.ws = new $WebSocket("ws://localhost:8181/snamp/console/events", [],
            {initialTimeout: 500, maxTimeout: 300000, reconnectIfNotNormalClose: true});

        this.ws.getDataStream()
            .map((msg) => { return JSON.parse(msg.data); })
            .subscribe(
                (msg)=> {
                    let _log:AbstractNotification = NotificationFactory.makeFromJson(msg);
                    this._snampLogService.pushLog(_log);

                    // do not show notifications in case we are inside of snamp configuration (there is a table with notifications)
                    if (this._router.url.indexOf('/logview') < 0) {

                        // limit the notifications maximum count
                        if (this.notificationCount > 3) {
                            PNotify.removeAll();
                            this.notificationCount = 0;
                        }
                        if (!document.hidden) {
                            let notice = new PNotify({
                                title: _log.level,
                                text: _log.shortDescription() + "<a class='details'>Details</a>",
                                type: _log.level,
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
                                    .body(_log.htmlDetails())
                                    .isBlocking(false)
                                    .keyboard(27)
                                    .open()
                            });

                            this.notificationCount++;
                        }
                    }
                },
                (msg)=> {
                    console.log("Error occurred while listening to the socket: ", msg);
                },
                ()=> {
                    console.log("Socket connection has been completed");
                }
            );
    }
}
