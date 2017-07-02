import { Component, OnInit } from '@angular/core';
import { SnampLogService } from '../services/app.logService';

import { LocalDataSource } from 'ng2-smart-table';

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
            perPage: 7
        }
    };

    constructor(private _snampLogService:SnampLogService) {}

    ngOnInit():void {
        this._snampLogService.flushBuffer();
        this.source = new LocalDataSource(this._snampLogService.getAllLogs());
    }

    refreshLogs():void {
        this._snampLogService.flushBuffer();
        this.source = new LocalDataSource(this._snampLogService.getAllLogs());
        this.source.refresh();
    }

    clearAllLogs():void {
        $('#overlay').fadeIn();
        this._snampLogService.clear();
        this.source.empty();
        this.source.refresh();
        $('#overlay').fadeOut();
    }
}

