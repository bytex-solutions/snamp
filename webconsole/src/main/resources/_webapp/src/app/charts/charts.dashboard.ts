import { Component, OnInit, ViewContainerRef, ViewEncapsulation } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { Overlay } from 'angular2-modal';
import { Modal } from 'angular2-modal/plugins/vex';

import { DragulaService } from 'ng2-dragula/ng2-dragula';

import 'rxjs/add/operator/publishLast';

const Chart = require('chart.js')
import 'smartwizard';
import 'select2';


@Component({
  moduleId: module.id,
  templateUrl: './templates/dashboard.html',
  styleUrls: ['./templates/css/dashboard.css', './templates/css/dragula.css']
})
export class Dashboard {

        private http:ApiClient;

        components:Observable<string[]>;
        selectedComponent:string = "";

        metrics:Observable<AttributeInformation[]>;
        selectedMetric:string = "";

        instances:Observable<string[]>;
        selectedAllInstances:boolean = true;

        selectedChartType:string = "bar";


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
           this.selectedChartType = _type;
           /*
           var _divHolder = $("<div></div>").addClass("col-md-4");
           var _canvas = $("<canvas id='" + _type +"'></canvas>");
           _divHolder.append(_canvas);
           $(value[2]).append(_divHolder);
           var myChart = new Chart(_canvas, {
                type: _type,
                data: _data,
                options: _options
           });

           */
           $(value[2]).find('div.thumbnail').remove();
           $("#addChartModal").modal("show");
        });
   }

   ngOnInit():void {
        this.components = this.http.get(REST.CHART_COMPONENTS)
            .map((res:Response) => { return <string[]>res.json()})
            .publishLast().refCount(); // http://stackoverflow.com/questions/36271899/what-is-the-correct-way-to-share-the-result-of-an-angular-2-http-network-call-in
        this.components.subscribe((data:string[]) => {
            if (data && data.length > 0) {
                this.selectedComponent = data[0];
                // load instances as well - if we autoselect a component
                this.instances = this.http.get(REST.CHART_INSTANCES(this.selectedComponent))
                            .map((res:Response) => { return <string[]>res.json()});
            }
        });
   }

   ngAfterViewInit():void {
        var _thisReference = this;
        $(document).ready(function(){
             _thisReference.initWizard();
        });
   }

   private initWizard():void {
        $(this.getSmartWizardIdentifier()).smartWizard({
            theme: 'arrows',
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });
   }

   onComponentSelect(event:any):void {
        this.instances = this.http.get(REST.CHART_INSTANCES(event))
            .map((res:Response) => { return <string[]>res.json()});
   }

   onInstanceSelect(event:any):void {
        $('#overlay').fadeIn();
        this.metrics = this.http.get(REST.CHART_METRICS(event))
            .map((res:Response) => {
                let _data:any = res.json();
                let _values:AttributeInformation[] = [];
                for (let i in _data) {
                    _values.push(new AttributeInformation(_data[i]));
                }
                return _values;
            });
         $('#overlay').fadeOut();
   }

   triggerShowInstances(event:any):void {
        var _select = $("#instancesSelect");
        var _thisReference = this;
        if (event == false) {
            _select.select2({
                placeholder: "Select instances from the dropdown",
                allowClear: true
            });
            _select.on('change', (e) => {
                _thisReference.onInstanceSelect($(e.target).val());
             });
            _select.fadeIn("fast");
        } else {
            _select.fadeOut("fast", function(){
                _select.select2("destroy");
            });
        }
   }


    private getSmartWizardIdentifier():string {
      return "#smartwizardForChart";
    }

   addChartToDashboard():void {
        $(this.getSmartWizardIdentifier()).smartWizard("reset");
        $("#addChartModal").modal("hide");
        this.initWizard();
   }
}

class AttributeInformation {
    public name:string = "undefined";
    public unitOfMeasurement:string = "undefined";
    public type:string = "undefined";

    constructor(_json:any) {
        if (_json["name"] != undefined) {
            this.name = _json["name"];
        }
        if (_json["unitOfMeasurement"] != undefined) {
            this.unitOfMeasurement = _json["unitOfMeasurement"];
        }
        if (_json["type"] != undefined) {
            this.type = _json["type"];
        }
    }
}

