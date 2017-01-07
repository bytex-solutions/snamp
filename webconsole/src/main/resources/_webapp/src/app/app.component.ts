import { Component, ViewEncapsulation, ViewContainerRef } from '@angular/core';
import 'style!css!less!font-awesome-webpack/font-awesome-styles.loader!font-awesome-webpack/font-awesome.config.js';
import { LocalStorageService } from 'angular-2-local-storage';
import { WebSocketClient } from './app.websocket';

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
  styleUrls: [
    './app.style.css'
  ],
  templateUrl: './app.component.html'
})
export class App {
  ws: WebSocketClient;
  constructor(overlay: Overlay,
              vcRef: ViewContainerRef,
              public modal: Modal,
              private localStorageService: LocalStorageService) {
       overlay.defaultViewContainer = vcRef;
  }

  public notificationCount:number = 0;
  public activeEvent:any = undefined;
  stack_bottomright:any = {"dir1": "up", "dir2": "left", "firstpos1": 25, "firstpos2": 25};

  ngAfterViewInit() {
    this.ws = new WebSocketClient("ws://localhost:8181/snamp/console/events" );

    this.ws.getDataStream().subscribe(
        (msg)=> {
            let _json = JSON.parse(msg.data);
            let _title: string = _json.level;

            // limit the notifications maximum count
            if (this.notificationCount > 5) {
              PNotify.removeAll();
              this.notificationCount = 0;
            }
            var notice = new PNotify({
                 title: _title,
                 text: _json.message  + "<a class='details'>Details</a>",
                 type: _json.level,
                 hide: false,
                 styling: 'bootstrap3',
                 addclass: "stack-bottomright",
                 stack: this.stack_bottomright
             });

             var _thisReference = this;
             let _details:string = "";
             _details += "<strong>Message: </strong>" + _json.message + "<br/>";
             _details += "<strong>Timestamp: </strong>" + _json.timeStamp + "<br/>";
             if (_json.stackTrace && _json.stackTrace.length > 5) {
                _details += "<strong>Stacktrace: </strong>" + _json.stackTrace + "<br/>";
             }
             _details += "<strong>Level: </strong>" + _json.level + "<br/>";
             if (_json.details) {
                _details += "<strong>Details</strong></br/>";
                for (let key in _json.details) {
                   _details += "<strong>" + key + ": </strong>" + _json.details[key] + "<br/>"
                }
             }

             notice.get().find('a.details').on('click', function() {
                 _thisReference.modal.alert()
                         .size('lg')
                         .title("Details for notification")
                         .body(_details)
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
