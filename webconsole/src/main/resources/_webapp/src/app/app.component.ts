/*
 * Angular 2 decorators and services
 */
import { Component, ViewEncapsulation } from '@angular/core';
import 'style!css!less!font-awesome-webpack/font-awesome-styles.loader!font-awesome-webpack/font-awesome.config.js';
import { AppState } from './app.service';
import { CookieService } from 'angular2-cookie/core';

import { WebSocketClient } from './app.websocket';

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

  constructor(public appState: AppState, private _cookieService:CookieService) {
     //this.ws = new WebSocketClient("ws://localhost:8181/snamp/console/events");
  }

  ngOnInit() {
    console.log('Initial App State', this.appState.state);
    this.ws = new WebSocketClient("ws://localhost:8181/snamp/console/events" );
    this.ws.getDataStream().subscribe(
        (msg)=> {
            console.log("next", msg.data);
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
