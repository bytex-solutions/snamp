import { Component, ViewContainerRef } from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Overlay } from 'angular2-modal';
import { Modal } from 'angular2-modal/plugins/vex';
import { AttributeInformation } from './model/attribute';

import { ChartService } from '../services/app.chartService';
import { Factory } from './model/objectFactory';
import { AbstractChart } from './model/abstract.chart';

import { ActivatedRoute } from '@angular/router';

import 'rxjs/add/operator/publishLast';
import 'rxjs/add/operator/cache';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/of';

import { NgGridConfig, NgGridItemEvent } from '../controls/nggrid/main';

import 'smartwizard';
import 'select2';


@Component({
  moduleId: module.id,
  templateUrl: './templates/dashboard.html',
  styleUrls: [ './templates/css/dashboard.css' ]
})
export class Dashboard {

    private http:ApiClient;

    timerId:any = undefined;

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

    public groupName:string = "";

    intervals:TimeInterval[] = TimeInterval.generateIntervals();

    timeInterval:TimeInterval = undefined;

    private gridConfig: NgGridConfig = <NgGridConfig>{
        'margins': [10],
        'draggable': true,
        'resizable': true,
        'max_cols': 14,
        'max_rows': 0,
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
          private _chartService:ChartService,
          private route: ActivatedRoute) {

        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;
        this.timeInterval = this.intervals[0];
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
            this.timeInterval = this.intervals[0];

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
            this.selectedComponent + "." + ((this.selectedMetric != undefined) ? this.selectedMetric.name : "") + "_" + this._charts.length;
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
        console.log("ng on init view for chart dashboard... ");
        let _thisReference:any = this;
        this.route.params
             .map(params => params['groupName'])
             .subscribe((gn) => {
                console.log("Component for group: ", gn);
                this.groupName = gn;
                this._charts = this._chartService.getChartsByGroupName(this.groupName);
                for (let i = 0; i < this._charts.length; i++) {
                    this._charts[i].draw();
                }
                this.timerId = setInterval(function(){
                    _thisReference._chartService.receiveChartDataForGroupName(gn);
                }, 1000);
             });
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

        let _thisReference:any = this;

        $(this.getSmartWizardIdentifier()).on("showStep", function(e, anchorObject, stepNumber, stepDirection) {
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
        });
         $('#overlay').fadeOut();
   }

   triggerShowInstances(event:any):void {
        let _select:any = $("#instancesSelect");
        let _thisReference:any = this;
        if (event == false) {
            _select.select2({
                placeholder: "Select instances from the dropdown",
                allowClear: true
            });
            _select.on('change', (e) => {
                _thisReference.onInstanceSelect($(e.target).val()); // no native actions on the selec2 componentс
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
        let chart:AbstractChart = Factory.create2dChart(this.selectedChartType, this.chartName, this.groupName, this.selectedComponent,
            _instances, this.selectedMetric);

        if (this.selectedChartType == "line") {
            chart.preferences["interval"] = this.timeInterval.id;
        }
        this._chartService.newChart(chart);
        this._charts = this._chartService.getChartsByGroupName(this.groupName);
        $("#addChartModal").modal("hide");

         let _thisReference:any = this;
         setTimeout(function() {
            chart.draw();
            _thisReference._chartService.saveDashboard();
         }, 400);
   }

   	onChangeStop(index: number, event: NgGridItemEvent): void {
   		this._charts[index].preferences["gridcfg"] = event;
   		this._chartService.saveDashboard();
   	}

   	removeChart(chartName:string):void {
   	    this._chartService.removeChart(chartName);
   	    this._charts = this._chartService.getChartsByGroupName(this.groupName);
   	}

    ngOnDestroy() {
        clearInterval(this.timerId);
    }

    isInstanceSelected(instance:string):boolean {
        return (this.selectedInstances.indexOf(instance) >= 0);
    }
}

class TimeInterval {
    public id:number = 0;
    public description:string = "";

    constructor(id:number, description:string) {
        this.id = id;
        this.description = description;
    }

    public static generateIntervals():TimeInterval[] {
        let value:TimeInterval[] = [];
        value.push(new TimeInterval(1, "1 minute"));
        value.push(new TimeInterval(5, "5 minutes"));
        value.push(new TimeInterval(15, "15 minutes"));
        value.push(new TimeInterval(60, "1 hour"));
        value.push(new TimeInterval(720, "12 hours"));
        value.push(new TimeInterval(1440, "24 hours"));
        return value;
    }
}

