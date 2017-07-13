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
import { ActivatedRoute, Router } from '@angular/router';
import { ResourceGroupHealthStatusChart } from "./model/charts/resource.group.health.status";
import { SeriesBasedChart, DescriptionIdClass } from "./model/abstract.line.based.chart";
import { ScalingRateChart } from "./model/scaling.rate.chart";
import { ChartOfAttributeValues } from "./model/abstract.chart.attributes.values";
import { TwoDimensionalChartOfAttributeValues } from "./model/abstract.2d.chart.attributes.values";

import { VEXBuiltInThemes, Modal } from 'angular2-modal/plugins/vex';

import 'rxjs/add/operator/publishLast';
import 'rxjs/add/operator/cache';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/of';

import 'smartwizard';
import 'select2';
import {UserProfileService} from "../services/app.user.profile";
import {isNullOrUndefined} from "util";
import {VotingResultChart} from "./model/charts/voting.result.chart";
import {ChartWithGroupName} from "./model/charts/group.name.based.chart";

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

    private allCharts:AbstractChart[] = [];

    private groupName:string = "";

    private intervals:DescriptionIdClass[] = SeriesBasedChart.generateIntervals();
    private rateIntervals:DescriptionIdClass[] = SeriesBasedChart.generateRateIntervals();
    private rateMetrics:DescriptionIdClass[] = ScalingRateChart.prepareRareMetrics();

    private timeInterval:number = undefined;
    private rateInterval:string = undefined;

    private static select2Id:string = "#instancesSelect";
    private static chartModalId:string = "#addChartModal";
    private static wizardId:string = "#smartwizardForChart";
    private static rateMetricSelect2Id:string = "#rateMetricSelect";

    private currentChart:AbstractChart = undefined;

    private smartWizardInitialized:boolean = false;

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
                private modal: Modal,
                private route: ActivatedRoute,
                private router: Router,
                private ups:UserProfileService) {

        overlay.defaultViewContainer = vcRef;
        this.timeInterval = this.intervals[0].id;
        this.rateInterval = this.rateIntervals[0].additionalId;
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
        this.timeInterval = this.intervals[0].id;
        this.selectedRateMetrics = [];
        this.rateInterval = this.rateIntervals[0].additionalId;
        this.currentChart = undefined;

        // fill components and selected component
        this.ngOnInit();

        // init modal components and show modal itself
        this.initModal();
    }

    modifyChart(chartToModify:AbstractChart):void {
        let chart:AbstractChart =  jQuery.extend(true, {}, chartToModify);
        // make this chart as current (for further saving it, redrawing and switching button to "save the chart")
        this.currentChart = chart;
        this.selectedChartType = Object.keys(AbstractChart.TYPE_MAPPING).filter((key) => AbstractChart.TYPE_MAPPING[key] == chart.type)[0];
        this.chartName = chart.name;

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

        this.onComponentSelect(this.selectedComponent);

        // fill the rate metric and rate intervals from the chart
        if (chart instanceof ScalingRateChart) {
            this.selectedRateMetrics =  chart.metrics;
            this.rateInterval = chart.interval;
        } else {
            this.selectedRateMetrics = [];
            this.rateInterval = isNullOrUndefined(this.intervals[0]) ? undefined : this.intervals[0].additionalId;
        }

        // fill the time interval if the chart belongs to series based charts
        if (chart instanceof SeriesBasedChart) {
            this.timeInterval = chart.preferences["interval"];
        } else {
            this.timeInterval = this.intervals[0].id;
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
        console.debug("Rate interval for chart ", chart.name, " is: ", this.rateInterval);
        console.debug("Time interval for chart ", chart.name, " is: ", this.timeInterval);
        this.initModal();
    }

    private initModal():void {
        // reset wizard
        if (this.smartWizardInitialized) {
            $(Dashboard.wizardId).off("showStep");
            $(Dashboard.wizardId).smartWizard("reset");
        }

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
            ((this.selectedMetric != undefined) ? this.selectedMetric.name : "") + "_";
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
                this._chartService.getChartsByGroupName(gn).subscribe((chs:AbstractChart[]) => {
                    console.debug("Got following charts from corresponding observable: ", chs.length, chs);
                    this.allCharts = chs;
                    this.cd.detectChanges(); // process template
                    if (!isNullOrUndefined(this.timerId)) {
                        clearInterval(this.timerId);
                    }
                    for (let i = 0; i < chs.length; i++) {
                        if (!chs[i].initialized) {
                            chs[i].draw();
                            chs[i].initialized = true;
                        }
                    }

                    if (chs.length > 0) {
                        this.timerId = setInterval(function () {
                            console.debug("Timer event is thrown");
                            _thisReference._chartService.receiveDataForCharts(chs);
                        }, 1500);
                    }
                });
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
        this.smartWizardInitialized = true;
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

    private generateChart():AbstractChart {
        let _instances:string[] = ((this.selectedAllInstances) ? [] : this.selectedInstances);
        let chart:AbstractChart = Factory.create2dChart(this.selectedChartType, this.chartName, this.groupName, this.selectedComponent,
            _instances, this.selectedMetric);

        // if this is a line chart - add time interval
        if (chart instanceof SeriesBasedChart) {
            chart.preferences["interval"] = this.timeInterval;
        }
        if (chart instanceof ScalingRateChart) {
            chart.metrics = this.selectedRateMetrics;
            chart.interval = this.rateInterval;
        }
        return chart;
    }

    addChartToDashboard():void {
        this._chartService.newChart(this.generateChart());
        $(Dashboard.chartModalId).modal("hide");
    }

    saveChart():void {
        let _gChart:AbstractChart = this.generateChart();
        _gChart.id = this.currentChart.id;
        this._chartService.modifyChart(_gChart);
        this.currentChart = undefined;
        $(Dashboard.chartModalId).modal("hide");
    }

    onChangeStop(index: number, event: NgGridItemEvent): void {
        if (this.isAllowed() && !isNullOrUndefined(index) && !isNullOrUndefined(this.allCharts[index])) {
            this.allCharts[index].preferences["gridcfg"] = event;
            this._chartService.saveDashboard();
            this.allCharts[index].resize();
        }
    }

    removeChart(chartName:string):void {
        this._chartService.removeChart(chartName);
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

    isResetable(chart:AbstractChart):boolean {
        return chart instanceof ScalingRateChart || chart instanceof VotingResultChart || chart instanceof ResourceGroupHealthStatusChart;
    }

    resetChart(chart:ChartWithGroupName):void {
        this.modal.confirm()
            .className(<VEXBuiltInThemes>'default')
            .message('Chart will be reset for all dashboards and users. Proceed?')
            .open()
            .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                    .then((response) => {
                        this._chartService.resetChart(chart);
                        return response;
                    })
                    .catch(() => {
                        console.debug("user preferred to decline dashboard removing");
                    });
            }).catch(() => {});
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

    removeDashboard():void {
        this.modal.confirm()
            .className(<VEXBuiltInThemes>'default')
            .message('Dashboard will be removed. Proceed?')
            .open()
            .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                    .then((response) => {
                        this._chartService.removeChartsByGroupName(this.groupName);
                        let _arr:string[] = this._chartService.getSimpleGroupName();
                        if (_arr.length > 0) {
                            this.router.navigate(['charts', _arr[0]]);
                        } else {
                            this.router.navigate(['charts']);
                        }
                        return response;
                    })
                    .catch(() => {
                        console.debug("user preferred to decline dashboard removing");
                    });
            }).catch(() => {});
    }

    isAllowed():boolean {
        return this.ups.isUserHasManagerOrAdminRole();
    }
}

