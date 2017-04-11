import { Component, ViewEncapsulation, ViewContainerRef } from '@angular/core';
import 'style!css!less!font-awesome-webpack/font-awesome-styles.loader!font-awesome-webpack/font-awesome.config.js';
import { SnampLogService } from './services/app.logService';
import { Title }  from '@angular/platform-browser';

import { $WebSocket } from 'angular2-websocket/angular2-websocket';
import { Router } from '@angular/router';

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
import {AbstractNotification} from "./services/model/abstract.notification";
import {Factory} from "./services/model/factory";


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
  public activeEvent:any = undefined;
  stack_bottomright:any = {"dir1": "up", "dir2": "left", "firstpos1": 25, "firstpos2": 25};

  ngAfterViewInit() {

    $(document).ready(function(){
        // open the current active element on the left side panel
        setTimeout(function() {
          $('li.activeLi').parents('li').addClass('active');
          $('li.activeLi').parents("ul").slideDown("slow");
         }, 500)
     });

    this.ws = new $WebSocket("ws://localhost:8181/snamp/console/events", [],
        {initialTimeout: 500, maxTimeout: 300000, reconnectIfNotNormalClose: true});

    this.ws.getDataStream()
        .map((msg) => { console.log(msg); return JSON.parse(msg.data); })
        .filter((msg) => msg['@messageType'] == 'log')
        .subscribe(
          (msg)=> {
              let _log:AbstractNotification = Factory.makeFromJson(msg);
              this._snampLogService.pushLog(_log);

              // do not show notifications in case we are inside of snamp configuration (there is a table with notifications)
              if (this._router.url.indexOf('/snampcfg') < 0) {

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
              }
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
