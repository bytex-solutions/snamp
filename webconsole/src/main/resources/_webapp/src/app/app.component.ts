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

  ngAfterViewInit() {
    console.log('Initial App State', this.appState.state);

    let stack_bottomright:any = {"dir1": "up", "dir2": "left", "firstpos1": 25, "firstpos2": 25};

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
                 stack: stack_bottomright
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


  date_formats:any = {
      past: [
        { ceiling: 60, text: "$seconds seconds ago" },
        { ceiling: 3600, text: "$minutes minutes ago" },
        { ceiling: 86400, text: "$hours hours ago" },
        { ceiling: 2629744, text: "$days days ago" },
        { ceiling: 31556926, text: "$months months ago" },
        { ceiling: null, text: "$years years ago" }
      ],
      future: [
        { ceiling: 60, text: "in $seconds seconds" },
        { ceiling: 3600, text: "in $minutes minutes" },
        { ceiling: 86400, text: "in $hours hours" },
        { ceiling: 2629744, text: "in $days days" },
        { ceiling: 31556926, text: "in $months months" },
        { ceiling: null, text: "in $years years" }
      ]
    };

    time_units:any = [
      [31556926, 'years'],
      [2629744, 'months'],
      [86400, 'days'],
      [3600, 'hours'],
      [60, 'minutes'],
      [1, 'seconds']
    ];

  humanized_time_span(date:Date, ref_date?:Date):string {
    date = new Date(date);
    ref_date = ref_date ? new Date(ref_date) : new Date();
    var seconds_difference = (ref_date.getTime() - date.getTime()) / 1000;

    var tense = 'past';
    if (seconds_difference < 0) {
      tense = 'future';
      seconds_difference = 0-seconds_difference;
    }
    return this.render_date(this.get_format(seconds_difference, tense), seconds_difference);
  }

  get_format(seconds_difference:number, tense:string):any {
    for (var i=0; i<this.date_formats[tense].length; i++) {
      if (this.date_formats[tense][i].ceiling == null || seconds_difference <= this.date_formats[tense][i].ceiling) {
        return this.date_formats[tense][i];
      }
    }
    return null;
  }

  get_time_breakdown(seconds_difference:number):any {
    var seconds = seconds_difference;
    var breakdown = {};
    for(var i=0; i<this.time_units.length; i++) {
      var occurences_of_unit = Math.floor(seconds / this.time_units[i][0]);
      seconds = seconds - (this.time_units[i][0] * occurences_of_unit);
      breakdown[this.time_units[i][1]] = occurences_of_unit;
    }
    return breakdown;
  }

  render_date(date_format:any, seconds_difference:number):any {
    var breakdown = this.get_time_breakdown(seconds_difference);
    var time_ago_text = date_format.text.replace(/\$(\w+)/g, function() {
      return breakdown[arguments[1]];
    });
    return this.depluralize_time_ago_text(time_ago_text, breakdown);
  }

  depluralize_time_ago_text(time_ago_text:string, breakdown:any):any {
    for(var i in breakdown) {
      if (breakdown[i] == 1) {
        var regexp = new RegExp("\\b"+i+"\\b");
        time_ago_text = time_ago_text.replace(regexp, function() {
          return arguments[0].replace(/s\b/g, '');
        });
      }
    }
    return time_ago_text;
  }

}
