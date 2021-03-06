import { Component, ViewContainerRef, ChangeDetectorRef, OnDestroy } from '@angular/core';
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
import { TwoDimensionalChartOfAttributeValues } from "./model/abstract.2d.chart.attributes.values";

import { VEXBuiltInThemes, Modal } from 'angular2-modal/plugins/vex';

import 'rxjs/add/operator/publishLast';
import 'rxjs/add/operator/cache';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/observable/from';
import 'rxjs/add/observable/of';

import 'smartwizard';
import 'select2';
import { UserProfileService } from "../services/app.user.profile";
import { isNullOrUndefined } from "util";
import { VotingResultChart } from "./model/charts/voting.result.chart";
import { ChartWithGroupName } from "./model/charts/group.name.based.chart";
import { Subscription } from "rxjs/Subscription";
import { TimerObservable } from "rxjs/observable/TimerObservable";
import { ChartTypeDescription } from "./model/utils";

@Component({
    moduleId: module.id,
    templateUrl: './templates/dashboard.html',
    styleUrls: [ './templates/css/dashboard.css' ]
})
export class Dashboard implements OnDestroy {

    private components:Observable<string[]>;
    private selectedComponent:string = "";

    private metrics:Observable<AttributeInformation[]>;
    private selectedMetric:AttributeInformation;

    private instances:Observable<string[]>;
    private selectedInstances:string[] = [];
    private allInstances:string[] = [];
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

    private subscriberRoute:any = undefined;
    private chartGroupSubscriber:any = undefined;
    private componentSubscriber:any = undefined;
    private instancesSubscriber:any = undefined;
    private metricsSubscriber:any = undefined;

    private subscription: Subscription;

    private useGroup:boolean = true;
    private isInstancesSupported:boolean = true;

    static TYPES:ChartTypeDescription[] = ChartTypeDescription.generateType();

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
        this.isInstancesSupported = Dashboard.TYPES.find((_type:ChartTypeDescription) => this.selectedChartType == _type.consoleSpecificName).instancesSupport;
        this.cd.detectChanges(); // draw the modal html
        this.initNewChart();
    }

    private initNewChart():void {
        this.updateComponentsAndInstances();

        // set all elements to the initial state
        this.allInstances = [];
        this.selectedInstances = [];
        this.selectedMetric = undefined;
        this.components.subscribe((comps:string[]) => {
            this.selectedComponent = comps.length > 0 ? comps[0] : "";
            this.useGroup = comps.length > 0;
            console.debug("Components got: ", comps, " useGroup: ", this.useGroup);
            this.triggerUseGroup(this.useGroup);
        });
        this.timeInterval = this.intervals[0].id;
        this.selectedRateMetrics = [];
        this.rateInterval = this.rateIntervals[0].additionalId;
        this.currentChart = undefined;
        this.cd.detectChanges();

        // init modal components and show modal itself
        this.initModal();
    }

    modifyChart(chartToModify:AbstractChart):void {
        let chart:AbstractChart =  jQuery.extend(true, {}, chartToModify);

        this.selectedChartType = Dashboard.TYPES.find((key:ChartTypeDescription) => key.mappedTypeName == chart.type).consoleSpecificName;
        this.isInstancesSupported = Dashboard.TYPES.find((key:ChartTypeDescription) => key.mappedTypeName == chart.type).instancesSupport;

        this.chartName = chart.name;

        // prefill instances from the existing chart
        if (!isNullOrUndefined(chart["resources"]) && chart["resources"].length > 0) {
            this.selectedInstances = chart["resources"];
        } else {
            this.selectedInstances = [];
        }

        // fill the group (I used property check because some charts have 'group' property defined in different places - fix@todo)
        if (!isNullOrUndefined(chart['group']) && chart['group'].length > 0) {
            this.selectedComponent = chart['group'];
        } else {
            this.selectedComponent = "";
        }

        // fill the rate metric and rate intervals from the chart
        if (!isNullOrUndefined(chart["metrics"])) {
            this.selectedRateMetrics =  chart["metrics"];
        } else {
            this.selectedRateMetrics = [];
        }

        if (!isNullOrUndefined(chart["interval"])) {
            this.rateInterval = chart["interval"];
        } else {
            this.rateInterval = isNullOrUndefined(this.intervals[0]) ? undefined : this.intervals[0].additionalId;
        }

        // fill the time interval if the chart belongs to series based charts
        if (!isNullOrUndefined(chart.preferences["interval"])) {
            this.timeInterval = chart.preferences["interval"];
        } else {
            this.timeInterval = this.intervals[0].id;
        }

        this.useGroup = this.selectedComponent.length > 0;

        // make this chart as current (for further saving it, redrawing and switching button to "save the chart")
        this.currentChart = chart;

        // make sure the front end has received the changes
        this.cd.detectChanges();

        if (!this.useGroup) { // initialize the select2 in case of need
            this.triggerUseGroup(false);
        }

        // init modal components and show the modal
        console.debug("Modifying chart class name is ", chart.constructor.name);
        console.debug("Rate interval for chart ", chart.name, " is: ", this.rateInterval);
        console.debug("Time interval for chart ", chart.name, " is: ", this.timeInterval);
        console.debug("Selected instances for chart ", chart.name, " are: ", this.selectedInstances);
        this.initModal();
    }

    private updateComponentsAndInstances():void {
        //fill components
        this.components = this.http.get(REST.GROUPS_WEB_API)
            .map((res:Response) => { return <string[]>res.json()})
            .publishLast()
            .refCount();
        this.componentSubscriber = this.components.subscribe((data:string[]) => {
            if (data && data.length > 0) {
                this.selectedComponent = data[0];
            }
        });

        // fill instances
        this.instances = this.http.get(REST.GROUPS_RESOURCES)
            .map((res:Response) => { return <string[]>res.json()})
            .publishLast()
            .refCount();
        this.instancesSubscriber = this.instances.subscribe((data:string[]) => {
            this.allInstances = data;
        });

        this.cd.detectChanges();
    }

    private initModal():void {
        // reset wizard
        if (this.smartWizardInitialized) {
            $(Dashboard.wizardId).off("showStep");
            $(Dashboard.wizardId).smartWizard("reset");
        }

        this.initWizard();

        // reset rate metric select2 component
        if ($(Dashboard.rateMetricSelect2Id).data('select2')) {
            $(Dashboard.rateMetricSelect2Id).select2("destroy");
        }
        $(Dashboard.rateMetricSelect2Id).select2({
            placeholder: "Select rate metrics from the dropdown",
            width: '100%',
            allowClear: true
        });
        $(Dashboard.rateMetricSelect2Id).on('change', (e) => {
            this.selectedRateMetrics = $(e.target).val();
        });
        // open the modal
        $(Dashboard.chartModalId).modal("show");
    }

    private updateChartName():void {
        this.chartName = this.selectedChartType + "." +
            (this.selectedComponent != "" ? this.selectedComponent  + "." : "") +
            ((this.selectedMetric != undefined) ? this.selectedMetric.name : "") + "_" + this.allCharts.length;
    }

    ngAfterViewInit():void {
        this.subscription = TimerObservable.create(2000, 1000).subscribe(() => {
            if (this.allCharts.length > 0) this._chartService.receiveDataForCharts(this.allCharts)
        });

        this.subscriberRoute = this.route.params
            .map(params => params['groupName'])
            .subscribe((gn) => {
                this.cd.reattach();
                this.updateComponentsAndInstances();
                // fill dashboard name (group that charts here belong to)
                this.groupName = gn;
                if (!isNullOrUndefined(this._chartService.activeSubscriber)) {
                    this._chartService.activeSubscriber.unsubscribe();
                    this._chartService.activeSubscriber = undefined;
                }
                this.chartGroupSubscriber = this._chartService.getChartsByGroupName(gn).subscribe((chs:AbstractChart[]) => {
                    this.allCharts = chs;
                    chs.forEach((chart:AbstractChart) => chart.reinitialize());
                    this.cd.markForCheck(); // process template
                });
                this._chartService.activeSubscriber = this.chartGroupSubscriber;
            });
    }

    private initWizard():void {
        let _hidden:number[] =  this.getHiddenSteps();

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

        $(Dashboard.wizardId).on("showStep", (e, anchorObject, stepNumber, stepDirection) => {
            this.cd.detectChanges();
            if (stepNumber == 2 && this.isNewChart() && stepDirection == "forward") {
                this.updateChartName();
            } else if (stepNumber == 1 && stepDirection == "forward") {
                if (this.useGroup) {
                    this.loadMetricsByComponents();
                } else {
                    this.loadMetricsByInstances();
                }
            }
        });
        this.smartWizardInitialized = true;
    }

    triggerUseGroup(useGroupStatus:boolean):void {
        this.cd.detectChanges();
        console.debug("Triggered. UseGroupStatus is ", useGroupStatus);
        // if we show the select for instances - reinitialize the select2 component
        if (!useGroupStatus) {
            if ($(Dashboard.select2Id).data('select2')) {
                $(Dashboard.select2Id).select2("destroy");
            }
            $(Dashboard.select2Id).select2({
                placeholder: "Select instances from the dropdown",
                width: '100%',
                allowClear: true
            });
            $(Dashboard.select2Id).on('change', (e) => {
                this.selectedInstances = $(e.target).val(); // no native actions on the selec2 componentс
            });
        }
    }

    private fillMetrics(obs:Observable<AttributeInformation[]>):void {
        this.metrics = obs;
            // set auto selected first metric if the array is not empty
        this.metricsSubscriber = this.metrics.subscribe((data:AttributeInformation[]) => {
            if (data && data.length > 0) {
                if (this.isNewChart()) {
                    this.selectedMetric = data[0];
                } else if (typeof this.currentChart["getSourceAttribute"] != undefined) {
                    console.debug("Chart after metrics received: ", this.currentChart, " instanceof: ", this.currentChart instanceof TwoDimensionalChartOfAttributeValues, " or ", typeof this.currentChart["getSourceAttribute"] != undefined);
                    let _attr:AttributeInformation = this.currentChart["getSourceAttribute"]();
                    this.selectedMetric = data.find(metric => metric.name == _attr.name && metric.type == _attr.type);
                    console.debug("Attr was ", _attr, " metric is ", this.selectedMetric);
                }
            }
        });
    }

    private loadMetricsByInstances():void {
        this.fillMetrics(
            this.http.post(REST.CHART_METRICS_BY_INSTANCES, this.selectedInstances)
                .map((res:Response) => {
                    let _data:any = res.json();
                    let _values:AttributeInformation[] = [];
                    for (let i in _data) {
                        _values.push(new AttributeInformation(_data[i]));
                    }
                    return _values;
                }).catch((res:Response) => Observable.of([])).cache()
        );
    }

    private loadMetricsByComponents():void {
        this.fillMetrics(
            this.selectedComponent == "" ? Observable.of([]) : this.http.get(REST.CHART_METRICS_BY_COMPONENT(this.selectedComponent))
                .map((res:Response) => {
                    let _data:any = res.json();
                    let _values:AttributeInformation[] = [];
                    for (let i in _data) {
                        _values.push(new AttributeInformation(_data[i]));
                    }
                    return _values;
                }).catch((res:Response) => Observable.of([])).cache()
        );
    }

    private generateChart():AbstractChart {
        let _instances:string[] = this.selectedInstances;
        let chart:AbstractChart = Factory.create2dChart(this.selectedChartType, this.chartName, this.groupName, this.useGroup ? this.selectedComponent : "",
            this.useGroup ? [] : _instances, this.selectedMetric);

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
        this._chartService.removeChart(this.currentChart.name, false);
        this.allCharts = this.allCharts.filter((as:AbstractChart) => as.name != this.currentChart.name);
        this.cd.detectChanges();
        this.addChartToDashboard();
    }

    onChangeStop(index: number, event: NgGridItemEvent): void {
        if (this.isAllowed() && !isNullOrUndefined(index) && !isNullOrUndefined(this.allCharts[index])) {
            this._chartService.saveChartsPreferences(this.allCharts[index].name, event);
            this.allCharts[index].resize();
        }
    }

    removeChart(chartName:string):void {
        this._chartService.removeChart(chartName, true);
    }

    ngOnDestroy():void {
        console.debug("DESTROING CHART FOR GROUP ",this.groupName);
        if (!isNullOrUndefined(this.subscription)) this.subscription.unsubscribe();
        if (!isNullOrUndefined(this.subscriberRoute)) {
            this.subscriberRoute.unsubscribe();
        }
        if (!isNullOrUndefined(this.chartGroupSubscriber)) {
            this.chartGroupSubscriber.unsubscribe();
        }
        if (!isNullOrUndefined(this.componentSubscriber)) {
            this.componentSubscriber.unsubscribe();
        }
        if (!isNullOrUndefined(this.instancesSubscriber)) {
            this.instancesSubscriber.unsubscribe();
        }
        if (!isNullOrUndefined(this.metricsSubscriber)) {
            this.metricsSubscriber.unsubscribe();
        }
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

