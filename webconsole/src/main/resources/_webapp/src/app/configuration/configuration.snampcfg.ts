import { Component, OnInit, ViewContainerRef, ViewEncapsulation } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { SnampLog, SnampLogService } from '../app.logService';

import { Overlay } from 'angular2-modal';
import { Modal } from 'angular2-modal/plugins/vex';

import { Ng2SmartTableModule, LocalDataSource } from 'ng2-smart-table';

@Component({
  moduleId: module.id,
  templateUrl: './templates/snampcfg.html',
  styleUrls: ['./templates/css/snampcfg.css']
})
export class SnampCfgComponent implements OnInit {

  private http:ApiClient;
  public selectedComponent:SnampComponent;
  public components:SnampComponent[] = [];
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

  constructor(apiClient: ApiClient,
              overlay: Overlay,
              vcRef: ViewContainerRef,
              private modal: Modal,
              private _snampLogService:SnampLogService) {
        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;
   }

   ngOnInit() {
      this.http.get(REST.AVAILABLE_COMPONENT_LIST)
        .map((res:Response) => res.json())
        .subscribe(data => {
            for (let i = 0; i < data.length; i++) {
              this.components.push(new SnampComponent(data[i]));
            }
            if (this.components.length > 0) {
              this.selectedComponent = this.components[0];
            }
        });

        this.source = new LocalDataSource(this._snampLogService.getAllLogsJSON());
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

   selectComponent(selected:SnampComponent) {
      this.selectedComponent = selected;
   }

   startComponent(selected:SnampComponent) {
      $('#overlay').fadeIn();
      this.http.post(REST.ENABLE_COMPONENT(selected.class, selected.type), "")
       .map((res:Response) => res.text())
       .subscribe(data => {
          console.log("started " + selected.type + " component. result from server is " + data);
          if (data == "true") {
              selected.state = "ACTIVE";
              for (let i = 0; i < this.components.length; i++) {
                if (this.components[i].type == selected.type) {
                  this.components[i] = selected;
                  break;
                }
              }
          } else {
            console.log("Could not start component " + selected.type + " - server responded false");
          }
          $('#overlay').fadeOut();
       })
   }

   stopComponent(selected:SnampComponent) {
      $('#overlay').fadeIn();
      this.http.post(REST.DISABLE_COMPONENT(selected.class, selected.type), "")
       .map((res:Response) => res.text())
       .subscribe(data => {
          console.log("stopped " + selected.type + " component. result from server is " + data);
          if (data == "true") {
               selected.state = "RESOLVED";
               for (let i = 0; i < this.components.length; i++) {
                 if (this.components[i].type == selected.type) {
                   this.components[i] = selected;
                   break;
                 }
               }
          } else {
            console.log("Could not stop component " + selected.type + " - server responded false");
          }
          $('#overlay').fadeOut();
       })
   }
}


class SnampComponent {
  public name:string = "";
  public description:string = "";
  public state:string = "";
  public version:string = "";
  public type:string = "";
  public class:string = "";
  constructor(parameters:any) {
    if (parameters["name"] != undefined) {
        this.name = parameters["name"];
    }
    if (parameters["description"] != undefined) {
        this.description = parameters["description"];
    }
    if (parameters["state"] != undefined) {
        this.state = parameters["state"];
    }
    if (parameters["version"] != undefined) {
        this.version = parameters["version"];
    }
    if (parameters["type"] != undefined) {
        this.type = parameters["type"];
    }
    if (parameters["class"] != undefined) {
        this.class = parameters["class"];
    }
  }
}
