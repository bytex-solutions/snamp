import { Component, OnInit, ViewContainerRef, ViewEncapsulation } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Overlay } from 'angular2-modal';
import { Modal } from 'angular2-modal/plugins/vex';

import { DragulaService } from 'ng2-dragula/ng2-dragula';

const Chart = require('chart.js')


@Component({
  moduleId: module.id,
  templateUrl: './templates/dashboard.html',
  styleUrls: ['./templates/css/dashboard.css', './templates/css/dragula.css']
})
export class Dashboard {

  private http:ApiClient;

  constructor(apiClient: ApiClient,
              overlay: Overlay,
              vcRef: ViewContainerRef,
              private modal: Modal,
              private dragulaService: DragulaService) {
        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;

        // do not remove when dropping
        dragulaService.setOptions('first-bag', {
          copy: true
        });

        let _data:any =  {
           labels:["dispatcher#0", "dispatcher#1", "dispatcher#2", "dispatcher#3", "dispatcher#4", "dispatcher#5"],
           datasets: [{
               label: '% of load',
               data: [42, 20, 3, 59, 88, 31],
               backgroundColor: [
                   'rgba(255, 99, 132, 0.2)',
                   'rgba(54, 162, 235, 0.2)',
                   'rgba(255, 206, 86, 0.2)',
                   'rgba(75, 192, 192, 0.2)',
                   'rgba(153, 102, 255, 0.2)',
                   'rgba(255, 159, 64, 0.2)'
               ],
               borderColor: [
                   'rgba(255,99,132,1)',
                   'rgba(54, 162, 235, 1)',
                   'rgba(255, 206, 86, 1)',
                   'rgba(75, 192, 192, 1)',
                   'rgba(153, 102, 255, 1)',
                   'rgba(255, 159, 64, 1)'
               ],
               borderWidth: 1
           }]
        };

       let _options:any = {
          scales: {
              yAxes: [{
                  ticks: {
                      beginAtZero:true
                  }
              }]
          }
       };

         dragulaService.drop.subscribe((value) => {
           console.log("drop: ", value);
           console.log("Type to be created: ", $(value[1]).attr("data-chart-type"));
           var _type = $(value[1]).attr("data-chart-type");
           var _divHolder = $("<div></div>").addClass("col-md-4");
           var _canvas = $("<canvas id='" + _type +"'></canvas>");
           _divHolder.append(_canvas);
           $(value[2]).append(_divHolder);
           var myChart = new Chart(_canvas, {
                type: _type,
                data: _data,
                options: _options
           });
           $(value[2]).find('div.thumbnail').remove();
        });
   }
}

