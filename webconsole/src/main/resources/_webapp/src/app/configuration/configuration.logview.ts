import { Component, OnInit } from '@angular/core';
import { SnampLogService } from '../services/app.logService';

import { LocalDataSource } from 'ng2-smart-table';
import { AbstractNotification } from "../services/model/notifications/abstract.notification";

@Component({
  moduleId: module.id,
  templateUrl: './templates/snamplogview.html'
})
export class SnampLogViewComponent implements OnInit {

    source: LocalDataSource;
    public rows = [];

     public settings = {
         columns: {
               savedTimestamp: {
                   title: 'Timestamp',
                   filter: false,
                   sortDirection: 'desc'
               },
               level: {
                 title: 'Level'
               },
               savedMessage: {
                 title: 'Message'
               },
               timestamp: {
                 title: 'Date and time',
                 filter: false
               },
               type: {
                  title: 'Type',
                  type: 'html'
               },
               savedDetails: {
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
            perPage: 20
         }
     };

    constructor(private _snampLogService:SnampLogService) {}

    ngOnInit() {
        this._snampLogService.getLogObs()
             .subscribe((newLog:AbstractNotification) => {
                 newLog.savedMessage = newLog.shortDescription();
                 newLog.savedDetails = newLog.htmlDetails();
                 newLog.savedTimestamp = newLog.timestamp.getTime();
                 if (this.source == undefined) {
                     this.source = new LocalDataSource(new Array(newLog));
                 } else {
                     this.source.add(newLog);
                 }
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

