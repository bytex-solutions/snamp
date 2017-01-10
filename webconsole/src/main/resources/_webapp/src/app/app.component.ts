import { Component, ViewEncapsulation, ViewContainerRef } from '@angular/core';
import 'style!css!less!font-awesome-webpack/font-awesome-styles.loader!font-awesome-webpack/font-awesome.config.js';
import { LocalStorageService } from 'angular-2-local-storage';
import { WebSocketClient } from './app.websocket';
import { SnampLog, SnampLogService } from './app.logService';
import { Title }  from '@angular/platform-browser';

var PNotify = require("pnotify/src/pnotify.js");
require("pnotify/src/pnotify.mobile.js");
require("pnotify/src/pnotify.buttons.js");
require("pnotify/src/pnotify.desktop.js");

import { Overlay } from 'angular2-modal';
import {
  Modal,
  OneButtonPresetBuilder,
  TwoButtonPresetBuilder,
  PromptPresetBuilder
} from 'angular2-modal/plugins/bootstrap/index';


@Component({
  selector: 'app',
  encapsulation: ViewEncapsulation.None,
  styleUrls: ['./app.style.css'],
  templateUrl: './app.component.html',
  providers: [ Title ]
})
export class App {
  ws: WebSocketClient;
  constructor(overlay: Overlay,
              title:Title,
              vcRef: ViewContainerRef,
              private modal: Modal,
              private _snampLogService: SnampLogService) {
       title.setTitle("SNAMP web console");
       overlay.defaultViewContainer = vcRef;
  }

  public notificationCount:number = 0;
  public activeEvent:any = undefined;
  stack_bottomright:any = {"dir1": "up", "dir2": "left", "firstpos1": 25, "firstpos2": 25};

  ngAfterViewInit() {

    $(document).ready(function(){
        $('#overlay').fadeOut();
     });

    this.ws = new WebSocketClient("ws://localhost:8181/snamp/console/events" );

    this.ws.getDataStream().subscribe(
        (msg)=> {
            let _log:SnampLog = SnampLog.makeFromJson(JSON.parse(msg.data));
            this._snampLogService.pushLog(_log);

            // limit the notifications maximum count
            if (this.notificationCount > 3) {
              PNotify.removeAll();
              this.notificationCount = 0;
            }
            var notice = new PNotify({
                 title: _log.level,
                 text: _log.message  + "<a class='details'>Details</a>",
                 type: _log.level,
                 hide: false,
                 styling: 'bootstrap3',
                 addclass: "stack-bottomright",
                 stack: this.stack_bottomright
             });

             var _thisReference = this;
             notice.get().find('a.details').on('click', function() {
                 _thisReference.modal.alert()
                         .size('lg')
                         .title("Details for notification")
                         .body(_log.htmlDetails())
                         .isBlocking(false)
                         .keyboard(27)
                         .open()
             });

            this.notificationCount++;
        },
        (msg)=> {
            console.log("error", msg);
        },
        ()=> {
            console.log("complete");
        }
    );
  }

}
