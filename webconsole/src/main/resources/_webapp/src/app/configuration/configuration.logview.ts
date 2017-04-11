import { Component, OnInit } from '@angular/core';
import { SnampLogService } from '../services/app.logService';

import { LocalDataSource } from 'ng2-smart-table';
import {AbstractNotification} from "../services/model/abstract.notification";

@Component({
  moduleId: module.id,
  templateUrl: './templates/snamplogview.html'
})
export class SnampLogViewComponent implements OnInit {

    source: LocalDataSource;
    public rows = [];

     public settings = {
         columns: {
           level: {
             title: 'Level'
           },
           message: {
             title: 'Message'
           },
           localTime: {
             title: 'Timestamp',
             filter: false,
             sortDirection: 'desc '
           },
           shortDetailsHtml: {
              title: 'Details',
              type: 'html'
           }
        },
         actions: {
            add: false,
            edit: false,
            delete: false
         },
         pager: {
            perPage: 8
         }
     };

    constructor(private _snampLogService:SnampLogService) {}

    ngOnInit() {
        this._snampLogService.getLogObs()
             .subscribe((newLog:AbstractNotification) => {
                  this.source.add(newLog);
                  this.source.refresh();
           });
     }

    clearAllLogs() {
      $('#overlay').fadeIn();
      this._snampLogService.clear();
      this.source.empty();
      this.source.refresh();
      $('#overlay').fadeOut();
   }
}