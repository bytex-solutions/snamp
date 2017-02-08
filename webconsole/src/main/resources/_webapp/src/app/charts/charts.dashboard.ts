import { Component, OnInit, ViewContainerRef, ViewEncapsulation } from '@angular/core';
import { ApiClient, REST } from '../app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Overlay } from 'angular2-modal';
import { Modal } from 'angular2-modal/plugins/vex';
import { AttributeInformation } from './model/attribute';

import { ChartService } from '../app.chartService';
import { Factory } from './model/objectFactory';
import { AbstractChart } from './model/abstract.chart';

import 'rxjs/add/operator/publishLast';
import 'rxjs/add/operator/cache';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/of';

import { NgGrid, NgGridItem, NgGridConfig, NgGridItemConfig, NgGridItemEvent } from '../controls/nggrid/main';

import 'smartwizard';
import 'select2';


@Component({
  moduleId: module.id,
  templateUrl: './templates/dashboard.html',
  styleUrls: [ './templates/css/dashboard.css' ]
})
export class Dashboard {

        private http:ApiClient;

        components:Observable<string[]>;
        selectedComponent:string = "";

        metrics:Observable<AttributeInformation[]>;
        selectedMetric:AttributeInformation;

        instances:Observable<string[]>;
        selectedInstances:string[] = [];
        allInstances:string[] = [];
        selectedAllInstances:boolean = true;

        selectedChartType:string = "bar";

        chartName:string = "newChart";

        initialized = false;

        private _charts:AbstractChart[] = [];

        private gridConfig: NgGridConfig = <NgGridConfig>{
            'margins': [10],
            'draggable': true,
            'resizable': true,
            'max_cols': 10,
            'max_rows': 10,
            'visible_cols': 0,
            'visible_rows': 0,
            'min_cols': 1,
            'min_rows': 1,
            'col_width': 100,
            'row_height': 100,
            'cascade': 'left',
            'min_width': 50,
            'min_height': 50,
            'fix_to_grid': false,
            'auto_style': true,
            'auto_resize': false,
            'maintain_ratio': false,
            'prefer_new': true,
            'zoom_on_drag': false,
            'limit_to_screen': true
        };


        constructor(apiClient: ApiClient,
              overlay: Overlay,
              vcRef: ViewContainerRef,
              private modal: Modal,
              private _chartService:ChartService) {
        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;

        this._charts = this._chartService.getCharts();
   }

   appendChartClicked(type:string) {
        this.selectedChartType = type;
        this.initModal();
   }

   private initModal():void {
        // clean the data if the component was already initialized
        if (this.initialized) {
            // set all elements to the initial state
            this.selectedAllInstances = true;
            this.allInstances = [];
            this.selectedInstances = [];
            this.selectedMetric = undefined;
            this.selectedComponent = "";

            // fill components and selected component
            this.ngOnInit();

            // reset wizard
            $(this.getSmartWizardIdentifier()).off("showStep");
            $(this.getSmartWizardIdentifier()).smartWizard("reset");
            this.initWizard();

            if ($("#instancesSelect").data('select2')) {
                $("#instancesSelect").select2("destroy");
            }
        }
        // open the modal
        $("#addChartModal").modal("show");
        // and next time user adds the chart - we will reinit all the dialog
        this.initialized = true;
   }

   private updateChartName():void {
        this.chartName = this.selectedChartType + "." +
            this.selectedComponent + "." + ((this.selectedMetric != undefined) ? this.selectedMetric.name : "");
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
                            .map((res:Response) => { return <string[]>res.json()}).publishLast().refCount();
                this.instances.subscribe((data:string[]) => { this.allInstances = data});
            }
        });
   }

   ngAfterViewInit():void {
        var _thisReference = this;
        $(document).ready(function(){
             _thisReference.initWizard();
             let ch:any = _thisReference._charts;
             for (let i = 0; i < ch.length; i++) {
                ch[i].draw();
             }
        });
   }

   private initWizard():void {
        $(this.getSmartWizardIdentifier()).smartWizard({
            theme: 'arrows',
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });

        var _thisReference = this;

        $(this.getSmartWizardIdentifier()).on("showStep", function(e, anchorObject, stepNumber, stepDirection) {
            console.log("step direction: ", stepDirection);
            if (stepNumber == 3) {
                _thisReference.updateChartName();
            } else if (stepNumber == 2) {
                _thisReference.loadMetricsOnInstancesSelected();
            }
        });
   }

   onComponentSelect(event:any):void {
        this.instances = this.http.get(REST.CHART_INSTANCES(event))
            .map((res:Response) => { return <string[]>res.json()})
            .publishLast()
            .refCount();
        this.instances.subscribe((data:string[]) => { this.allInstances = data});
   }

    onInstanceSelect(event):void {
        this.selectedInstances = event;
    }

   private loadMetricsOnInstancesSelected():void {
        $('#overlay').fadeIn();
        let _instanceForSearchMetrics:string = ((this.selectedAllInstances) ? this.allInstances[0] : this.selectedInstances[0]);
        let _obsComponents = this.http.getIgnoreErrors(REST.CHART_METRICS_BY_COMPONENT(this.selectedComponent))
             .map((res:Response) => {
                 let _data:any = res.json();
                 let _values:AttributeInformation[] = [];
                 for (let i in _data) {
                     _values.push(new AttributeInformation(_data[i]));
                 }
                 return _values;
             }).catch((res:Response) => Observable.of([])).cache();

        let _obsInstances = this.http.getIgnoreErrors(REST.CHART_METRICS_BY_INSTANCE(_instanceForSearchMetrics))
            .map((res:Response) => {
                let _data:any = res.json();
                let _values:AttributeInformation[] = [];
                for (let i in _data) {
                    _values.push(new AttributeInformation(_data[i]));
                }
                return _values;
            }).catch((res:Response) => Observable.of([])).cache();

        this.metrics = Observable.forkJoin([_obsComponents, _obsInstances])
            .map((_data) => {
                let _returnData:AttributeInformation[] = [];
                // if one of input arrays is empty - return another one
                if (_data[1].length == 0 && _data[0].length > 0) {
                    return _data[0];
                }
                if (_data[0].length == 0 && _data[1].length > 0) {
                    return _data[1];
                }
                for (let i = 0; i < _data[0].length; i++) {
                    let _currentValue:AttributeInformation = _data[0][i];
                    for (let j = 0; j < _data[1].length; j++) {
                        if (_currentValue.name == _data[1][j].name) {
                            if (_currentValue.description == undefined) {
                                _currentValue.description = _data[1][j].description;
                            }
                            if (_currentValue.type == undefined) {
                                _currentValue.type = _data[1][j].type;
                            }
                            if (_currentValue.unitOfMeasurement == undefined) {
                                _currentValue.unitOfMeasurement = _data[1][j].unitOfMeasurement;
                            }
                        }
                    }
                    _returnData.push(_currentValue);
                }
                return _returnData;
            });

        // set auto selected first metric if the array is not empty
        this.metrics.subscribe((data:AttributeInformation[]) => {
            if (data && data.length > 0) {
                this.selectedMetric = data[0];
            }
        })
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
        let _instances:string[] = ((this.selectedAllInstances) ? this.allInstances : this.selectedInstances);
        let chart:AbstractChart = Factory.create2dChart(this.selectedChartType, this.chartName, this.selectedComponent,
            _instances, this.selectedMetric);
        this._chartService.newChart(chart);
        $("#addChartModal").modal("hide");

         var _thisReference = this;
         setTimeout(function() {
            chart.draw();
            _thisReference._chartService.saveDashboard();
         }, 400);
   }

   	onChangeStop(index: number, event: NgGridItemEvent): void {
   		this._charts[index].preferences["gridcfg"] = event;
   		this._chartService.saveDashboard();
   	}
}

