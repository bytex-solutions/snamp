import { Component, OnInit } from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { Response } from '@angular/http';

import fileSaver = require("file-saver");

import 'rxjs/Rx' ;

@Component({
  moduleId: module.id,
  templateUrl: './templates/fullsave.html',
  styleUrls: [ './templates/css/fullsave.css' ]
})
export class FullSaveComponent implements OnInit {
  currentConfiguration:string;

  constructor(private http: ApiClient) {}

   ngOnInit():void {
        this.http.get(REST.CURRENT_CONFIG)
            .map((data:Response) => JSON.stringify(data.json(), null, 4))
            .subscribe((data) => {
                this.currentConfiguration = data;
            });
   }

   save():void {
       let blob = new Blob([this.currentConfiguration], {type: 'application/json'});
       let filename = 'configuration.json';
       fileSaver.saveAs(blob, filename);
   }

   load(event) {
      let _thisReference = this;
      let fileList: FileList = event.target.files;
        if(fileList.length > 0) {
             let file: File = fileList[0];
             let reader:any = new FileReader();

              // Read file into memory as UTF-8
              reader.readAsText(file, "UTF-8");

              // Handle progress, success, and errors
              reader.onload = function(evt:any){
                  let fileString:string = evt.target.result;
                  _thisReference.http.put(REST.CURRENT_CONFIG, fileString)
                      .map((response:Response) => response.text())
                      .subscribe((data) => {
                          console.debug("configuration has been upload successfully", data);
                          location.reload();
                      });
              };
              reader.onerror = function(evt:any){
                  console.debug("Error occured while loading file: ", evt);
              };
        }
   }
}