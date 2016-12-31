/*
 * Angular 2 decorators and services
 */
import { Component, ViewEncapsulation } from '@angular/core';
import 'style!css!less!font-awesome-webpack/font-awesome-styles.loader!font-awesome-webpack/font-awesome.config.js';
import { AppState } from './app.service';

import { WebSocketClient } from './app.websocket';

var PNotify = require("pnotify/src/pnotify.js");
require("pnotify/src/pnotify.mobile.js");
require("pnotify/src/pnotify.buttons.js");
require("pnotify/src/pnotify.desktop.js");

/*
 * App Component
 * Top Level Component
 */
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
  constructor(public appState: AppState) {}

  public notificationCount:number = 0;
  public activeEvent:any = undefined;
  stack_bottomright:any = {"dir1": "up", "dir2": "left", "firstpos1": 25, "firstpos2": 25};

  ngAfterViewInit() {
    console.log('Initial App State', this.appState.state);

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
             new PNotify({
                 title: _title,
                 text: _json.message,
                 type: _json.level,
                 hide: false,
                 styling: 'bootstrap3',
                 addclass: "stack-bottomright",
                 stack: this.stack_bottomright
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
