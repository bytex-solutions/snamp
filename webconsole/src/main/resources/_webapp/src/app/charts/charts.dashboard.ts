import { Component, ViewContainerRef, ChangeDetectorRef } from '@angular/core';
import { ApiClient, REST } from '../services/app.restClient';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Overlay } from 'angular2-modal';
import { AttributeInformation } from './model/attribute';
import { ChartService } from '../services/app.chartService';
import { Factory } from './model/objectFactory';
import { AbstractChart } from './model/abstract.chart';
import { PanelOfAttributeValues } from "./model/charts/panel.attributes.values";
import { NgGridConfig, NgGridItemEvent } from '../controls/nggrid/main';
import { ActivatedRoute } from '@angular/router';
import { ResourceGroupHealthStatusChart } from "./model/charts/resource.group.health.status";
import { SeriesBasedChart, DescriptionIdClass } from "./model/abstract.line.based.chart";

import 'rxjs/add/operator/publishLast';
import 'rxjs/add/operator/cache';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/of';

import 'smartwizard';
import 'select2';
import { ScalingRateChart } from "./model/scaling.rate.chart";
import {ChartOfAttributeValues} from "./model/abstract.chart.attributes.values";
import {TwoDimensionalChart} from "./model/two.dimensional.chart";
import {AttributeValueAxis} from "./model/axis/attribute.value.axis";
import {TwoDimensionalChartOfAttributeValues} from "./model/abstract.2d.chart.attributes.values";
import {VotingResultChart} from "./model/charts/voting.result.chart";

@Component({
    moduleId: module.id,
    templateUrl: './templates/dashboard.html',
    styleUrls: [ './templates/css/dashboard.css' ]
})
export class Dashboard {

    private timerId:any = undefined;

    private components:Observable<string[]>;
    private selectedComponent:string = "";

    private metrics:Observable<AttributeInformation[]>;
    private selectedMetric:AttributeInformation;

    private instances:Observable<string[]>;
    private selectedInstances:string[] = [];
    private allInstances:string[] = [];
    private selectedAllInstances:boolean = true;
    private selectedRateMetrics:string[] = [];

    private selectedChartType:string = "";

    private chartName:string = "newChart";

    private _charts:AbstractChart[] = [];

    private groupName:string = "";

    private intervals:DescriptionIdClass[] = SeriesBasedChart.generateIntervals();
    private rateIntervals:DescriptionIdClass[] = SeriesBasedChart.generateRateIntervals();
    private rateMetrics:DescriptionIdClass[] = ScalingRateChart.prepareRareMetrics();

    private timeInterval:DescriptionIdClass = undefined;
    private rateInterval:DescriptionIdClass = undefined;

    private static select2Id:string = "#instancesSelect";
    private static chartModalId:string = "#addChartModal";
    private static wizardId:string = "#smartwizardForChart";
    private static rateMetricSelect2Id:string = "#rateMetricSelect";

    private currentChart:AbstractChart = undefined;

    private gridConfig: NgGridConfig = <NgGridConfig> {
        'margins': [10],
        'draggable': true,
        'resizable': true,
        'max_cols': 50,
        'max_rows': 0,
        'visible_cols': 0,
        'visible_rows': 0,
        'min_cols': 1,
        'min_rows': 1,
        'min_width': 10,
        'min_height': 10,
        'col_width': 20,
        'row_height': 20,
        'cascade': 'up',
        'fix_to_grid': true  ,
        'auto_style': true,
        'auto_resize': true,
        'maintain_ratio': false,
        'prefer_new': false,
        'zoom_on_drag': false,
        'limit_to_screen': true
    };


    constructor(private http: ApiClient,
                overlay: Overlay,
                vcRef: ViewContainerRef,
                private _chartService:ChartService,
                private cd: ChangeDetectorRef,
                private route: ActivatedRoute) {

        overlay.defaultViewContainer = vcRef;
        this.timeInterval = this.intervals[0];
        this.rateInterval = this.rateIntervals[0];
    }

    appendChartClicked(type:string) {
        this.selectedChartType = type;
        this.cd.detectChanges(); // draw the modal html
        this.initNewChart();
    }

    private initNewChart():void {

        // set all elements to the initial state
        this.selectedAllInstances = true;
        this.allInstances = [];
        this.selectedInstances = [];
        this.selectedMetric = undefined;
        this.selectedComponent = "";
        this.timeInterval = this.intervals[0];
        this.selectedRateMetrics = [];
        this.rateInterval = this.rateIntervals[0];
        this.currentChart = undefined;

        // fill components and selected component
        this.ngOnInit();

        // init modal components and show modal itself
        this.initModal();
    }

    modifyChart(chart:AbstractChart):void {
        // make this chart as current (for further saving it, redrawing and switching button to "save the chart")
        this.currentChart = chart;

        // prefill instances from the existing chart
        if (chart instanceof ChartOfAttributeValues && chart.resources != undefined && chart.resources.length > 0) {
            this.selectedAllInstances = false;
            this.selectedInstances = chart.resources;
        } else {
            this.selectedAllInstances = true;
            this.selectedInstances = [];
        }

        // fill the group (I used property check because some charts have 'group' property defined in different places - fix@todo)
        if (chart['group'] != undefined && chart['group'].length > 0) {
            this.selectedComponent = chart['group'];
        } else {
            this.selectedComponent = "";
        }

        // fill the rate metric and rate intervals from the chart
        if (chart instanceof ScalingRateChart) {
            this.selectedRateMetrics =  chart.metrics;
            this.rateInterval = this.rateIntervals.filter(element => element.additionalId == chart.interval)[0];
        } else {
            this.selectedRateMetrics = [];
            this.rateInterval = this.intervals[0];
        }

        // fill the time interval if the chart belongs to series based charts
        if (chart instanceof SeriesBasedChart) {
            this.timeInterval = this.rateIntervals.filter(element => element.id == chart.preferences["interval"])[0];
        } else {
            this.timeInterval = this.intervals[0];
        }

        // fill the selected metric if the chart has metric in one of its axes
        if (chart instanceof TwoDimensionalChartOfAttributeValues) {
            this.selectedMetric = chart.getSourceAttribute();
        } else {
            this.selectedMetric = undefined;
        }

        // make sure the front end has received the changes
        this.cd.detectChanges();

        // init modal components and show the modal
        this.initModal();
    }

    private initModal():void {
        // reset wizard
        $(Dashboard.wizardId).off("showStep");
        $(Dashboard.wizardId).smartWizard("reset");
        this.initWizard();

        if ($(Dashboard.select2Id).data('select2')) {
            $(Dashboard.select2Id).select2("destroy");
        }

        // reset rate metric select2 component
        let _thisReference = this;
        if ($(Dashboard.rateMetricSelect2Id).data('select2')) {
            $(Dashboard.rateMetricSelect2Id).select2("destroy");
        }
        $(Dashboard.rateMetricSelect2Id).select2({
            placeholder: "Select rate metrics from the dropdown",
            width: '100%',
            allowClear: true
        });
        $(Dashboard.rateMetricSelect2Id).on('change', (e) => {
            _thisReference.onRateMetricSelect($(e.target).val());
        });

        // open the modal
        $(Dashboard.chartModalId).modal("show");
    }

    private updateChartName():void {
        this.chartName = this.selectedChartType + "." +
            (this.selectedComponent != "" ? this.selectedComponent  + "." : "") +
            ((this.selectedMetric != undefined) ? this.selectedMetric.name : "") + "_" +
            this._charts.length;
    }

    ngOnInit():void {
        this.components = this.http.get(REST.GROUPS_WEB_API)
            .map((res:Response) => { return <string[]>res.json()})
            .publishLast().refCount(); // http://stackoverflow.com/questions/36271899/what-is-the-correct-way-to-share-the-result-of-an-angular-2-http-network-call-in
        this.components.subscribe((data:string[]) => {
            if (data && data.length > 0) {
                this.selectedComponent = data[0];
                // load instances as well - if we autoselect a component
                this.instances = this.http.get(REST.GROUPS_RESOURCE_BY_COMPONENT_NAME(this.selectedComponent))
                    .map((res:Response) => { return <string[]>res.json()}).publishLast().refCount();
                this.instances.subscribe((data:string[]) => { this.allInstances = data});
            } else {
                this.selectedComponent = "";
                this.instances = this.http.get(REST.GROUPS_RESOURCES)
                    .map((res:Response) => { return <string[]>res.json()}).publishLast().refCount();
                this.instances.subscribe((data:string[]) => { this.allInstances = data});
            }
        });
    }

    ngAfterViewInit():void {
        let _thisReference:any = this;
        this.route.params
            .map(params => params['groupName'])
            .subscribe((gn) => {
                this.groupName = gn;
                this._charts = this._chartService.getChartsByGroupName(this.groupName);
                this.cd.detectChanges();
                for (let i = 0; i < this._charts.length; i++) {
                    this._charts[i].draw();
                }
                this.timerId = setInterval(function(){
                    _thisReference._chartService.receiveChartDataForGroupName(gn);
                }, 1500);
            });
    }

    private initWizard():void {
        let _thisReference:any = this;
        let _hidden:number[] =  _thisReference.getHiddenSteps();

        $(Dashboard.wizardId).smartWizard({
            theme: 'arrows',
            hiddenSteps: _hidden,
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });

        let _elements:any = $(".nav.nav-tabs.step-anchor").find("li");
        for (let i = 0; i < _elements.length; i++) {
            if (_hidden.indexOf(i) >= 0) {
                $(_elements[i]).addClass("hidden");
            } else {
                $(_elements[i]).removeClass("hidden");
            }
        }

        $(Dashboard.wizardId).on("showStep", function(e, anchorObject, stepNumber, stepDirection) {
            _thisReference.cd.detectChanges();
            if (stepNumber == 3 && _thisReference.isNewChart()) {
                _thisReference.updateChartName();
            } else if (stepNumber == 2 && stepDirection == "forward") {
                _thisReference.loadMetricsOnInstancesSelected();
            } else if (stepNumber == 1) {
                if (stepDirection == "forward" && _thisReference.isNewChart()) {
                    _thisReference.selectedAllInstances = true;
                    _thisReference.selectedInstances = [];
                }
                _thisReference.triggerShowInstances(_thisReference.selectedAllInstances);
            }
        });
    }

    onComponentSelect(event:any):void {
        let _endpoint:string = event == "" ? REST.GROUPS_RESOURCES : REST.GROUPS_RESOURCE_BY_COMPONENT_NAME(event);
        this.instances = this.http.get(_endpoint)
            .map((res:Response) => { return <string[]>res.json()})
            .publishLast()
            .refCount();
        this.instances.subscribe((data:string[]) => { this.allInstances = data});
    }

    private onInstanceSelect(event):void {
        this.selectedInstances = event;
    }

    private onRateMetricSelect(event):void {
        this.selectedRateMetrics = event;
    }

    private loadMetricsOnInstancesSelected():void {
        $('#overlay').fadeIn();
        let _instanceForSearchMetrics:string = ((this.selectedAllInstances) ? this.allInstances[0] : this.selectedInstances[0]);
        let _obsComponents = this.selectedComponent == "" ? Observable.of([]) : this.http.get(REST.CHART_METRICS_BY_COMPONENT(this.selectedComponent))
            .map((res:Response) => {
                let _data:any = res.json();
                let _values:AttributeInformation[] = [];
                for (let i in _data) {
                    _values.push(new AttributeInformation(_data[i]));
                }
                return _values;
            }).catch((res:Response) => Observable.of([])).cache();

        let _obsInstances = this.http.get(REST.CHART_METRICS_BY_INSTANCE(_instanceForSearchMetrics))
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
        let _select:any = $(Dashboard.select2Id);
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
            if (_select.data('select2')) {
                _select.fadeOut("fast", function(){
                    _select.select2("destroy");
                });
            }
        }
    }

    addChartToDashboard():void {
        let _instances:string[] = ((this.selectedAllInstances) ? this.allInstances : this.selectedInstances);
        let chart:AbstractChart = Factory.create2dChart(this.selectedChartType, this.chartName, this.groupName, this.selectedComponent,
            _instances, this.selectedMetric);

        // if this is a line chart - add time interval
        if (chart instanceof SeriesBasedChart) {
            chart.preferences["interval"] = this.timeInterval.id;
        }
        if (chart instanceof ScalingRateChart) {
            chart.metrics = this.selectedRateMetrics;
            chart.interval = this.rateInterval.additionalId;
        }
        this._chartService.newChart(chart);
        this._charts = this._chartService.getChartsByGroupName(this.groupName);
        $(Dashboard.chartModalId).modal("hide");
        this.cd.detectChanges();
        let _thisReference:any = this;
        setTimeout(function() {
            chart.draw();
            _thisReference._chartService.saveDashboard();
        }, 400);
    }

    saveChart():void {
        this._chartService.modifyChart(this.currentChart);
        this.cd.detectChanges();
        this.currentChart.draw();
    }

    onChangeStop(index: number, event: NgGridItemEvent): void {
        if (index != undefined && this._charts[index] != undefined) {
            this._charts[index].preferences["gridcfg"] = event;
            this._chartService.saveDashboard();
            this._charts[index].resize();
        }
    }

    removeChart(chartName:string):void {
        this._chartService.removeChart(chartName);
        this._charts = this._chartService.getChartsByGroupName(this.groupName);
    }

    ngOnDestroy():void {
        clearInterval(this.timerId);
    }

    isInstanceSelected(instance:string):boolean {
        return (this.selectedInstances.indexOf(instance) >= 0);
    }

    toggleDrawingChart(chart:AbstractChart):void {
        chart.toggleUpdate();
        $('#toggleDraw' + chart.id + ' i').attr('class', chart.updateStopped ? 'fa fa-play-circle-o' : 'fa fa-pause-circle-o');
    }

    isSvgType(chart:AbstractChart):boolean {
        return chart instanceof SeriesBasedChart || chart instanceof ScalingRateChart;
    }

    isDivType(chart:AbstractChart):boolean {
        return chart instanceof PanelOfAttributeValues || chart instanceof ResourceGroupHealthStatusChart;
    }

    isCanvasType(chart:AbstractChart):boolean {
        return !this.isDivType(chart) && !this.isSvgType(chart);
    }

    isRateMetricSelected(metric:string):boolean {
        return this.selectedRateMetrics.find(value => (value == metric)) != undefined;
    }

    isNewChart():boolean {
        return this.currentChart == undefined;
    }

    private getHiddenSteps():number[] {
        switch (this.selectedChartType) {
            case "statuses":
            case "resources":
            case "voting":
                return [1,2];
            case "scaleIn":
            case "scaleOut":
                return [1];
            default:
                return [];
        }
    }
}

