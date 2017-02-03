import { Component, OnInit } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { SnampLog, SnampLogService } from '../app.logService';

import { Ng2SmartTableModule, LocalDataSource } from 'ng2-smart-table';

@Component({
  moduleId: module.id,
  templateUrl: './templates/snamplogview.html'
})
export class SnampLogViewComponent implements OnInit {

  private http:ApiClient;
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

  constructor(apiClient: ApiClient, private _snampLogService:SnampLogService) {
        this.http = apiClient;
   }

   ngOnInit() {
        this._snampLogService.getLogObs()
             .subscribe((newLog:SnampLog) => {
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