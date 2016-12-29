/*
 * Angular 2 decorators and services
 */
import { Component, ViewEncapsulation } from '@angular/core';
import 'style!css!less!font-awesome-webpack/font-awesome-styles.loader!font-awesome-webpack/font-awesome.config.js';
import { AppState } from './app.service';

import { WebSocketClient } from './app.websocket';

import { NotificationsService, SimpleNotificationsComponent } from 'angular2-notifications';

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
  constructor(public appState: AppState, private _service: NotificationsService) {}

  public title: string = 'just a title';
  public content: string = 'just content';
  public type: string = 'success';

  public options = {
      timeOut: 4000,
      lastOnBottom: true,
      clickToClose: true,
      maxLength: 0,
      maxStack: 7,
      showProgressBar: true,
      pauseOnHover: true,
      preventDuplicates: false,
      preventLastDuplicates: 'visible',
      rtl: false,
      animate: 'scale',
      position: ['right', 'bottom']
  };

  private html = `<p>Test</p><p>Another test</p>`;

  removeAll() { this._service.remove() }

  ngAfterViewInit() {
    console.log('Initial App State', this.appState.state);

    this.ws = new WebSocketClient("ws://localhost:8181/snamp/console/events" );
    this.ws.getDataStream().subscribe(
        (msg)=> {
            let _json = JSON.parse(msg.data);
            console.log("next", msg.data, _json);

             new PNotify({
                 title: _json.level,
                 text: _json.message,
                 type: _json.level,
                 sound: false,
                 addclass: "stack-bottomright"
             })
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
