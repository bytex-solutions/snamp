webpackJsonpac__name_([2],{

/***/ "./node_modules/css-loader/index.js!./src/app/charts/templates/css/dashboard.css":
/***/ function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("./node_modules/css-loader/lib/css-base.js")();
// imports


// module
exports.push([module.i, ".thumbnail.with-caption {\r\n    display: inline-block;\r\n    background: #f5f5f5;\r\n}\r\n.thumbnail.with-caption p {\r\n    margin: 0;\r\n    padding-top: 0.5em;\r\n    text-align: center;\r\n    font-size: large;\r\n}\r\n.thumbnail.with-caption small:before {\r\n    content: '\\2014   \\A0';\r\n}\r\n.thumbnail {\r\n    height: auto;\r\n    margin: 10px;\r\n    max-height: 190px;\r\n}\r\n.thumbnail.with-caption small {\r\n    width: 100%;\r\n    text-align: right;\r\n    display: inline-block;\r\n    color: #999;\r\n}\r\n\r\nselect.form-control:not([size]):not([multiple]) {\r\n    height: auto;\r\n    cursor: pointer;\r\n}\r\n\r\n.chartMenuImage {\r\n  position: fixed;\r\n  right: 80px;\r\n  top: 50%;\r\n  margin-top: -9em !important;\r\n  z-index: 3;\r\n}\r\n\r\n#chartMenu {\r\n  background: #536980;\r\n  color: rgba(255,255,255,0.95);\r\n  position: fixed;\r\n  right: 0;\r\n  top: 50%;\r\n  margin-top: -10em !important;\r\n  list-style-type: none;\r\n  z-index: 3;\r\n  padding: 9px 0 9px 9px;\r\n  border-top-left-radius: 5px;\r\n  border-bottom-left-radius: 5px;\r\n}\r\n\r\n#chartMenu li {\r\n    cursor: pointer;\r\n    margin-bottom: 15px;\r\n    margin-right: 5px;\r\n}\r\n\r\n#chartMenu li i {\r\n    padding: 7px;\r\n    border-radius: 5px;\r\n    font-size: 1.5em;\r\n}\r\n\r\n#chartMenu li:hover i {\r\n    background: #1ABB9C;\r\n}\r\n\r\n#chartMenu li:last-child {\r\n    margin-bottom: 0;\r\n}\r\n\r\n.jumbotron {\r\n    padding: 15px !important;\r\n}\r\n\r\n.jumbotron p {\r\n    font-size: 17px !important;\r\n}\r\n\r\n.grid {\r\n\ttransform-origin: top center;\r\n\ttransition: transform 0.5s;\r\n\tposition: relative;\r\n}\r\n\r\n.grid-item {\r\n\tbackground-color: #ffffff;\r\n\t-webkit-transition: width 0.25s, height 0.25s, transform 0.5s;\r\n\t-moz-transition: width 0.25s, height 0.25s, transform 0.5s;\r\n\t-o-transition: width 0.25s, height 0.25s, transform 0.5s;\r\n\ttransition: width 0.25s, height 0.25s, transform 0.5s;\r\n\toverflow: hidden;\r\n\tposition: absolute;\r\n\tbox-shadow: 3px 3px 3px rgba(0,0,0,0.4);\r\n\tborder: 1px solid rgb(42, 63, 84);\r\n    border-radius: 5px;\r\n}\r\n\r\n.grid-item:active, .grid-item.moving {\r\n\tz-index: 2;\r\n\t-webkit-transition: none;\r\n\t-moz-transition: none;\r\n\t-o-transition: none;\r\n\ttransition: none;\r\n}\r\n\r\n.grid-placeholder {\r\n\tbackground-color: rgba(0, 0, 0, 0.3);\r\n}\r\n\r\n.grid-item.moving {\r\n    z-index: z-index + 1;\r\n}\r\n\r\n.placeholder {\r\n    position: absolute;\r\n}\r\n\r\n.nvd3.nv-pie.nv-chart-donut1 .nv-pie-title {\r\n    opacity: 0.4;\r\n    fill: rgba(224, 116, 76, 0.91);\r\n}\r\n\r\n.x_title h2 {\r\n    font-size: inherit !important;\r\n    color: black;\r\n    display: inline;\r\n    max-width: calc(100% - 81px);\r\n}\r\n\r\n.x_title h5 {\r\n    display: inline;\r\n    margin-left: 15px;\r\n}\r\n\r\n.x_title {\r\n    border-bottom: 2px solid #E6E9ED;\r\n    padding: 0 0 7px 0 !important;\r\n    margin-bottom: 0 !important;\r\n    font-style: italic;\r\n}\r\n\r\n.panel_toolbox > li >a {\r\n    display: inline;\r\n}\r\n\r\nul.panel_toolbox i.fa-pause-circle-o {\r\n    color:#ffa3a3;\r\n}\r\n\r\nul.panel_toolbox i.fa-play-circle-o {\r\n    color:#639863;\r\n}\r\n\r\n.tableWrapper {\r\n    position: relative;\r\n    height: 100%;\r\n    overflow: hidden;\r\n}\r\n\r\n.hidedSelect {\r\n    display: none;\r\n}", ""]);

// exports


/***/ },

/***/ "./node_modules/rxjs/observable/TimerObservable.js":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var __extends = (this && this.__extends) || function (d, b) {
    for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p];
    function __() { this.constructor = d; }
    d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
};
var isNumeric_1 = __webpack_require__("./node_modules/rxjs/util/isNumeric.js");
var Observable_1 = __webpack_require__("./node_modules/rxjs/Observable.js");
var async_1 = __webpack_require__("./node_modules/rxjs/scheduler/async.js");
var isScheduler_1 = __webpack_require__("./node_modules/rxjs/util/isScheduler.js");
var isDate_1 = __webpack_require__("./node_modules/rxjs/util/isDate.js");
/**
 * We need this JSDoc comment for affecting ESDoc.
 * @extends {Ignored}
 * @hide true
 */
var TimerObservable = (function (_super) {
    __extends(TimerObservable, _super);
    function TimerObservable(dueTime, period, scheduler) {
        if (dueTime === void 0) { dueTime = 0; }
        _super.call(this);
        this.period = -1;
        this.dueTime = 0;
        if (isNumeric_1.isNumeric(period)) {
            this.period = Number(period) < 1 && 1 || Number(period);
        }
        else if (isScheduler_1.isScheduler(period)) {
            scheduler = period;
        }
        if (!isScheduler_1.isScheduler(scheduler)) {
            scheduler = async_1.async;
        }
        this.scheduler = scheduler;
        this.dueTime = isDate_1.isDate(dueTime) ?
            (+dueTime - this.scheduler.now()) :
            dueTime;
    }
    /**
     * Creates an Observable that starts emitting after an `initialDelay` and
     * emits ever increasing numbers after each `period` of time thereafter.
     *
     * <span class="informal">Its like {@link interval}, but you can specify when
     * should the emissions start.</span>
     *
     * <img src="./img/timer.png" width="100%">
     *
     * `timer` returns an Observable that emits an infinite sequence of ascending
     * integers, with a constant interval of time, `period` of your choosing
     * between those emissions. The first emission happens after the specified
     * `initialDelay`. The initial delay may be a {@link Date}. By default, this
     * operator uses the `async` Scheduler to provide a notion of time, but you
     * may pass any Scheduler to it. If `period` is not specified, the output
     * Observable emits only one value, `0`. Otherwise, it emits an infinite
     * sequence.
     *
     * @example <caption>Emits ascending numbers, one every second (1000ms), starting after 3 seconds</caption>
     * var numbers = Rx.Observable.timer(3000, 1000);
     * numbers.subscribe(x => console.log(x));
     *
     * @example <caption>Emits one number after five seconds</caption>
     * var numbers = Rx.Observable.timer(5000);
     * numbers.subscribe(x => console.log(x));
     *
     * @see {@link interval}
     * @see {@link delay}
     *
     * @param {number|Date} initialDelay The initial delay time to wait before
     * emitting the first value of `0`.
     * @param {number} [period] The period of time between emissions of the
     * subsequent numbers.
     * @param {Scheduler} [scheduler=async] The Scheduler to use for scheduling
     * the emission of values, and providing a notion of "time".
     * @return {Observable} An Observable that emits a `0` after the
     * `initialDelay` and ever increasing numbers after each `period` of time
     * thereafter.
     * @static true
     * @name timer
     * @owner Observable
     */
    TimerObservable.create = function (initialDelay, period, scheduler) {
        if (initialDelay === void 0) { initialDelay = 0; }
        return new TimerObservable(initialDelay, period, scheduler);
    };
    TimerObservable.dispatch = function (state) {
        var index = state.index, period = state.period, subscriber = state.subscriber;
        var action = this;
        subscriber.next(index);
        if (subscriber.closed) {
            return;
        }
        else if (period === -1) {
            return subscriber.complete();
        }
        state.index = index + 1;
        action.schedule(state, period);
    };
    TimerObservable.prototype._subscribe = function (subscriber) {
        var index = 0;
        var _a = this, period = _a.period, dueTime = _a.dueTime, scheduler = _a.scheduler;
        return scheduler.schedule(TimerObservable.dispatch, dueTime, {
            index: index, period: period, subscriber: subscriber
        });
    };
    return TimerObservable;
}(Observable_1.Observable));
exports.TimerObservable = TimerObservable;
//# sourceMappingURL=TimerObservable.js.map

/***/ },

/***/ "./node_modules/rxjs/scheduler/async.js":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var AsyncAction_1 = __webpack_require__("./node_modules/rxjs/scheduler/AsyncAction.js");
var AsyncScheduler_1 = __webpack_require__("./node_modules/rxjs/scheduler/AsyncScheduler.js");
exports.async = new AsyncScheduler_1.AsyncScheduler(AsyncAction_1.AsyncAction);
//# sourceMappingURL=async.js.map

/***/ },

/***/ "./node_modules/rxjs/util/isDate.js":
/***/ function(module, exports) {

"use strict";
"use strict";
function isDate(value) {
    return value instanceof Date && !isNaN(+value);
}
exports.isDate = isDate;
//# sourceMappingURL=isDate.js.map

/***/ },

/***/ "./node_modules/rxjs/util/isNumeric.js":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var isArray_1 = __webpack_require__("./node_modules/rxjs/util/isArray.js");
function isNumeric(val) {
    // parseFloat NaNs numeric-cast false positives (null|true|false|"")
    // ...but misinterprets leading-number strings, particularly hex literals ("0x...")
    // subtraction forces infinities to NaN
    // adding 1 corrects loss of precision from parseFloat (#15100)
    return !isArray_1.isArray(val) && (val - parseFloat(val) + 1) >= 0;
}
exports.isNumeric = isNumeric;
;
//# sourceMappingURL=isNumeric.js.map

/***/ },

/***/ "./src/app/charts/charts.dashboard.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(jQuery, $) {"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var app_restClient_1 = __webpack_require__("./src/app/services/app.restClient.ts");
var Observable_1 = __webpack_require__("./node_modules/rxjs/Observable.js");
var angular2_modal_1 = __webpack_require__("./node_modules/angular2-modal/esm/index.js");
var attribute_1 = __webpack_require__("./src/app/charts/model/attribute.ts");
var app_chartService_1 = __webpack_require__("./src/app/services/app.chartService.ts");
var objectFactory_1 = __webpack_require__("./src/app/charts/model/objectFactory.ts");
var panel_attributes_values_1 = __webpack_require__("./src/app/charts/model/charts/panel.attributes.values.ts");
var router_1 = __webpack_require__("./node_modules/@angular/router/index.js");
var resource_group_health_status_1 = __webpack_require__("./src/app/charts/model/charts/resource.group.health.status.ts");
var abstract_line_based_chart_1 = __webpack_require__("./src/app/charts/model/abstract.line.based.chart.ts");
var scaling_rate_chart_1 = __webpack_require__("./src/app/charts/model/scaling.rate.chart.ts");
var abstract_2d_chart_attributes_values_1 = __webpack_require__("./src/app/charts/model/abstract.2d.chart.attributes.values.ts");
var vex_1 = __webpack_require__("./node_modules/angular2-modal/plugins/vex/index.js");
__webpack_require__("./node_modules/rxjs/add/operator/publishLast.js");
__webpack_require__("./node_modules/rxjs/add/operator/cache.js");
__webpack_require__("./node_modules/rxjs/add/observable/forkJoin.js");
__webpack_require__("./node_modules/rxjs/add/observable/from.js");
__webpack_require__("./node_modules/rxjs/add/observable/of.js");
__webpack_require__("./node_modules/smartwizard/js/jquery.smartWizard.min.js");
__webpack_require__("./node_modules/select2/dist/js/select2.js");
var app_user_profile_1 = __webpack_require__("./src/app/services/app.user.profile.ts");
var util_1 = __webpack_require__("./node_modules/util/util.js");
var voting_result_chart_1 = __webpack_require__("./src/app/charts/model/charts/voting.result.chart.ts");
var TimerObservable_1 = __webpack_require__("./node_modules/rxjs/observable/TimerObservable.js");
var utils_1 = __webpack_require__("./src/app/charts/model/utils.ts");
var Dashboard = (function () {
    function Dashboard(http, overlay, vcRef, _chartService, cd, modal, route, router, ups) {
        this.http = http;
        this._chartService = _chartService;
        this.cd = cd;
        this.modal = modal;
        this.route = route;
        this.router = router;
        this.ups = ups;
        this.selectedComponent = "";
        this.selectedInstances = [];
        this.allInstances = [];
        this.selectedRateMetrics = [];
        this.selectedChartType = "";
        this.chartName = "newChart";
        this.allCharts = [];
        this.groupName = "";
        this.intervals = abstract_line_based_chart_1.SeriesBasedChart.generateIntervals();
        this.rateIntervals = abstract_line_based_chart_1.SeriesBasedChart.generateRateIntervals();
        this.rateMetrics = scaling_rate_chart_1.ScalingRateChart.prepareRareMetrics();
        this.timeInterval = undefined;
        this.rateInterval = undefined;
        this.currentChart = undefined;
        this.smartWizardInitialized = false;
        this.subscriberRoute = undefined;
        this.chartGroupSubscriber = undefined;
        this.componentSubscriber = undefined;
        this.instancesSubscriber = undefined;
        this.metricsSubscriber = undefined;
        this.useGroup = true;
        this.isInstancesSupported = true;
        this.gridConfig = {
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
            'fix_to_grid': true,
            'auto_style': true,
            'auto_resize': true,
            'maintain_ratio': false,
            'prefer_new': false,
            'zoom_on_drag': false,
            'limit_to_screen': true
        };
        overlay.defaultViewContainer = vcRef;
        this.timeInterval = this.intervals[0].id;
        this.rateInterval = this.rateIntervals[0].additionalId;
    }
    Dashboard.prototype.appendChartClicked = function (type) {
        var _this = this;
        this.selectedChartType = type;
        this.isInstancesSupported = Dashboard.TYPES.find(function (_type) { return _this.selectedChartType == _type.consoleSpecificName; }).instancesSupport;
        this.cd.detectChanges(); // draw the modal html
        this.initNewChart();
    };
    Dashboard.prototype.initNewChart = function () {
        var _this = this;
        this.updateComponentsAndInstances();
        // set all elements to the initial state
        this.allInstances = [];
        this.selectedInstances = [];
        this.selectedMetric = undefined;
        this.components.subscribe(function (comps) {
            _this.selectedComponent = comps.length > 0 ? comps[0] : "";
            _this.useGroup = comps.length > 0;
            console.debug("Components got: ", comps, " useGroup: ", _this.useGroup);
            _this.triggerUseGroup(_this.useGroup);
        });
        this.timeInterval = this.intervals[0].id;
        this.selectedRateMetrics = [];
        this.rateInterval = this.rateIntervals[0].additionalId;
        this.currentChart = undefined;
        this.cd.detectChanges();
        // init modal components and show modal itself
        this.initModal();
    };
    Dashboard.prototype.modifyChart = function (chartToModify) {
        var chart = jQuery.extend(true, {}, chartToModify);
        this.selectedChartType = Dashboard.TYPES.find(function (key) { return key.mappedTypeName == chart.type; }).mappedTypeName;
        this.isInstancesSupported = Dashboard.TYPES.find(function (key) { return key.mappedTypeName == chart.type; }).instancesSupport;
        this.chartName = chart.name;
        // prefill instances from the existing chart
        if (!util_1.isNullOrUndefined(chart["resources"]) && chart["resources"].length > 0) {
            this.selectedInstances = chart["resources"];
        }
        else {
            this.selectedInstances = [];
        }
        // fill the group (I used property check because some charts have 'group' property defined in different places - fix@todo)
        if (!util_1.isNullOrUndefined(chart['group']) && chart['group'].length > 0) {
            this.selectedComponent = chart['group'];
        }
        else {
            this.selectedComponent = "";
        }
        // fill the rate metric and rate intervals from the chart
        if (!util_1.isNullOrUndefined(chart["metrics"])) {
            this.selectedRateMetrics = chart["metrics"];
        }
        else {
            this.selectedRateMetrics = [];
        }
        if (!util_1.isNullOrUndefined(chart["interval"])) {
            this.rateInterval = chart["interval"];
        }
        else {
            this.rateInterval = util_1.isNullOrUndefined(this.intervals[0]) ? undefined : this.intervals[0].additionalId;
        }
        // fill the time interval if the chart belongs to series based charts
        if (!util_1.isNullOrUndefined(chart.preferences["interval"])) {
            this.timeInterval = chart.preferences["interval"];
        }
        else {
            this.timeInterval = this.intervals[0].id;
        }
        this.useGroup = this.selectedComponent.length > 0;
        // make this chart as current (for further saving it, redrawing and switching button to "save the chart")
        this.currentChart = chart;
        // make sure the front end has received the changes
        this.cd.detectChanges();
        if (!this.useGroup) {
            this.triggerUseGroup(false);
        }
        // init modal components and show the modal
        console.debug("Modifying chart class name is ", chart.constructor.name);
        console.debug("Rate interval for chart ", chart.name, " is: ", this.rateInterval);
        console.debug("Time interval for chart ", chart.name, " is: ", this.timeInterval);
        console.debug("Selected instances for chart ", chart.name, " are: ", this.selectedInstances);
        this.initModal();
    };
    Dashboard.prototype.updateComponentsAndInstances = function () {
        var _this = this;
        //fill components
        this.components = this.http.get(app_restClient_1.REST.GROUPS_WEB_API)
            .map(function (res) { return res.json(); })
            .publishLast()
            .refCount();
        this.componentSubscriber = this.components.subscribe(function (data) {
            if (data && data.length > 0) {
                _this.selectedComponent = data[0];
            }
        });
        // fill instances
        this.instances = this.http.get(app_restClient_1.REST.GROUPS_RESOURCES)
            .map(function (res) { return res.json(); })
            .publishLast()
            .refCount();
        this.instancesSubscriber = this.instances.subscribe(function (data) {
            _this.allInstances = data;
        });
        this.cd.detectChanges();
    };
    Dashboard.prototype.initModal = function () {
        var _this = this;
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
        $(Dashboard.rateMetricSelect2Id).on('change', function (e) {
            _this.selectedRateMetrics = $(e.target).val();
        });
        // open the modal
        $(Dashboard.chartModalId).modal("show");
    };
    Dashboard.prototype.updateChartName = function () {
        this.chartName = this.selectedChartType + "." +
            (this.selectedComponent != "" ? this.selectedComponent + "." : "") +
            ((this.selectedMetric != undefined) ? this.selectedMetric.name : "") + "_" + this.allCharts.length;
    };
    Dashboard.prototype.ngAfterViewInit = function () {
        var _this = this;
        this.subscription = TimerObservable_1.TimerObservable.create(2000, 1000).subscribe(function () {
            if (_this.allCharts.length > 0)
                _this._chartService.receiveDataForCharts(_this.allCharts);
        });
        this.subscriberRoute = this.route.params
            .map(function (params) { return params['groupName']; })
            .subscribe(function (gn) {
            _this.cd.reattach();
            _this.updateComponentsAndInstances();
            // fill dashboard name (group that charts here belong to)
            _this.groupName = gn;
            _this.chartGroupSubscriber = _this._chartService.getChartsByGroupName(gn).subscribe(function (chs) {
                _this.allCharts = chs;
                _this.cd.markForCheck(); // process template
            });
        });
    };
    Dashboard.prototype.initWizard = function () {
        var _this = this;
        var _hidden = this.getHiddenSteps();
        $(Dashboard.wizardId).smartWizard({
            theme: 'arrows',
            hiddenSteps: _hidden,
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });
        var _elements = $(".nav.nav-tabs.step-anchor").find("li");
        for (var i = 0; i < _elements.length; i++) {
            if (_hidden.indexOf(i) >= 0) {
                $(_elements[i]).addClass("hidden");
            }
            else {
                $(_elements[i]).removeClass("hidden");
            }
        }
        $(Dashboard.wizardId).on("showStep", function (e, anchorObject, stepNumber, stepDirection) {
            _this.cd.detectChanges();
            if (stepNumber == 2 && _this.isNewChart() && stepDirection == "forward") {
                _this.updateChartName();
            }
            else if (stepNumber == 1 && stepDirection == "forward") {
                if (_this.useGroup) {
                    _this.loadMetricsByComponents();
                }
                else {
                    _this.loadMetricsByInstances();
                }
            }
        });
        this.smartWizardInitialized = true;
    };
    Dashboard.prototype.triggerUseGroup = function (useGroupStatus) {
        var _this = this;
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
            $(Dashboard.select2Id).on('change', function (e) {
                _this.selectedInstances = $(e.target).val(); // no native actions on the selec2 componentс
            });
        }
    };
    Dashboard.prototype.fillMetrics = function (obs) {
        var _this = this;
        this.metrics = obs;
        // set auto selected first metric if the array is not empty
        this.metricsSubscriber = this.metrics.subscribe(function (data) {
            if (data && data.length > 0) {
                if (_this.isNewChart()) {
                    _this.selectedMetric = data[0];
                }
                else if (typeof _this.currentChart["getSourceAttribute"] != undefined) {
                    console.debug("Chart after metrics received: ", _this.currentChart, " instanceof: ", _this.currentChart instanceof abstract_2d_chart_attributes_values_1.TwoDimensionalChartOfAttributeValues, " or ", typeof _this.currentChart["getSourceAttribute"] != undefined);
                    var _attr_1 = _this.currentChart["getSourceAttribute"]();
                    _this.selectedMetric = data.find(function (metric) { return metric.name == _attr_1.name && metric.type == _attr_1.type; });
                    console.debug("Attr was ", _attr_1, " metric is ", _this.selectedMetric);
                }
            }
        });
    };
    Dashboard.prototype.loadMetricsByInstances = function () {
        this.fillMetrics(this.http.post(app_restClient_1.REST.CHART_METRICS_BY_INSTANCES, this.selectedInstances)
            .map(function (res) {
            var _data = res.json();
            var _values = [];
            for (var i in _data) {
                _values.push(new attribute_1.AttributeInformation(_data[i]));
            }
            return _values;
        }).catch(function (res) { return Observable_1.Observable.of([]); }).cache());
    };
    Dashboard.prototype.loadMetricsByComponents = function () {
        this.fillMetrics(this.selectedComponent == "" ? Observable_1.Observable.of([]) : this.http.get(app_restClient_1.REST.CHART_METRICS_BY_COMPONENT(this.selectedComponent))
            .map(function (res) {
            var _data = res.json();
            var _values = [];
            for (var i in _data) {
                _values.push(new attribute_1.AttributeInformation(_data[i]));
            }
            return _values;
        }).catch(function (res) { return Observable_1.Observable.of([]); }).cache());
    };
    Dashboard.prototype.generateChart = function () {
        var _instances = this.selectedInstances;
        var chart = objectFactory_1.Factory.create2dChart(this.selectedChartType, this.chartName, this.groupName, this.useGroup ? this.selectedComponent : "", this.useGroup ? [] : _instances, this.selectedMetric);
        // if this is a line chart - add time interval
        if (chart instanceof abstract_line_based_chart_1.SeriesBasedChart) {
            chart.preferences["interval"] = this.timeInterval;
        }
        if (chart instanceof scaling_rate_chart_1.ScalingRateChart) {
            chart.metrics = this.selectedRateMetrics;
            chart.interval = this.rateInterval;
        }
        return chart;
    };
    Dashboard.prototype.addChartToDashboard = function () {
        this._chartService.newChart(this.generateChart());
        $(Dashboard.chartModalId).modal("hide");
    };
    Dashboard.prototype.saveChart = function () {
        var _this = this;
        this.allCharts = this.allCharts.filter(function (as) { return as.name != _this.currentChart.name; });
        this.removeChart(this.currentChart.name);
        this.cd.detectChanges();
        this.addChartToDashboard();
    };
    Dashboard.prototype.onChangeStop = function (index, event) {
        if (this.isAllowed() && !util_1.isNullOrUndefined(index) && !util_1.isNullOrUndefined(this.allCharts[index])) {
            this._chartService.saveChartsPreferences(this.allCharts[index].name, event);
            this.allCharts[index].resize();
        }
    };
    Dashboard.prototype.removeChart = function (chartName) {
        this._chartService.removeChart(chartName);
    };
    Dashboard.prototype.ngOnDestroy = function () {
        console.debug("DESTROING CHART FOR GROUP ", this.groupName);
        if (!util_1.isNullOrUndefined(this.subscription))
            this.subscription.unsubscribe();
        if (!util_1.isNullOrUndefined(this.subscriberRoute)) {
            this.subscriberRoute.unsubscribe();
        }
        if (!util_1.isNullOrUndefined(this.chartGroupSubscriber)) {
            this.chartGroupSubscriber.unsubscribe();
        }
        if (!util_1.isNullOrUndefined(this.componentSubscriber)) {
            this.componentSubscriber.unsubscribe();
        }
        if (!util_1.isNullOrUndefined(this.instancesSubscriber)) {
            this.instancesSubscriber.unsubscribe();
        }
        if (!util_1.isNullOrUndefined(this.metricsSubscriber)) {
            this.metricsSubscriber.unsubscribe();
        }
    };
    Dashboard.prototype.isInstanceSelected = function (instance) {
        return (this.selectedInstances.indexOf(instance) >= 0);
    };
    Dashboard.prototype.toggleDrawingChart = function (chart) {
        chart.toggleUpdate();
        $('#toggleDraw' + chart.id + ' i').attr('class', chart.updateStopped ? 'fa fa-play-circle-o' : 'fa fa-pause-circle-o');
    };
    Dashboard.prototype.isSvgType = function (chart) {
        return chart instanceof abstract_line_based_chart_1.SeriesBasedChart || chart instanceof scaling_rate_chart_1.ScalingRateChart;
    };
    Dashboard.prototype.isDivType = function (chart) {
        return chart instanceof panel_attributes_values_1.PanelOfAttributeValues || chart instanceof resource_group_health_status_1.ResourceGroupHealthStatusChart;
    };
    Dashboard.prototype.isCanvasType = function (chart) {
        return !this.isDivType(chart) && !this.isSvgType(chart);
    };
    Dashboard.prototype.isRateMetricSelected = function (metric) {
        return this.selectedRateMetrics.find(function (value) { return (value == metric); }) != undefined;
    };
    Dashboard.prototype.isNewChart = function () {
        return this.currentChart == undefined;
    };
    Dashboard.prototype.isResetable = function (chart) {
        return chart instanceof scaling_rate_chart_1.ScalingRateChart || chart instanceof voting_result_chart_1.VotingResultChart || chart instanceof resource_group_health_status_1.ResourceGroupHealthStatusChart;
    };
    Dashboard.prototype.resetChart = function (chart) {
        var _this = this;
        this.modal.confirm()
            .className('default')
            .message('Chart will be reset for all dashboards and users. Proceed?')
            .open()
            .then(function (resultPromise) {
            return resultPromise.result
                .then(function (response) {
                _this._chartService.resetChart(chart);
                return response;
            })
                .catch(function () {
                console.debug("user preferred to decline dashboard removing");
            });
        }).catch(function () { });
    };
    Dashboard.prototype.getHiddenSteps = function () {
        switch (this.selectedChartType) {
            case "statuses":
            case "resources":
            case "voting":
                return [1];
            default:
                return [];
        }
    };
    Dashboard.prototype.removeDashboard = function () {
        var _this = this;
        this.modal.confirm()
            .className('default')
            .message('Dashboard will be removed. Proceed?')
            .open()
            .then(function (resultPromise) {
            return resultPromise.result
                .then(function (response) {
                _this._chartService.removeChartsByGroupName(_this.groupName);
                var _arr = _this._chartService.getSimpleGroupName();
                if (_arr.length > 0) {
                    _this.router.navigate(['charts', _arr[0]]);
                }
                else {
                    _this.router.navigate(['charts']);
                }
                return response;
            })
                .catch(function () {
                console.debug("user preferred to decline dashboard removing");
            });
        }).catch(function () { });
    };
    Dashboard.prototype.isAllowed = function () {
        return this.ups.isUserHasManagerOrAdminRole();
    };
    Dashboard.select2Id = "#instancesSelect";
    Dashboard.chartModalId = "#addChartModal";
    Dashboard.wizardId = "#smartwizardForChart";
    Dashboard.rateMetricSelect2Id = "#rateMetricSelect";
    Dashboard.TYPES = utils_1.ChartTypeDescription.generateType();
    Dashboard = __decorate([
        core_1.Component({
            moduleId: module.i,
            template: __webpack_require__("./src/app/charts/templates/dashboard.html"),
            styles: [__webpack_require__("./src/app/charts/templates/css/dashboard.css")]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof app_restClient_1.ApiClient !== 'undefined' && app_restClient_1.ApiClient) === 'function' && _a) || Object, (typeof (_b = typeof angular2_modal_1.Overlay !== 'undefined' && angular2_modal_1.Overlay) === 'function' && _b) || Object, (typeof (_c = typeof core_1.ViewContainerRef !== 'undefined' && core_1.ViewContainerRef) === 'function' && _c) || Object, (typeof (_d = typeof app_chartService_1.ChartService !== 'undefined' && app_chartService_1.ChartService) === 'function' && _d) || Object, (typeof (_e = typeof core_1.ChangeDetectorRef !== 'undefined' && core_1.ChangeDetectorRef) === 'function' && _e) || Object, (typeof (_f = typeof vex_1.Modal !== 'undefined' && vex_1.Modal) === 'function' && _f) || Object, (typeof (_g = typeof router_1.ActivatedRoute !== 'undefined' && router_1.ActivatedRoute) === 'function' && _g) || Object, (typeof (_h = typeof router_1.Router !== 'undefined' && router_1.Router) === 'function' && _h) || Object, (typeof (_j = typeof app_user_profile_1.UserProfileService !== 'undefined' && app_user_profile_1.UserProfileService) === 'function' && _j) || Object])
    ], Dashboard);
    return Dashboard;
    var _a, _b, _c, _d, _e, _f, _g, _h, _j;
}());
exports.Dashboard = Dashboard;

/* WEBPACK VAR INJECTION */}.call(exports, __webpack_require__("./node_modules/jquery/dist/jquery.js"), __webpack_require__("./node_modules/jquery/dist/jquery.js")))

/***/ },

/***/ "./src/app/charts/charts.empty.dashboard.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var EmptyDashboard = (function () {
    function EmptyDashboard() {
    }
    EmptyDashboard = __decorate([
        core_1.Component({
            moduleId: module.i,
            template: __webpack_require__("./src/app/charts/templates/empty.html"),
        }), 
        __metadata('design:paramtypes', [])
    ], EmptyDashboard);
    return EmptyDashboard;
}());
exports.EmptyDashboard = EmptyDashboard;


/***/ },

/***/ "./src/app/charts/charts.modules.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var common_1 = __webpack_require__("./node_modules/@angular/common/index.js");
var app_restClient_1 = __webpack_require__("./src/app/services/app.restClient.ts");
var ng2_tooltip_1 = __webpack_require__("./node_modules/ng2-tooltip/index.js");
var forms_1 = __webpack_require__("./node_modules/@angular/forms/index.js");
var http_1 = __webpack_require__("./node_modules/@angular/http/index.js");
var router_1 = __webpack_require__("./node_modules/@angular/router/index.js");
var angular2_modal_1 = __webpack_require__("./node_modules/angular2-modal/esm/index.js");
var vex_1 = __webpack_require__("./node_modules/angular2-modal/plugins/vex/index.js");
var NgGrid_module_1 = __webpack_require__("./src/app/controls/nggrid/modules/NgGrid.module.ts");
var charts_dashboard_1 = __webpack_require__("./src/app/charts/charts.dashboard.ts");
var app_module_1 = __webpack_require__("./src/app/app.module.ts");
var charts_template_1 = __webpack_require__("./src/app/charts/charts.template.ts");
var charts_empty_dashboard_1 = __webpack_require__("./src/app/charts/charts.empty.dashboard.ts");
var PROVIDERS = [
    app_restClient_1.ApiClient
];
var DashboardModule = (function () {
    function DashboardModule() {
    }
    DashboardModule = __decorate([
        core_1.NgModule({
            imports: [
                common_1.CommonModule,
                ng2_tooltip_1.TooltipModule,
                forms_1.FormsModule,
                angular2_modal_1.ModalModule.forRoot(),
                vex_1.VexModalModule,
                http_1.HttpModule,
                app_module_1.CommonSnampUtilsModule,
                NgGrid_module_1.NgGridModule,
                router_1.RouterModule.forChild([{
                        path: '', component: charts_template_1.TemplateView, children: [
                            { path: '', component: charts_empty_dashboard_1.EmptyDashboard },
                            { path: ':groupName', component: charts_dashboard_1.Dashboard }
                        ]
                    }])
            ],
            declarations: [charts_dashboard_1.Dashboard, charts_empty_dashboard_1.EmptyDashboard, charts_template_1.TemplateView],
            providers: PROVIDERS
        }), 
        __metadata('design:paramtypes', [])
    ], DashboardModule);
    return DashboardModule;
}());
exports.DashboardModule = DashboardModule;


/***/ },

/***/ "./src/app/charts/charts.template.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var TemplateView = (function () {
    function TemplateView() {
    }
    TemplateView = __decorate([
        core_1.Component({
            moduleId: module.i,
            template: __webpack_require__("./src/app/charts/templates/template.html"),
        }), 
        __metadata('design:paramtypes', [])
    ], TemplateView);
    return TemplateView;
}());
exports.TemplateView = TemplateView;


/***/ },

/***/ "./src/app/charts/templates/css/dashboard.css":
/***/ function(module, exports, __webpack_require__) {


        var result = __webpack_require__("./node_modules/css-loader/index.js!./src/app/charts/templates/css/dashboard.css");

        if (typeof result === "string") {
            module.exports = result;
        } else {
            module.exports = result.toString();
        }
    

/***/ },

/***/ "./src/app/charts/templates/dashboard.html":
/***/ function(module, exports) {

module.exports = "<!-- Modal for entity appending -->\r\n<div class=\"modal fade\" id=\"addChartModal\" role=\"dialog\" aria-labelledby=\"addChartLabel\" *ngIf=\"selectedChartType != ''\">\r\n  <div class=\"modal-dialog modal-xlg modal-lg\" role=\"document\">\r\n    <div class=\"modal-content\">\r\n      <div class=\"modal-header\">\r\n        <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>\r\n        <h4 class=\"modal-title leftAlign\" id=\"addChartLabel\">Visualization wizard</h4>\r\n      </div>\r\n      <br/>\r\n      <div class=\"modal-body\">\r\n        <div id=\"smartwizardForChart\" style=\"height: 95%\">\r\n          <ul>\r\n            <li>\r\n              <a [attr.href]=\"'#component'\">\r\n                Chart data object<br />\r\n                <small>Select group or instances</small>\r\n              </a>\r\n            </li>\r\n            <li>\r\n              <a [attr.href]=\"'#metric'\">\r\n                Metric<br />\r\n                <small>Choose metric</small>\r\n              </a>\r\n            </li>\r\n            <li>\r\n              <a [attr.href]=\"'#save'\">\r\n                Save<br />\r\n                <small>Add to dashboard</small>\r\n              </a>\r\n            </li>\r\n          </ul>\r\n\r\n          <div style=\"height:100%\">\r\n            <div id=\"component\" class=\"row\" style=\"margin-top: 70px;\">\r\n              <div class=\"row\" *ngIf=\"isInstancesSupported && (instances | async)?.length > 0\">\r\n                <div class=\"item form-group\">\r\n                  <label\r\n                          class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                          for=\"componentSelect\"\r\n                          style=\"margin-top: 7px;\">\r\n                    Build chart on resource group\r\n                  </label>\r\n                  <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                    <ui-switch\r\n                            [(ngModel)]=\"useGroup\"\r\n                            (ngModelChange)=\"triggerUseGroup($event)\"\r\n                            [disabled]=\"isInstancesSupported && (components | async)?.length == 0\"\r\n                            [size]=\"'small'\">\r\n                    </ui-switch>\r\n                  </div>\r\n                </div>\r\n              </div>\r\n\r\n              <div class=\"row\" *ngIf=\"useGroup\" style=\"margin-top: 15px;\">\r\n                <div class=\"item form-group\">\r\n                  <label\r\n                          class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                          for=\"componentSelect\"\r\n                          style=\"margin-top: 7px;\">\r\n                    Group\r\n                  </label>\r\n                  <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                    <select class=\"form-control\" [(ngModel)]=\"selectedComponent\" id=\"componentSelect\">\r\n                      <option *ngFor=\"let component of components | async\" [ngValue]=\"component\">{{component}}</option>\r\n                    </select>\r\n                  </div>\r\n                </div>\r\n              </div>\r\n\r\n              <div class=\"row\" *ngIf=\"!useGroup\" style=\"margin-top: 15px;\">\r\n                <div class=\"item form-group\">\r\n                  <label\r\n                          class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                          for=\"instancesSelect\"\r\n                          style=\"margin-top: 7px;\">\r\n                    Instances\r\n                  </label>\r\n\r\n                  <div class=\"col-md-6 col-sm-6 col-xs-12\" >\r\n                    <select class=\"select2_multiple form-control\" id=\"instancesSelect\" multiple=\"multiple\">\r\n                      <option *ngFor=\"let instance of instances | async\" [attr.selected]=\"isInstanceSelected(instance) ? true : null\">{{instance}}</option>\r\n                    </select>\r\n                  </div>\r\n                </div>\r\n              </div>\r\n\r\n            </div>\r\n\r\n            <div id=\"metric\" class=\"row\" style=\"margin-top: 10px;\">\r\n              <div *ngIf=\"selectedChartType != 'scaleIn' && selectedChartType != 'scaleOut'\">\r\n                <div class=\"col-md-12 col-sm-12 col-xs-12 text-center\" style=\"margin-top:100px\" *ngIf=\"(metrics | async) == undefined || (metrics | async).length == 0\">\r\n                  <strong>No metrics are found - cannot proceed</strong>\r\n                </div>\r\n                <div  *ngIf=\"(metrics | async) != undefined && (metrics | async).length > 0\">\r\n                <div class=\"bs-example\" data-example-id=\"simple-jumbotron\" *ngIf=\"selectedMetric != undefined\">\r\n                  <div class=\"jumbotron\">\r\n                    <p>\r\n                      <strong>Name: </strong>{{selectedMetric.name}}\r\n                    </p>\r\n                    <p *ngIf=\"selectedMetric.type != undefined && selectedMetric.type.length > 0\">\r\n                      <strong>Type: </strong>{{selectedMetric.type}}\r\n                    </p>\r\n                    <p *ngIf=\"selectedMetric.unitOfMeasurement != undefined && selectedMetric.unitOfMeasurement.length > 0\">\r\n                      <strong>Unit of measurement: </strong>{{selectedMetric.unitOfMeasurement}}\r\n                    </p>\r\n                    <p *ngIf=\"selectedMetric.description != undefined && selectedMetric.description.length > 0\">\r\n                      <strong>Description: </strong>{{selectedMetric.description}}\r\n                    </p>\r\n                  </div>\r\n                </div>\r\n                <div class=\"item form-group\">\r\n                  <label\r\n                          class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                          for=\"metricSelect\"\r\n                          style=\"margin-top: 7px;\">\r\n                    Select metric <span class=\"required\">*</span>\r\n                  </label>\r\n                  <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                    <select class=\"form-control\" [(ngModel)]=\"selectedMetric\" id=\"metricSelect\">\r\n                      <option *ngFor=\"let metric of metrics | async\" [ngValue]=\"metric\">{{metric.name}}</option>\r\n                    </select>\r\n                  </div>\r\n                </div>\r\n              </div>\r\n              </div>\r\n              <div *ngIf=\"selectedChartType == 'scaleIn' || selectedChartType == 'scaleOut'\" style=\"margin-top:100px\">\r\n                <div class=\"item form-group\">\r\n                  <label\r\n                          class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                          for=\"rateMetricSelect\"\r\n                          style=\"margin-top: 7px;\">\r\n                    Select rate metrics (multiple selection) <span class=\"required\">*</span>\r\n                  </label>\r\n                  <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                    <select class=\"select2_multiple form-control\" id=\"rateMetricSelect\"  multiple=\"multiple\">\r\n                      <option *ngFor=\"let rateMetric of rateMetrics\" [attr.selected]=\"isRateMetricSelected(rateMetric.id) ? true : null\">{{rateMetric.id}}</option>\r\n                    </select>\r\n                  </div>\r\n                </div>\r\n              </div>\r\n            </div>\r\n\r\n            <div id=\"save\" style=\"margin-top: 40px;\">\r\n\r\n              <div class=\"row\" *ngIf=\"selectedChartType == 'line' || selectedChartType == 'resources' || selectedChartType == 'scaleIn' || selectedChartType == 'scaleOut'\">\r\n                <div class=\"item form-group col-md-offset-3\">\r\n                  <label\r\n                          class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                          for=\"lineChartIntervalSelect\"\r\n                          style=\"margin-top: 7px;\">\r\n                    Select time interval <span class=\"required\">*</span>\r\n                  </label>\r\n                  <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                    <select class=\"form-control\" [(ngModel)]=\"timeInterval\" id=\"lineChartIntervalSelect\">\r\n                      <option *ngFor=\"let interval of intervals\" [ngValue]=\"interval.id\">{{interval.description}}</option>\r\n                    </select>\r\n                  </div>\r\n                </div>\r\n              </div>\r\n\r\n              <div class=\"row\" *ngIf=\"selectedChartType == 'scaleIn' || selectedChartType == 'scaleOut'\" style=\"margin-top: 15px;\">\r\n                <div class=\"item form-group col-md-offset-3\" >\r\n                  <label\r\n                          class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                          for=\"rateIntervalSelect\"\r\n                          style=\"margin-top: 7px;\">\r\n                    Select rate interval <span class=\"required\">*</span>\r\n                  </label>\r\n                  <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                    <select class=\"form-control\" [(ngModel)]=\"rateInterval\" id=\"rateIntervalSelect\">\r\n                      <option *ngFor=\"let interval of rateIntervals\" [ngValue]=\"interval.additionalId\">{{interval.description}}</option>\r\n                    </select>\r\n                  </div>\r\n                </div>\r\n              </div>\r\n\r\n              <br/>\r\n\r\n              <div class=\"row\">\r\n                <div class=\"item form-group col-md-offset-3\">\r\n                  <label\r\n                          class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                          style=\"margin-top: 7px;\"\r\n                          for=\"chartNameInput\">\r\n                    Chart name <span class=\"required\">*</span>\r\n                  </label>\r\n                  <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                    <input type=\"text\" class=\"form-control\" id=\"chartNameInput\" placeholder=\"Chart name\" [(ngModel)]=\"chartName\" [disabled]=\"!isNewChart()\">\r\n                  </div>\r\n                </div>\r\n              </div>\r\n\r\n              <div class=\"row\" style=\"margin-top: 15px\">\r\n                <button\r\n                        *ngIf=\"isNewChart()\"\r\n                        type=\"button\"\r\n                        class=\"btn btn-primary col-md-4 col-md-offset-4\"\r\n                        (click)=\"addChartToDashboard()\">\r\n                  Append to dashboard\r\n                </button>\r\n\r\n                <button\r\n                        *ngIf=\"!isNewChart()\"\r\n                        type=\"button\"\r\n                        class=\"btn btn-primary col-md-4 col-md-offset-4\"\r\n                        (click)=\"saveChart()\">\r\n                  Save the chart\r\n                </button>\r\n              </div>\r\n            </div>\r\n          </div>\r\n        </div>\r\n      </div>\r\n    </div>\r\n  </div>\r\n</div>\r\n\r\n<div *ngIf=\"isAllowed() && ((components | async)?.length > 0 || (instances | async)?.length > 0)\">\r\n  <img src=\"/snamp/assets/img/add.png\" *ngIf=\"!allCharts || allCharts.length == 0\" class=\"chartMenuImage\"/>\r\n  <ul id=\"chartMenu\" *ngIf=\"isAllowed()\">\r\n    <li [tooltip]=\"'Pie chart'\" (click)=\"appendChartClicked('doughnut')\"><i class=\"fa fa-pie-chart\" aria-hidden=\"true\"></i></li>\r\n    <li [tooltip]=\"'Horizontal bar chart'\" (click)=\"appendChartClicked('horizontalBar')\"><i class=\"fa fa-align-left \" aria-hidden=\"true\"></i></li>\r\n    <li [tooltip]=\"'Vertical bar chart'\" (click)=\"appendChartClicked('bar')\"><i class=\"fa fa-bar-chart\" aria-hidden=\"true\"></i></li>\r\n    <li [tooltip]=\"'Simple line chart'\" (click)=\"appendChartClicked('line')\"><i class=\"fa fa-line-chart\" aria-hidden=\"true\"></i></li>\r\n    <li [tooltip]=\"'Panel with values'\" (click)=\"appendChartClicked('panel')\"><i class=\"fa fa-columns\" aria-hidden=\"true\"></i></li>\r\n    <li [tooltip]=\"'Resource group health status'\" (click)=\"appendChartClicked('statuses')\"><i class=\"fa fa-lightbulb-o\" aria-hidden=\"true\"></i></li>\r\n    <li [tooltip]=\"'Resources count in a group'\" (click)=\"appendChartClicked('resources')\"><i class=\"fa fa-sitemap\" aria-hidden=\"true\"></i></li>\r\n    <li [tooltip]=\"'Rate of downscale'\" (click)=\"appendChartClicked('scaleIn')\"><i class=\"fa fa-arrow-circle-down\" aria-hidden=\"true\"></i></li>\r\n    <li [tooltip]=\"'Rate of upscale'\" (click)=\"appendChartClicked('scaleOut')\"><i class=\"fa fa-arrow-circle-up\" aria-hidden=\"true\"></i></li>\r\n    <li [tooltip]=\"'Voting chart'\" (click)=\"appendChartClicked('voting')\"><i class=\"fa fa-arrows-v\" aria-hidden=\"true\"></i></li>\r\n  </ul>\r\n</div>\r\n\r\n<div class=\"right_col\" role=\"main\" style=\"min-height: 949px;\">\r\n\r\n    <div class=\"row\" style=\"padding-right: 10px;\">\r\n      <div class=\"col-md-4\" style=\"padding-top: 20px;\">\r\n        <h3>{{groupName}}</h3>\r\n      </div>\r\n\r\n      <div class=\"col-md-6\"></div>\r\n\r\n      <div class=\"col-md-2\" style=\"padding-top: 20px;\">\r\n\r\n        <div class=\"row\" style=\"float: right\" *ngIf=\"isAllowed()\">\r\n          <button class=\"btn btn-danger\" (click)=\"removeDashboard()\"><i class=\"fa fa-trash\"></i></button>\r\n        </div>\r\n      </div>\r\n    </div>\r\n\r\n    <hr/>\r\n\r\n\r\n    <div class=\"clearfix\"></div>\r\n\r\n    <div class=\"row\" style=\"margin-top: 30px\">\r\n\r\n      <div class='container col-md-12' [ngGrid]=\"gridConfig\"  style=\"min-height: 900px; width: calc(100% - 45px) !important;\">\r\n\r\n          <div class=\"x_panel\"\r\n               *ngFor=\"let chart of allCharts; let i = index;\"\r\n               [(ngGridItem)]=\"chart.preferences['gridcfg']\"\r\n               (onChangeStop)=\"onChangeStop(i, $event)\"\r\n               [attr.chartPanelId]=\"chart.id\">\r\n\r\n              <div class=\"x_title title handle\">\r\n                <h2>{{chart.name}}</h2>\r\n                <h5 *ngIf=\"chart.updateStopped\">(Paused at {{chart.pausedTime | date:'dd/MM/y HH:mm:ss'}})</h5>\r\n                <ul class=\"nav navbar-right panel_toolbox\">\r\n                  <li>\r\n                    <a (click)=\"resetChart(chart)\" *ngIf=\"isAllowed() && isResetable(chart)\"><i class=\"fa fa-refresh\"></i></a>\r\n                    <a (click)=\"modifyChart(chart)\" *ngIf=\"isAllowed()\"><i class=\"fa fa-wrench\"></i></a>\r\n                    <a [attr.id]=\"'toggleDraw' + chart.id\" class=\"toggle-link\" (click)=\"toggleDrawingChart(chart)\"><i class=\"fa fa-pause-circle-o\"></i></a>\r\n                    <a class=\"close-link\" *ngIf=\"isAllowed()\" (click)=\"removeChart(chart.name)\"><i class=\"fa fa-close\"></i></a>\r\n                  </li>\r\n                </ul>\r\n                <div class=\"clearfix\"></div>\r\n              </div>\r\n              <div *ngIf=\"isSvgType(chart)\"\r\n                      class=\"x_content\"\r\n                      style=\"position: relative; width: 100%; height: calc(100% - 10px) !important;\">\r\n                <svg [attr.id]=\"chart.id\"></svg>\r\n              </div>\r\n              <div *ngIf=\"isCanvasType(chart)\"\r\n                   class=\"x_content\"\r\n                   style=\"position: relative; width: 100%; height: calc(100% - 30px) !important;\">\r\n                <canvas [attr.id]=\"chart.id\" height=\"80\"></canvas>\r\n              </div>\r\n              <div *ngIf=\"isDivType(chart)\"\r\n                 class=\"x_content\"\r\n                 style=\"position: relative; width: 100%; height: calc(100% - 30px) !important;\">\r\n                  <div [attr.id]=\"chart.id\" class=\"tableWrapper\"></div>\r\n               </div>\r\n            </div>\r\n      </div>\r\n    </div>\r\n</div>"

/***/ },

/***/ "./src/app/charts/templates/empty.html":
/***/ function(module, exports) {

module.exports = "<div class=\"right_col\" role=\"main\" style=\"min-height: 949px;\">\r\n\r\n    <div class=\"row\" style=\"padding-right: 10px;\">\r\n        <div class=\"col-md-4\" style=\"padding-top: 20px;\">\r\n            <h3>Dashboards settings</h3>\r\n        </div>\r\n\r\n        <div class=\"col-md-8\"></div>\r\n    </div>\r\n\r\n    <hr/>\r\n\r\n    <div style=\"float: left !important;\" >\r\n        <div>\r\n            <h5>There are no active dashboards - you can create one with the corresponding menu item</h5>\r\n        </div>\r\n    </div>\r\n</div>\r\n"

/***/ },

/***/ "./src/app/charts/templates/template.html":
/***/ function(module, exports) {

module.exports = "<router-outlet></router-outlet>"

/***/ },

/***/ "./src/app/controls/nggrid/components/NgGridPlaceholder.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var NgGridPlaceholder = (function () {
    function NgGridPlaceholder(_ngEl, _renderer) {
        this._ngEl = _ngEl;
        this._renderer = _renderer;
    }
    NgGridPlaceholder.prototype.registerGrid = function (ngGrid) {
        this._ngGrid = ngGrid;
    };
    NgGridPlaceholder.prototype.ngOnInit = function () {
        this._renderer.setElementClass(this._ngEl.nativeElement, 'grid-placeholder', true);
        if (this._ngGrid.autoStyle)
            this._renderer.setElementStyle(this._ngEl.nativeElement, 'position', 'absolute');
    };
    NgGridPlaceholder.prototype.setSize = function (newSize) {
        this._size = newSize;
        this._recalculateDimensions();
    };
    NgGridPlaceholder.prototype.setGridPosition = function (newPosition) {
        this._position = newPosition;
        this._recalculatePosition();
    };
    NgGridPlaceholder.prototype.setCascadeMode = function (cascade) {
        this._cascadeMode = cascade;
        switch (cascade) {
            case 'up':
            case 'left':
            default:
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'left', '0px');
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'top', '0px');
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'right', null);
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'bottom', null);
                break;
            case 'right':
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'right', '0px');
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'top', '0px');
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'left', null);
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'bottom', null);
                break;
            case 'down':
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'left', '0px');
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'bottom', '0px');
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'right', null);
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'top', null);
                break;
        }
    };
    //	Private methods
    NgGridPlaceholder.prototype._setDimensions = function (w, h) {
        this._renderer.setElementStyle(this._ngEl.nativeElement, 'width', w + 'px');
        this._renderer.setElementStyle(this._ngEl.nativeElement, 'height', h + 'px');
    };
    NgGridPlaceholder.prototype._setPosition = function (x, y) {
        switch (this._cascadeMode) {
            case 'up':
            case 'left':
            default:
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'transform', 'translate(' + x + 'px, ' + y + 'px)');
                break;
            case 'right':
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'transform', 'translate(' + -x + 'px, ' + y + 'px)');
                break;
            case 'down':
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'transform', 'translate(' + x + 'px, ' + -y + 'px)');
                break;
        }
    };
    NgGridPlaceholder.prototype._recalculatePosition = function () {
        var x = (this._ngGrid.colWidth + this._ngGrid.marginLeft + this._ngGrid.marginRight) * (this._position.col - 1) + this._ngGrid.marginLeft;
        var y = (this._ngGrid.rowHeight + this._ngGrid.marginTop + this._ngGrid.marginBottom) * (this._position.row - 1) + this._ngGrid.marginTop;
        this._setPosition(x, y);
    };
    NgGridPlaceholder.prototype._recalculateDimensions = function () {
        var w = (this._ngGrid.colWidth * this._size.x) + ((this._ngGrid.marginLeft + this._ngGrid.marginRight) * (this._size.x - 1));
        var h = (this._ngGrid.rowHeight * this._size.y) + ((this._ngGrid.marginTop + this._ngGrid.marginBottom) * (this._size.y - 1));
        this._setDimensions(w, h);
    };
    NgGridPlaceholder = __decorate([
        core_1.Component({
            selector: 'ng-grid-placeholder',
            template: ''
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof core_1.ElementRef !== 'undefined' && core_1.ElementRef) === 'function' && _a) || Object, (typeof (_b = typeof core_1.Renderer !== 'undefined' && core_1.Renderer) === 'function' && _b) || Object])
    ], NgGridPlaceholder);
    return NgGridPlaceholder;
    var _a, _b;
}());
exports.NgGridPlaceholder = NgGridPlaceholder;


/***/ },

/***/ "./src/app/controls/nggrid/directives/NgGrid.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var NgGridPlaceholder_1 = __webpack_require__("./src/app/controls/nggrid/components/NgGridPlaceholder.ts");
var NgGrid = (function () {
    //	Constructor
    function NgGrid(_differs, _ngEl, _renderer, componentFactoryResolver, _containerRef) {
        this._differs = _differs;
        this._ngEl = _ngEl;
        this._renderer = _renderer;
        this.componentFactoryResolver = componentFactoryResolver;
        this._containerRef = _containerRef;
        //	Event Emitters
        this.onDragStart = new core_1.EventEmitter();
        this.onDrag = new core_1.EventEmitter();
        this.onDragStop = new core_1.EventEmitter();
        this.onResizeStart = new core_1.EventEmitter();
        this.onResize = new core_1.EventEmitter();
        this.onResizeStop = new core_1.EventEmitter();
        this.onItemChange = new core_1.EventEmitter();
        //	Public variables
        this.colWidth = 250;
        this.rowHeight = 250;
        this.minCols = 1;
        this.minRows = 1;
        this.marginTop = 10;
        this.marginRight = 10;
        this.marginBottom = 10;
        this.marginLeft = 10;
        this.isDragging = false;
        this.isResizing = false;
        this.autoStyle = true;
        this.resizeEnable = true;
        this.dragEnable = true;
        this.cascade = 'up';
        this.minWidth = 100;
        this.minHeight = 100;
        //	Private variables
        this._items = [];
        this._draggingItem = null;
        this._resizingItem = null;
        this._resizeDirection = null;
        this._itemGrid = {}; //{ 1: { 1: null } };
        this._maxCols = 0;
        this._maxRows = 0;
        this._visibleCols = 0;
        this._visibleRows = 0;
        this._setWidth = 250;
        this._setHeight = 250;
        this._posOffset = null;
        this._adding = false;
        this._placeholderRef = null;
        this._fixToGrid = false;
        this._autoResize = false;
        this._destroyed = false;
        this._maintainRatio = false;
        this._preferNew = false;
        this._zoomOnDrag = false;
        this._limitToScreen = false;
        this._curMaxRow = 0;
        this._curMaxCol = 0;
        this._dragReady = false;
        this._resizeReady = false;
        this._config = NgGrid.CONST_DEFAULT_CONFIG;
    }
    Object.defineProperty(NgGrid.prototype, "config", {
        //	[ng-grid] attribute handler
        set: function (v) {
            this.setConfig(v);
            if (this._differ == null && v != null) {
                this._differ = this._differs.find(this._config).create(null);
            }
        },
        enumerable: true,
        configurable: true
    });
    //	Public methods
    NgGrid.prototype.ngOnInit = function () {
        this._renderer.setElementClass(this._ngEl.nativeElement, 'grid', true);
        if (this.autoStyle)
            this._renderer.setElementStyle(this._ngEl.nativeElement, 'position', 'relative');
        this.setConfig(this._config);
    };
    NgGrid.prototype.ngOnDestroy = function () {
        this._destroyed = true;
    };
    NgGrid.prototype.setConfig = function (config) {
        this._config = config;
        var maxColRowChanged = false;
        for (var x in config) {
            var val = config[x];
            var intVal = !val ? 0 : parseInt(val);
            switch (x) {
                case 'margins':
                    this.setMargins(val);
                    break;
                case 'col_width':
                    this.colWidth = Math.max(intVal, 1);
                    break;
                case 'row_height':
                    this.rowHeight = Math.max(intVal, 1);
                    break;
                case 'auto_style':
                    this.autoStyle = val ? true : false;
                    break;
                case 'auto_resize':
                    this._autoResize = val ? true : false;
                    break;
                case 'draggable':
                    this.dragEnable = val ? true : false;
                    break;
                case 'resizable':
                    this.resizeEnable = val ? true : false;
                    break;
                case 'max_rows':
                    maxColRowChanged = maxColRowChanged || this._maxRows != intVal;
                    this._maxRows = intVal < 0 ? 0 : intVal;
                    break;
                case 'max_cols':
                    maxColRowChanged = maxColRowChanged || this._maxCols != intVal;
                    this._maxCols = intVal < 0 ? 0 : intVal;
                    break;
                case 'visible_rows':
                    this._visibleRows = Math.max(intVal, 0);
                    break;
                case 'visible_cols':
                    this._visibleCols = Math.max(intVal, 0);
                    break;
                case 'min_rows':
                    this.minRows = Math.max(intVal, 1);
                    break;
                case 'min_cols':
                    this.minCols = Math.max(intVal, 1);
                    break;
                case 'min_height':
                    this.minHeight = Math.max(intVal, 1);
                    break;
                case 'min_width':
                    this.minWidth = Math.max(intVal, 1);
                    break;
                case 'zoom_on_drag':
                    this._zoomOnDrag = val ? true : false;
                    break;
                case 'cascade':
                    if (this.cascade != val) {
                        this.cascade = val;
                        this._cascadeGrid();
                    }
                    break;
                case 'fix_to_grid':
                    this._fixToGrid = val ? true : false;
                    break;
                case 'maintain_ratio':
                    this._maintainRatio = val ? true : false;
                    break;
                case 'prefer_new':
                    this._preferNew = val ? true : false;
                    break;
                case 'limit_to_screen':
                    this._limitToScreen = val ? true : false;
                    break;
            }
        }
        if (this._maintainRatio) {
            if (this.colWidth && this.rowHeight) {
                this._aspectRatio = this.colWidth / this.rowHeight;
            }
            else {
                this._maintainRatio = false;
            }
        }
        if (maxColRowChanged) {
            if (this._maxCols > 0 && this._maxRows > 0) {
                switch (this.cascade) {
                    case 'left':
                    case 'right':
                        this._maxCols = 0;
                        break;
                    case 'up':
                    case 'down':
                    default:
                        this._maxRows = 0;
                        break;
                }
            }
            for (var _i = 0, _a = this._items; _i < _a.length; _i++) {
                var item = _a[_i];
                var pos = item.getGridPosition();
                var dims = item.getSize();
                this._removeFromGrid(item);
                if (this._maxCols > 0 && dims.x > this._maxCols) {
                    dims.x = this._maxCols;
                    item.setSize(dims);
                }
                else if (this._maxRows > 0 && dims.y > this._maxRows) {
                    dims.y = this._maxRows;
                    item.setSize(dims);
                }
                if (this._hasGridCollision(pos, dims) || !this._isWithinBounds(pos, dims)) {
                    var newPosition = this._fixGridPosition(pos, dims);
                    item.setGridPosition(newPosition);
                }
                this._addToGrid(item);
            }
            this._cascadeGrid();
        }
        this._calculateRowHeight();
        this._calculateColWidth();
        var maxWidth = this._maxCols * this.colWidth;
        var maxHeight = this._maxRows * this.rowHeight;
        if (maxWidth > 0 && this.minWidth > maxWidth)
            this.minWidth = 0.75 * this.colWidth;
        if (maxHeight > 0 && this.minHeight > maxHeight)
            this.minHeight = 0.75 * this.rowHeight;
        if (this.minWidth > this.colWidth)
            this.minCols = Math.max(this.minCols, Math.ceil(this.minWidth / this.colWidth));
        if (this.minHeight > this.rowHeight)
            this.minRows = Math.max(this.minRows, Math.ceil(this.minHeight / this.rowHeight));
        if (this._maxCols > 0 && this.minCols > this._maxCols)
            this.minCols = 1;
        if (this._maxRows > 0 && this.minRows > this._maxRows)
            this.minRows = 1;
        this._updateRatio();
        for (var _b = 0, _c = this._items; _b < _c.length; _b++) {
            var item = _c[_b];
            this._removeFromGrid(item);
            item.setCascadeMode(this.cascade);
        }
        this._updateLimit();
        for (var _d = 0, _e = this._items; _d < _e.length; _d++) {
            var item = _e[_d];
            item.recalculateSelf();
            this._addToGrid(item);
        }
        this._cascadeGrid();
        this._updateSize();
    };
    NgGrid.prototype.getItemPosition = function (index) {
        return this._items[index].getGridPosition();
    };
    NgGrid.prototype.getItemSize = function (index) {
        return this._items[index].getSize();
    };
    NgGrid.prototype.ngDoCheck = function () {
        if (this._differ != null) {
            var changes = this._differ.diff(this._config);
            if (changes != null) {
                this._applyChanges(changes);
                return true;
            }
        }
        return false;
    };
    NgGrid.prototype.setMargins = function (margins) {
        this.marginTop = Math.max(parseInt(margins[0]), 0);
        this.marginRight = margins.length >= 2 ? Math.max(parseInt(margins[1]), 0) : this.marginTop;
        this.marginBottom = margins.length >= 3 ? Math.max(parseInt(margins[2]), 0) : this.marginTop;
        this.marginBottom = margins.length >= 3 ? Math.max(parseInt(margins[2]), 0) : this.marginTop;
        this.marginLeft = margins.length >= 4 ? Math.max(parseInt(margins[3]), 0) : this.marginRight;
    };
    NgGrid.prototype.enableDrag = function () {
        this.dragEnable = true;
    };
    NgGrid.prototype.disableDrag = function () {
        this.dragEnable = false;
    };
    NgGrid.prototype.enableResize = function () {
        this.resizeEnable = true;
    };
    NgGrid.prototype.disableResize = function () {
        this.resizeEnable = false;
    };
    NgGrid.prototype.addItem = function (ngItem) {
        ngItem.setCascadeMode(this.cascade);
        if (!this._preferNew) {
            var newPos = this._fixGridPosition(ngItem.getGridPosition(), ngItem.getSize());
            ngItem.savePosition(newPos);
        }
        this._items.push(ngItem);
        this._addToGrid(ngItem);
        ngItem.recalculateSelf();
        ngItem.onCascadeEvent();
        this._emitOnItemChange();
    };
    NgGrid.prototype.removeItem = function (ngItem) {
        this._removeFromGrid(ngItem);
        for (var x = 0; x < this._items.length; x++) {
            if (this._items[x] == ngItem) {
                this._items.splice(x, 1);
            }
        }
        if (this._destroyed)
            return;
        this._cascadeGrid();
        this._updateSize();
        this._items.forEach(function (item) { return item.recalculateSelf(); });
        this._emitOnItemChange();
    };
    NgGrid.prototype.updateItem = function (ngItem) {
        this._removeFromGrid(ngItem);
        this._addToGrid(ngItem);
        this._cascadeGrid();
        this._updateSize();
        ngItem.onCascadeEvent();
    };
    NgGrid.prototype.triggerCascade = function () {
        this._cascadeGrid(null, null, false);
    };
    NgGrid.prototype.resizeEventHandler = function (e) {
        this._calculateColWidth();
        this._calculateRowHeight();
        this._updateRatio();
        for (var _i = 0, _a = this._items; _i < _a.length; _i++) {
            var item = _a[_i];
            this._removeFromGrid(item);
        }
        this._updateLimit();
        for (var _b = 0, _c = this._items; _b < _c.length; _b++) {
            var item = _c[_b];
            this._addToGrid(item);
            item.recalculateSelf();
        }
        this._updateSize();
    };
    NgGrid.prototype.mouseDownEventHandler = function (e) {
        var mousePos = this._getMousePosition(e);
        var item = this._getItemFromPosition(mousePos);
        if (item != null) {
            if (this.resizeEnable && item.canResize(e)) {
                this._resizeReady = true;
            }
            else if (this.dragEnable && item.canDrag(e)) {
                this._dragReady = true;
            }
        }
        return true;
    };
    NgGrid.prototype.mouseUpEventHandler = function (e) {
        if (this.isDragging) {
            this._dragStop(e);
            return false;
        }
        else if (this.isResizing) {
            this._resizeStop(e);
            return false;
        }
        else if (this._dragReady || this._resizeReady) {
            this._dragReady = false;
            this._resizeReady = false;
        }
        return true;
    };
    NgGrid.prototype.mouseMoveEventHandler = function (e) {
        if (this._resizeReady) {
            this._resizeStart(e);
            return false;
        }
        else if (this._dragReady) {
            this._dragStart(e);
            return false;
        }
        if (this.isDragging) {
            this._drag(e);
            return false;
        }
        else if (this.isResizing) {
            this._resize(e);
            return false;
        }
        else {
            var mousePos = this._getMousePosition(e);
            var item = this._getItemFromPosition(mousePos);
            if (item) {
                item.onMouseMove(e);
            }
        }
        return true;
    };
    //	Private methods
    NgGrid.prototype._calculateColWidth = function () {
        if (this._autoResize) {
            if (this._maxCols > 0 || this._visibleCols > 0) {
                var maxCols = this._maxCols > 0 ? this._maxCols : this._visibleCols;
                var maxWidth = this._ngEl.nativeElement.getBoundingClientRect().width;
                var colWidth = Math.floor(maxWidth / maxCols);
                colWidth -= (this.marginLeft + this.marginRight);
                if (colWidth > 0)
                    this.colWidth = colWidth;
                if (this.colWidth < this.minWidth || this.minCols > this._config.min_cols) {
                    this.minCols = Math.max(this._config.min_cols, Math.ceil(this.minWidth / this.colWidth));
                }
            }
        }
    };
    NgGrid.prototype._calculateRowHeight = function () {
        if (this._autoResize) {
            if (this._maxRows > 0 || this._visibleRows > 0) {
                var maxRows = this._maxRows > 0 ? this._maxRows : this._visibleRows;
                var maxHeight = window.innerHeight - this.marginTop - this.marginBottom;
                var rowHeight = Math.max(Math.floor(maxHeight / maxRows), this.minHeight);
                rowHeight -= (this.marginTop + this.marginBottom);
                if (rowHeight > 0)
                    this.rowHeight = rowHeight;
                if (this.rowHeight < this.minHeight || this.minRows > this._config.min_rows) {
                    this.minRows = Math.max(this._config.min_rows, Math.ceil(this.minHeight / this.rowHeight));
                }
            }
        }
    };
    NgGrid.prototype._updateRatio = function () {
        if (this._autoResize && this._maintainRatio) {
            if (this._maxCols > 0 && this._visibleRows <= 0) {
                this.rowHeight = this.colWidth / this._aspectRatio;
            }
            else if (this._maxRows > 0 && this._visibleCols <= 0) {
                this.colWidth = this._aspectRatio * this.rowHeight;
            }
            else if (this._maxCols == 0 && this._maxRows == 0) {
                if (this._visibleCols > 0) {
                    this.rowHeight = this.colWidth / this._aspectRatio;
                }
                else if (this._visibleRows > 0) {
                    this.colWidth = this._aspectRatio * this.rowHeight;
                }
            }
        }
    };
    NgGrid.prototype._updateLimit = function () {
        if (!this._autoResize && this._limitToScreen) {
            this._limitGrid(this._getContainerColumns());
        }
    };
    NgGrid.prototype._applyChanges = function (changes) {
        var _this = this;
        changes.forEachAddedItem(function (record) { _this._config[record.key] = record.currentValue; });
        changes.forEachChangedItem(function (record) { _this._config[record.key] = record.currentValue; });
        changes.forEachRemovedItem(function (record) { delete _this._config[record.key]; });
        this.setConfig(this._config);
    };
    NgGrid.prototype._resizeStart = function (e) {
        if (this.resizeEnable) {
            var mousePos = this._getMousePosition(e);
            var item = this._getItemFromPosition(mousePos);
            if (item) {
                item.startMoving();
                this._resizingItem = item;
                this._resizeDirection = item.canResize(e);
                this._removeFromGrid(item);
                this._createPlaceholder(item);
                this.isResizing = true;
                this._resizeReady = false;
                this.onResizeStart.emit(item);
                item.onResizeStartEvent();
            }
        }
    };
    NgGrid.prototype._dragStart = function (e) {
        if (this.dragEnable) {
            var mousePos = this._getMousePosition(e);
            var item = this._getItemFromPosition(mousePos);
            var itemPos = item.getPosition();
            var pOffset = { 'left': (mousePos.left - itemPos.left), 'top': (mousePos.top - itemPos.top) };
            item.startMoving();
            this._draggingItem = item;
            this._posOffset = pOffset;
            this._removeFromGrid(item);
            this._createPlaceholder(item);
            this.isDragging = true;
            this._dragReady = false;
            this.onDragStart.emit(item);
            item.onDragStartEvent();
            if (this._zoomOnDrag) {
                this._zoomOut();
            }
        }
    };
    NgGrid.prototype._zoomOut = function () {
        this._renderer.setElementStyle(this._ngEl.nativeElement, 'transform', 'scale(0.5, 0.5)');
    };
    NgGrid.prototype._resetZoom = function () {
        this._renderer.setElementStyle(this._ngEl.nativeElement, 'transform', '');
    };
    NgGrid.prototype._drag = function (e) {
        if (this.isDragging) {
            if (window.getSelection) {
                if (window.getSelection().empty) {
                    window.getSelection().empty();
                }
                else if (window.getSelection().removeAllRanges) {
                    window.getSelection().removeAllRanges();
                }
            }
            else if (document.selection) {
                document.selection.empty();
            }
            var mousePos = this._getMousePosition(e);
            var newL = (mousePos.left - this._posOffset.left);
            var newT = (mousePos.top - this._posOffset.top);
            var itemPos = this._draggingItem.getGridPosition();
            var gridPos = this._calculateGridPosition(newL, newT);
            var dims = this._draggingItem.getSize();
            if (!this._isWithinBoundsX(gridPos, dims))
                gridPos.col = this._maxCols - (dims.x - 1);
            if (!this._isWithinBoundsY(gridPos, dims))
                gridPos.row = this._maxRows - (dims.y - 1);
            if (!this._autoResize && this._limitToScreen) {
                if ((gridPos.col + dims.x - 1) > this._getContainerColumns()) {
                    gridPos.col = this._getContainerColumns() - (dims.x - 1);
                }
            }
            if (gridPos.col != itemPos.col || gridPos.row != itemPos.row) {
                this._draggingItem.setGridPosition(gridPos, this._fixToGrid);
                this._placeholderRef.instance.setGridPosition(gridPos);
                if (['up', 'down', 'left', 'right'].indexOf(this.cascade) >= 0) {
                    this._fixGridCollisions(gridPos, dims, true);
                    this._cascadeGrid(gridPos, dims);
                }
            }
            if (!this._fixToGrid) {
                this._draggingItem.setPosition(newL, newT);
            }
            this.onDrag.emit(this._draggingItem);
            this._draggingItem.onDragEvent();
        }
    };
    NgGrid.prototype._resize = function (e) {
        if (this.isResizing) {
            if (window.getSelection) {
                if (window.getSelection().empty) {
                    window.getSelection().empty();
                }
                else if (window.getSelection().removeAllRanges) {
                    window.getSelection().removeAllRanges();
                }
            }
            else if (document.selection) {
                document.selection.empty();
            }
            var mousePos = this._getMousePosition(e);
            var itemPos = this._resizingItem.getPosition();
            var itemDims = this._resizingItem.getDimensions();
            var newW = this._resizeDirection == 'height' ? itemDims.width : (mousePos.left - itemPos.left + 10);
            var newH = this._resizeDirection == 'width' ? itemDims.height : (mousePos.top - itemPos.top + 10);
            if (newW < this.minWidth)
                newW = this.minWidth;
            if (newH < this.minHeight)
                newH = this.minHeight;
            if (newW < this._resizingItem.minWidth)
                newW = this._resizingItem.minWidth;
            if (newH < this._resizingItem.minHeight)
                newH = this._resizingItem.minHeight;
            var calcSize = this._calculateGridSize(newW, newH);
            var itemSize = this._resizingItem.getSize();
            var iGridPos = this._resizingItem.getGridPosition();
            if (!this._isWithinBoundsX(iGridPos, calcSize))
                calcSize.x = (this._maxCols - iGridPos.col) + 1;
            if (!this._isWithinBoundsY(iGridPos, calcSize))
                calcSize.y = (this._maxRows - iGridPos.row) + 1;
            calcSize = this._resizingItem.fixResize(calcSize);
            if (calcSize.x != itemSize.x || calcSize.y != itemSize.y) {
                this._resizingItem.setSize(calcSize, false);
                this._placeholderRef.instance.setSize(calcSize);
                if (['up', 'down', 'left', 'right'].indexOf(this.cascade) >= 0) {
                    this._fixGridCollisions(iGridPos, calcSize, true);
                    this._cascadeGrid(iGridPos, calcSize);
                }
            }
            if (!this._fixToGrid)
                this._resizingItem.setDimensions(newW, newH);
            var bigGrid = this._maxGridSize(itemPos.left + newW + (2 * e.movementX), itemPos.top + newH + (2 * e.movementY));
            if (this._resizeDirection == 'height')
                bigGrid.x = iGridPos.col + itemSize.x;
            if (this._resizeDirection == 'width')
                bigGrid.y = iGridPos.row + itemSize.y;
            this.onResize.emit(this._resizingItem);
            this._resizingItem.onResizeEvent();
        }
    };
    NgGrid.prototype._dragStop = function (e) {
        if (this.isDragging) {
            this.isDragging = false;
            var itemPos = this._draggingItem.getGridPosition();
            this._draggingItem.savePosition(itemPos);
            this._addToGrid(this._draggingItem);
            this._cascadeGrid();
            this._draggingItem.stopMoving();
            this._draggingItem.onDragStopEvent();
            this.onDragStop.emit(this._draggingItem);
            this._draggingItem = null;
            this._posOffset = null;
            this._placeholderRef.destroy();
            this._emitOnItemChange();
            if (this._zoomOnDrag) {
                this._resetZoom();
            }
        }
    };
    NgGrid.prototype._resizeStop = function (e) {
        if (this.isResizing) {
            this.isResizing = false;
            var itemDims = this._resizingItem.getSize();
            this._resizingItem.setSize(itemDims);
            this._addToGrid(this._resizingItem);
            this._cascadeGrid();
            this._resizingItem.stopMoving();
            this._resizingItem.onResizeStopEvent();
            this.onResizeStop.emit(this._resizingItem);
            this._resizingItem = null;
            this._resizeDirection = null;
            this._placeholderRef.destroy();
            this._emitOnItemChange();
        }
    };
    NgGrid.prototype._maxGridSize = function (w, h) {
        var sizex = Math.ceil(w / (this.colWidth + this.marginLeft + this.marginRight));
        var sizey = Math.ceil(h / (this.rowHeight + this.marginTop + this.marginBottom));
        return { 'x': sizex, 'y': sizey };
    };
    NgGrid.prototype._calculateGridSize = function (width, height) {
        width += this.marginLeft + this.marginRight;
        height += this.marginTop + this.marginBottom;
        var sizex = Math.max(this.minCols, Math.round(width / (this.colWidth + this.marginLeft + this.marginRight)));
        var sizey = Math.max(this.minRows, Math.round(height / (this.rowHeight + this.marginTop + this.marginBottom)));
        if (!this._isWithinBoundsX({ col: 1, row: 1 }, { x: sizex, y: sizey }))
            sizex = this._maxCols;
        if (!this._isWithinBoundsY({ col: 1, row: 1 }, { x: sizex, y: sizey }))
            sizey = this._maxRows;
        return { 'x': sizex, 'y': sizey };
    };
    NgGrid.prototype._calculateGridPosition = function (left, top) {
        var col = Math.max(1, Math.round(left / (this.colWidth + this.marginLeft + this.marginRight)) + 1);
        var row = Math.max(1, Math.round(top / (this.rowHeight + this.marginTop + this.marginBottom)) + 1);
        if (!this._isWithinBoundsX({ col: col, row: row }, { x: 1, y: 1 }))
            col = this._maxCols;
        if (!this._isWithinBoundsY({ col: col, row: row }, { x: 1, y: 1 }))
            row = this._maxRows;
        return { 'col': col, 'row': row };
    };
    NgGrid.prototype._hasGridCollision = function (pos, dims) {
        var positions = this._getCollisions(pos, dims);
        if (positions == null || positions.length == 0)
            return false;
        return positions.some(function (v) {
            return !(v === null);
        });
    };
    NgGrid.prototype._getCollisions = function (pos, dims) {
        var returns = [];
        for (var j = 0; j < dims.y; j++) {
            if (this._itemGrid[pos.row + j] != null) {
                for (var i = 0; i < dims.x; i++) {
                    if (this._itemGrid[pos.row + j][pos.col + i] != null) {
                        var item = this._itemGrid[pos.row + j][pos.col + i];
                        if (returns.indexOf(item) < 0)
                            returns.push(item);
                        var itemPos = item.getGridPosition();
                        var itemDims = item.getSize();
                        i = itemPos.col + itemDims.x - pos.col;
                    }
                }
            }
        }
        return returns;
    };
    NgGrid.prototype._fixGridCollisions = function (pos, dims, shouldSave) {
        if (shouldSave === void 0) { shouldSave = false; }
        while (this._hasGridCollision(pos, dims)) {
            var collisions = this._getCollisions(pos, dims);
            this._removeFromGrid(collisions[0]);
            var itemPos = collisions[0].getGridPosition();
            var itemDims = collisions[0].getSize();
            switch (this.cascade) {
                case 'up':
                case 'down':
                default:
                    var oldRow = itemPos.row;
                    itemPos.row = pos.row + dims.y;
                    if (!this._isWithinBoundsY(itemPos, itemDims)) {
                        itemPos.col = pos.col + dims.x;
                        itemPos.row = oldRow;
                    }
                    break;
                case 'left':
                case 'right':
                    var oldCol = itemPos.col;
                    itemPos.col = pos.col + dims.x;
                    if (!this._isWithinBoundsX(itemPos, itemDims)) {
                        itemPos.col = oldCol;
                        itemPos.row = pos.row + dims.y;
                    }
                    break;
            }
            if (shouldSave) {
                collisions[0].savePosition(itemPos);
            }
            else {
                collisions[0].setGridPosition(itemPos);
            }
            this._fixGridCollisions(itemPos, itemDims, shouldSave);
            this._addToGrid(collisions[0]);
            collisions[0].onCascadeEvent();
        }
    };
    NgGrid.prototype._limitGrid = function (maxCols) {
        var items = this._items.slice();
        items.sort(function (a, b) {
            var aPos = a.getSavedPosition();
            var bPos = b.getSavedPosition();
            if (aPos.row == bPos.row) {
                return aPos.col == bPos.col ? 0 : (aPos.col < bPos.col ? -1 : 1);
            }
            else {
                return aPos.row < bPos.row ? -1 : 1;
            }
        });
        var columnMax = {};
        var largestGap = {};
        for (var i = 1; i <= maxCols; i++) {
            columnMax[i] = 1;
            largestGap[i] = 1;
        }
        var curPos = { col: 1, row: 1 };
        var currentRow = 1;
        var willCascade = function (item, col) {
            for (var i = col; i < col + item.sizex; i++) {
                if (columnMax[i] == currentRow)
                    return true;
            }
            return false;
        };
        while (items.length > 0) {
            var columns = [];
            var newBlock = {
                start: 1,
                end: 1,
                length: 0,
            };
            for (var col = 1; col <= maxCols; col++) {
                if (columnMax[col] <= currentRow) {
                    if (newBlock.length == 0) {
                        newBlock.start = col;
                    }
                    newBlock.length++;
                    newBlock.end = col + 1;
                }
                else if (newBlock.length > 0) {
                    columns.push(newBlock);
                    newBlock = {
                        start: col,
                        end: col,
                        length: 0,
                    };
                }
            }
            if (newBlock.length > 0) {
                columns.push(newBlock);
            }
            var tempColumns = columns.map(function (block) { return block.length; });
            var currentItems = [];
            while (items.length > 0) {
                var item = items[0];
                if (item.row > currentRow)
                    break;
                var fits = false;
                for (var x in tempColumns) {
                    if (item.sizex <= tempColumns[x]) {
                        tempColumns[x] -= item.sizex;
                        fits = true;
                        break;
                    }
                    else if (item.sizex > tempColumns[x]) {
                        tempColumns[x] = 0;
                    }
                }
                if (fits) {
                    currentItems.push(items.shift());
                }
                else {
                    break;
                }
            }
            if (currentItems.length > 0) {
                var itemPositions = [];
                var lastPosition = maxCols;
                for (var i = currentItems.length - 1; i >= 0; i--) {
                    var maxPosition = 1;
                    for (var j = columns.length - 1; j >= 0; j--) {
                        if (columns[j].start > lastPosition)
                            continue;
                        if (columns[j].start > (maxCols - currentItems[i].sizex))
                            continue;
                        if (columns[j].length < currentItems[i].sizex)
                            continue;
                        if (lastPosition < columns[j].end && (lastPosition - columns[j].start) < currentItems[i].sizex)
                            continue;
                        maxPosition = (lastPosition < columns[j].end ? lastPosition : columns[j].end) - currentItems[i].sizex;
                        break;
                    }
                    itemPositions[i] = Math.min(maxPosition, currentItems[i].row == currentRow ? currentItems[i].col : 1);
                    lastPosition = itemPositions[i];
                }
                var minPosition = 1;
                var currentItem = 0;
                while (currentItems.length > 0) {
                    var item = currentItems.shift();
                    for (var j = 0; j < columns.length; j++) {
                        if (columns[j].length < item.sizex)
                            continue;
                        if (minPosition > columns[j].end)
                            continue;
                        if (minPosition > columns[j].start && (columns[j].end - minPosition) < item.sizex)
                            continue;
                        if (minPosition < columns[j].start)
                            minPosition = columns[j].start;
                        break;
                    }
                    item.setGridPosition({ col: Math.max(minPosition, itemPositions[currentItem]), row: currentRow });
                    minPosition = item.currentCol + item.sizex;
                    currentItem++;
                    for (var i = item.currentCol; i < item.currentCol + item.sizex; i++) {
                        columnMax[i] = item.currentRow + item.sizey;
                    }
                }
            }
            else if (currentItems.length === 0 && columns.length === 1 && columns[0].length >= maxCols) {
                var item = items.shift();
                item.setGridPosition({ col: 1, row: currentRow });
                for (var i = item.currentCol; i < item.currentCol + item.sizex; i++) {
                    columnMax[i] = item.currentRow + item.sizey;
                }
            }
            var newRow = 0;
            for (var x in columnMax) {
                if (columnMax[x] > currentRow && (newRow == 0 || columnMax[x] < newRow)) {
                    newRow = columnMax[x];
                }
            }
            currentRow = newRow <= currentRow ? currentRow + 1 : newRow;
        }
    };
    NgGrid.prototype._cascadeGrid = function (pos, dims, shouldSave) {
        if (shouldSave === void 0) { shouldSave = true; }
        if (this._destroyed)
            return;
        if (pos && !dims)
            throw new Error('Cannot cascade with only position and not dimensions');
        if (this.isDragging && this._draggingItem && !pos && !dims) {
            pos = this._draggingItem.getGridPosition();
            dims = this._draggingItem.getSize();
        }
        else if (this.isResizing && this._resizingItem && !pos && !dims) {
            pos = this._resizingItem.getGridPosition();
            dims = this._resizingItem.getSize();
        }
        switch (this.cascade) {
            case 'up':
            case 'down':
                var lowRow = [0];
                for (var i = 1; i <= this._curMaxCol; i++)
                    lowRow[i] = 1;
                for (var r = 1; r <= this._curMaxRow; r++) {
                    if (this._itemGrid[r] == undefined)
                        continue;
                    for (var c = 1; c <= this._curMaxCol; c++) {
                        if (this._itemGrid[r] == undefined)
                            break;
                        if (r < lowRow[c])
                            continue;
                        if (this._itemGrid[r][c] != null) {
                            var item = this._itemGrid[r][c];
                            if (item.isFixed)
                                continue;
                            var itemDims = item.getSize();
                            var itemPos = item.getGridPosition();
                            if (itemPos.col != c || itemPos.row != r)
                                continue; //	If this is not the element's start
                            var lowest = lowRow[c];
                            for (var i = 1; i < itemDims.x; i++) {
                                lowest = Math.max(lowRow[(c + i)], lowest);
                            }
                            if (pos && (c + itemDims.x) > pos.col && c < (pos.col + dims.x)) {
                                if ((r >= pos.row && r < (pos.row + dims.y)) ||
                                    ((itemDims.y > (pos.row - lowest)) &&
                                        (r >= (pos.row + dims.y) && lowest < (pos.row + dims.y)))) {
                                    lowest = Math.max(lowest, pos.row + dims.y); //	Set the lowest row to be below it
                                }
                            }
                            var newPos = { col: c, row: lowest };
                            if (lowest != itemPos.row && this._isWithinBoundsY(newPos, itemDims)) {
                                this._removeFromGrid(item);
                                if (shouldSave) {
                                    item.savePosition(newPos);
                                }
                                else {
                                    item.setGridPosition(newPos);
                                }
                                item.onCascadeEvent();
                                this._addToGrid(item);
                            }
                            for (var i = 0; i < itemDims.x; i++) {
                                lowRow[c + i] = lowest + itemDims.y; //	Update the lowest row to be below the item
                            }
                        }
                    }
                }
                break;
            case 'left':
            case 'right':
                var lowCol = [0];
                for (var i = 1; i <= this._curMaxRow; i++)
                    lowCol[i] = 1;
                for (var r = 1; r <= this._curMaxRow; r++) {
                    if (this._itemGrid[r] == undefined)
                        continue;
                    for (var c = 1; c <= this._curMaxCol; c++) {
                        if (this._itemGrid[r] == undefined)
                            break;
                        if (c < lowCol[r])
                            continue;
                        if (this._itemGrid[r][c] != null) {
                            var item = this._itemGrid[r][c];
                            var itemDims = item.getSize();
                            var itemPos = item.getGridPosition();
                            if (itemPos.col != c || itemPos.row != r)
                                continue; //	If this is not the element's start
                            var lowest = lowCol[r];
                            for (var i = 1; i < itemDims.y; i++) {
                                lowest = Math.max(lowCol[(r + i)], lowest);
                            }
                            if (pos && (r + itemDims.y) > pos.row && r < (pos.row + dims.y)) {
                                if ((c >= pos.col && c < (pos.col + dims.x)) ||
                                    ((itemDims.x > (pos.col - lowest)) &&
                                        (c >= (pos.col + dims.x) && lowest < (pos.col + dims.x)))) {
                                    lowest = Math.max(lowest, pos.col + dims.x); //	Set the lowest col to be below it
                                }
                            }
                            var newPos = { col: lowest, row: r };
                            if (lowest != itemPos.col && this._isWithinBoundsX(newPos, itemDims)) {
                                this._removeFromGrid(item);
                                if (shouldSave) {
                                    item.savePosition(newPos);
                                }
                                else {
                                    item.setGridPosition(newPos);
                                }
                                item.onCascadeEvent();
                                this._addToGrid(item);
                            }
                            for (var i = 0; i < itemDims.y; i++) {
                                lowCol[r + i] = lowest + itemDims.x; //	Update the lowest col to be below the item
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
    };
    NgGrid.prototype._fixGridPosition = function (pos, dims) {
        while (this._hasGridCollision(pos, dims) || !this._isWithinBounds(pos, dims)) {
            if (this._hasGridCollision(pos, dims)) {
                switch (this.cascade) {
                    case 'up':
                    case 'down':
                    default:
                        pos.row++;
                        break;
                    case 'left':
                    case 'right':
                        pos.col++;
                        break;
                }
            }
            if (!this._isWithinBoundsY(pos, dims)) {
                pos.col++;
                pos.row = 1;
            }
            if (!this._isWithinBoundsX(pos, dims)) {
                pos.row++;
                pos.col = 1;
            }
        }
        return pos;
    };
    NgGrid.prototype._isWithinBoundsX = function (pos, dims) {
        return (this._maxCols == 0 || (pos.col + dims.x - 1) <= this._maxCols);
    };
    NgGrid.prototype._isWithinBoundsY = function (pos, dims) {
        return (this._maxRows == 0 || (pos.row + dims.y - 1) <= this._maxRows);
    };
    NgGrid.prototype._isWithinBounds = function (pos, dims) {
        return this._isWithinBoundsX(pos, dims) && this._isWithinBoundsY(pos, dims);
    };
    NgGrid.prototype._addToGrid = function (item) {
        var pos = item.getGridPosition();
        var dims = item.getSize();
        if (this._hasGridCollision(pos, dims)) {
            this._fixGridCollisions(pos, dims);
            pos = item.getGridPosition();
        }
        for (var j = 0; j < dims.y; j++) {
            if (this._itemGrid[pos.row + j] == null)
                this._itemGrid[pos.row + j] = {};
            for (var i = 0; i < dims.x; i++) {
                this._itemGrid[pos.row + j][pos.col + i] = item;
                this._updateSize(pos.col + dims.x - 1, pos.row + dims.y - 1);
            }
        }
    };
    NgGrid.prototype._removeFromGrid = function (item) {
        for (var y in this._itemGrid)
            for (var x in this._itemGrid[y])
                if (this._itemGrid[y][x] == item)
                    delete this._itemGrid[y][x];
    };
    NgGrid.prototype._updateSize = function (col, row) {
        if (this._destroyed)
            return;
        col = (col == undefined) ? this._getMaxCol() : col;
        row = (row == undefined) ? this._getMaxRow() : row;
        var maxCol = Math.max(this._curMaxCol, col);
        var maxRow = Math.max(this._curMaxRow, row);
        if (maxCol != this._curMaxCol || maxRow != this._curMaxRow) {
            this._curMaxCol = maxCol;
            this._curMaxRow = maxRow;
        }
        this._renderer.setElementStyle(this._ngEl.nativeElement, 'width', '100%'); //(maxCol * (this.colWidth + this.marginLeft + this.marginRight))+'px');
        this._renderer.setElementStyle(this._ngEl.nativeElement, 'height', (this._getMaxRow() * (this.rowHeight + this.marginTop + this.marginBottom)) + 'px');
    };
    NgGrid.prototype._getMaxRow = function () {
        return Math.max.apply(null, this._items.map(function (item) { return item.getGridPosition().row + item.getSize().y - 1; }));
    };
    NgGrid.prototype._getMaxCol = function () {
        return Math.max.apply(null, this._items.map(function (item) { return item.getGridPosition().col + item.getSize().x - 1; }));
    };
    NgGrid.prototype._getMousePosition = function (e) {
        if ((window.TouchEvent && e instanceof TouchEvent) || (e.touches || e.changedTouches)) {
            e = e.touches.length > 0 ? e.touches[0] : e.changedTouches[0];
        }
        var refPos = this._ngEl.nativeElement.getBoundingClientRect();
        var left = e.clientX - refPos.left;
        var top = e.clientY - refPos.top;
        if (this.cascade == 'down')
            top = refPos.top + refPos.height - e.clientY;
        if (this.cascade == 'right')
            left = refPos.left + refPos.width - e.clientX;
        if (this.isDragging && this._zoomOnDrag) {
            left *= 2;
            top *= 2;
        }
        return {
            left: left,
            top: top
        };
    };
    NgGrid.prototype._getAbsoluteMousePosition = function (e) {
        if ((window.TouchEvent && e instanceof TouchEvent) || (e.touches || e.changedTouches)) {
            e = e.touches.length > 0 ? e.touches[0] : e.changedTouches[0];
        }
        return {
            left: e.clientX,
            top: e.clientY
        };
    };
    NgGrid.prototype._getContainerColumns = function () {
        var maxWidth = this._ngEl.nativeElement.getBoundingClientRect().width;
        return Math.floor(maxWidth / (this.colWidth + this.marginLeft + this.marginRight));
    };
    NgGrid.prototype._getItemFromPosition = function (position) {
        for (var _i = 0, _a = this._items; _i < _a.length; _i++) {
            var item = _a[_i];
            var size = item.getDimensions();
            var pos = item.getPosition();
            if (position.left > (pos.left + this.marginLeft) && position.left < (pos.left + this.marginLeft + size.width) &&
                position.top > (pos.top + this.marginTop) && position.top < (pos.top + this.marginTop + size.height)) {
                return item;
            }
        }
        return null;
    };
    NgGrid.prototype._createPlaceholder = function (item) {
        var pos = item.getGridPosition();
        var dims = item.getSize();
        var factory = this.componentFactoryResolver.resolveComponentFactory(NgGridPlaceholder_1.NgGridPlaceholder);
        var componentRef = item.containerRef.createComponent(factory);
        this._placeholderRef = componentRef;
        var placeholder = componentRef.instance;
        placeholder.registerGrid(this);
        placeholder.setCascadeMode(this.cascade);
        placeholder.setGridPosition({ col: pos.col, row: pos.row });
        placeholder.setSize({ x: dims.x, y: dims.y });
    };
    NgGrid.prototype._emitOnItemChange = function () {
        this.onItemChange.emit(this._items.map(function (item) { return item.getEventOutput(); }));
    };
    //	Default config
    NgGrid.CONST_DEFAULT_CONFIG = {
        margins: [10],
        draggable: true,
        resizable: true,
        max_cols: 0,
        max_rows: 0,
        visible_cols: 0,
        visible_rows: 0,
        col_width: 250,
        row_height: 250,
        cascade: 'up',
        min_width: 100,
        min_height: 100,
        fix_to_grid: false,
        auto_style: true,
        auto_resize: false,
        maintain_ratio: false,
        prefer_new: false,
        zoom_on_drag: false
    };
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_a = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _a) || Object)
    ], NgGrid.prototype, "onDragStart", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_b = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _b) || Object)
    ], NgGrid.prototype, "onDrag", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_c = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _c) || Object)
    ], NgGrid.prototype, "onDragStop", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_d = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _d) || Object)
    ], NgGrid.prototype, "onResizeStart", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_e = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _e) || Object)
    ], NgGrid.prototype, "onResize", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_f = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _f) || Object)
    ], NgGrid.prototype, "onResizeStop", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_g = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _g) || Object)
    ], NgGrid.prototype, "onItemChange", void 0);
    NgGrid = __decorate([
        core_1.Directive({
            selector: '[ngGrid]',
            inputs: ['config: ngGrid'],
            host: {
                '(mousedown)': 'mouseDownEventHandler($event)',
                '(mousemove)': 'mouseMoveEventHandler($event)',
                '(mouseup)': 'mouseUpEventHandler($event)',
                '(touchstart)': 'mouseDownEventHandler($event)',
                '(touchmove)': 'mouseMoveEventHandler($event)',
                '(touchend)': 'mouseUpEventHandler($event)',
                '(window:resize)': 'resizeEventHandler($event)',
                '(document:mousemove)': 'mouseMoveEventHandler($event)',
                '(document:mouseup)': 'mouseUpEventHandler($event)'
            },
        }), 
        __metadata('design:paramtypes', [(typeof (_h = typeof core_1.KeyValueDiffers !== 'undefined' && core_1.KeyValueDiffers) === 'function' && _h) || Object, (typeof (_j = typeof core_1.ElementRef !== 'undefined' && core_1.ElementRef) === 'function' && _j) || Object, (typeof (_k = typeof core_1.Renderer !== 'undefined' && core_1.Renderer) === 'function' && _k) || Object, (typeof (_l = typeof core_1.ComponentFactoryResolver !== 'undefined' && core_1.ComponentFactoryResolver) === 'function' && _l) || Object, (typeof (_m = typeof core_1.ViewContainerRef !== 'undefined' && core_1.ViewContainerRef) === 'function' && _m) || Object])
    ], NgGrid);
    return NgGrid;
    var _a, _b, _c, _d, _e, _f, _g, _h, _j, _k, _l, _m;
}());
exports.NgGrid = NgGrid;


/***/ },

/***/ "./src/app/controls/nggrid/directives/NgGridItem.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var NgGrid_1 = __webpack_require__("./src/app/controls/nggrid/directives/NgGrid.ts");
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var NgGridItem = (function () {
    //	Constructor
    function NgGridItem(_differs, _ngEl, _renderer, _ngGrid, containerRef) {
        this._differs = _differs;
        this._ngEl = _ngEl;
        this._renderer = _renderer;
        this._ngGrid = _ngGrid;
        this.containerRef = containerRef;
        //	Event Emitters
        this.onItemChange = new core_1.EventEmitter(false);
        this.onDragStart = new core_1.EventEmitter();
        this.onDrag = new core_1.EventEmitter();
        this.onDragStop = new core_1.EventEmitter();
        this.onDragAny = new core_1.EventEmitter();
        this.onResizeStart = new core_1.EventEmitter();
        this.onResize = new core_1.EventEmitter();
        this.onResizeStop = new core_1.EventEmitter();
        this.onResizeAny = new core_1.EventEmitter();
        this.onChangeStart = new core_1.EventEmitter();
        this.onChange = new core_1.EventEmitter();
        this.onChangeStop = new core_1.EventEmitter();
        this.onChangeAny = new core_1.EventEmitter();
        this.ngGridItemChange = new core_1.EventEmitter();
        this.isFixed = false;
        this.isDraggable = true;
        this.isResizable = true;
        this.minWidth = 0;
        this.minHeight = 0;
        this._position = { col: 1, row: 1 };
        this._currentPosition = { col: 1, row: 1 };
        this._size = { x: 1, y: 1 };
        this._config = NgGridItem.CONST_DEFAULT_CONFIG;
        this._added = false;
        this._maxCols = 0;
        this._minCols = 0;
        this._maxRows = 0;
        this._minRows = 0;
    }
    Object.defineProperty(NgGridItem.prototype, "config", {
        //	[ng-grid-item] handler
        set: function (v) {
            var defaults = NgGridItem.CONST_DEFAULT_CONFIG;
            for (var x in defaults)
                if (v[x] == null)
                    v[x] = defaults[x];
            this.setConfig(v);
            if (this._differ == null && v != null) {
                this._differ = this._differs.find(this._config).create(null);
            }
            if (!this._added) {
                this._added = true;
                this._ngGrid.addItem(this);
            }
            this._recalculateDimensions();
            this._recalculatePosition();
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(NgGridItem.prototype, "sizex", {
        get: function () {
            return this._size.x;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(NgGridItem.prototype, "sizey", {
        get: function () {
            return this._size.y;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(NgGridItem.prototype, "col", {
        get: function () {
            return this._position.col;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(NgGridItem.prototype, "row", {
        get: function () {
            return this._position.row;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(NgGridItem.prototype, "currentCol", {
        get: function () {
            return this._currentPosition.col;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(NgGridItem.prototype, "currentRow", {
        get: function () {
            return this._currentPosition.row;
        },
        enumerable: true,
        configurable: true
    });
    NgGridItem.prototype.onResizeStartEvent = function () {
        var event = this.getEventOutput();
        this.onResizeStart.emit(event);
        this.onResizeAny.emit(event);
        this.onChangeStart.emit(event);
        this.onChangeAny.emit(event);
    };
    NgGridItem.prototype.onResizeEvent = function () {
        var event = this.getEventOutput();
        this.onResize.emit(event);
        this.onResizeAny.emit(event);
        this.onChange.emit(event);
        this.onChangeAny.emit(event);
    };
    NgGridItem.prototype.onResizeStopEvent = function () {
        var event = this.getEventOutput();
        this.onResizeStop.emit(event);
        this.onResizeAny.emit(event);
        this.onChangeStop.emit(event);
        this.onChangeAny.emit(event);
        this._config.sizex = this._size.x;
        this._config.sizey = this._size.y;
        this.ngGridItemChange.emit(this._config);
    };
    NgGridItem.prototype.onDragStartEvent = function () {
        var event = this.getEventOutput();
        this.onDragStart.emit(event);
        this.onDragAny.emit(event);
        this.onChangeStart.emit(event);
        this.onChangeAny.emit(event);
    };
    NgGridItem.prototype.onDragEvent = function () {
        var event = this.getEventOutput();
        this.onDrag.emit(event);
        this.onDragAny.emit(event);
        this.onChange.emit(event);
        this.onChangeAny.emit(event);
    };
    NgGridItem.prototype.onDragStopEvent = function () {
        var event = this.getEventOutput();
        this.onDragStop.emit(event);
        this.onDragAny.emit(event);
        this.onChangeStop.emit(event);
        this.onChangeAny.emit(event);
        this._config.col = this._position.col;
        this._config.row = this._position.row;
        this.ngGridItemChange.emit(this._config);
    };
    NgGridItem.prototype.onCascadeEvent = function () {
        this._config.sizex = this._size.x;
        this._config.sizey = this._size.y;
        this._config.col = this._position.col;
        this._config.row = this._position.row;
        this.ngGridItemChange.emit(this._config);
    };
    NgGridItem.prototype.ngOnInit = function () {
        this._renderer.setElementClass(this._ngEl.nativeElement, 'grid-item', true);
        if (this._ngGrid.autoStyle)
            this._renderer.setElementStyle(this._ngEl.nativeElement, 'position', 'absolute');
        this._recalculateDimensions();
        this._recalculatePosition();
        if (!this._added) {
            this._added = true;
            this._ngGrid.addItem(this);
        }
    };
    //	Public methods
    NgGridItem.prototype.canDrag = function (e) {
        if (!this.isDraggable)
            return false;
        if (this._dragHandle) {
            return this.findHandle(this._dragHandle, e.target);
        }
        return true;
    };
    NgGridItem.prototype.findHandle = function (handleSelector, startElement) {
        var targetElem = startElement;
        while (targetElem && targetElem != this._ngEl.nativeElement) {
            if (this.elementMatches(targetElem, handleSelector))
                return true;
            targetElem = targetElem.parentElement;
        }
        return false;
    };
    NgGridItem.prototype.canResize = function (e) {
        if (!this.isResizable)
            return null;
        if (this._resizeHandle) {
            return this.findHandle(this._resizeHandle, e.target) ? 'both' : null;
        }
        var mousePos = this._getMousePosition(e);
        if (mousePos.left < this._elemWidth && mousePos.left > this._elemWidth - this._borderSize
            && mousePos.top < this._elemHeight && mousePos.top > this._elemHeight - this._borderSize) {
            return 'both';
        }
        else if (mousePos.left < this._elemWidth && mousePos.left > this._elemWidth - this._borderSize) {
            return 'width';
        }
        else if (mousePos.top < this._elemHeight && mousePos.top > this._elemHeight - this._borderSize) {
            return 'height';
        }
        return null;
    };
    NgGridItem.prototype.onMouseMove = function (e) {
        if (this._ngGrid.autoStyle) {
            if (this._ngGrid.resizeEnable && !this._resizeHandle && this.isResizable) {
                var mousePos = this._getMousePosition(e);
                if (mousePos.left < this._elemWidth && mousePos.left > this._elemWidth - this._borderSize
                    && mousePos.top < this._elemHeight && mousePos.top > this._elemHeight - this._borderSize) {
                    this._renderer.setElementStyle(this._ngEl.nativeElement, 'cursor', 'nwse-resize');
                }
                else if (mousePos.left < this._elemWidth && mousePos.left > this._elemWidth - this._borderSize) {
                    this._renderer.setElementStyle(this._ngEl.nativeElement, 'cursor', 'ew-resize');
                }
                else if (mousePos.top < this._elemHeight && mousePos.top > this._elemHeight - this._borderSize) {
                    this._renderer.setElementStyle(this._ngEl.nativeElement, 'cursor', 'ns-resize');
                }
                else if (this._ngGrid.dragEnable && this.canDrag(e)) {
                    this._renderer.setElementStyle(this._ngEl.nativeElement, 'cursor', 'move');
                }
                else {
                    this._renderer.setElementStyle(this._ngEl.nativeElement, 'cursor', 'default');
                }
            }
            else if (this._ngGrid.resizeEnable && this.canResize(e)) {
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'cursor', 'nwse-resize');
            }
            else if (this._ngGrid.dragEnable && this.canDrag(e)) {
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'cursor', 'move');
            }
            else {
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'cursor', 'default');
            }
        }
    };
    NgGridItem.prototype.ngOnDestroy = function () {
        if (this._added)
            this._ngGrid.removeItem(this);
    };
    //	Getters
    NgGridItem.prototype.getElement = function () {
        return this._ngEl;
    };
    NgGridItem.prototype.getDragHandle = function () {
        return this._dragHandle;
    };
    NgGridItem.prototype.getResizeHandle = function () {
        return this._resizeHandle;
    };
    NgGridItem.prototype.getDimensions = function () {
        return { 'width': this._elemWidth, 'height': this._elemHeight };
    };
    NgGridItem.prototype.getSize = function () {
        return this._size;
    };
    NgGridItem.prototype.getPosition = function () {
        return { 'left': this._elemLeft, 'top': this._elemTop };
    };
    NgGridItem.prototype.getGridPosition = function () {
        return this._currentPosition;
    };
    NgGridItem.prototype.getSavedPosition = function () {
        return this._position;
    };
    //	Setters
    NgGridItem.prototype.setConfig = function (config) {
        this._config = config;
        this._payload = config.payload;
        this._position.col = this._currentPosition.col = config.col ? config.col : NgGridItem.CONST_DEFAULT_CONFIG.col;
        this._position.row = this._currentPosition.row = config.row ? config.row : NgGridItem.CONST_DEFAULT_CONFIG.row;
        this._size.x = config.sizex ? config.sizex : NgGridItem.CONST_DEFAULT_CONFIG.sizex;
        this._size.y = config.sizey ? config.sizey : NgGridItem.CONST_DEFAULT_CONFIG.sizey;
        this._dragHandle = config.dragHandle;
        this._resizeHandle = config.resizeHandle;
        this._borderSize = config.borderSize;
        this.isDraggable = config.draggable ? true : false;
        this.isResizable = config.resizable ? true : false;
        this.isFixed = config.fixed ? true : false;
        this._maxCols = !isNaN(config.maxCols) && isFinite(config.maxCols) ? config.maxCols : 0;
        this._minCols = !isNaN(config.minCols) && isFinite(config.minCols) ? config.minCols : 0;
        this._maxRows = !isNaN(config.maxRows) && isFinite(config.maxRows) ? config.maxRows : 0;
        this._minRows = !isNaN(config.minRows) && isFinite(config.minRows) ? config.minRows : 0;
        this.minWidth = !isNaN(config.minWidth) && isFinite(config.minWidth) ? config.minWidth : 0;
        this.minHeight = !isNaN(config.minHeight) && isFinite(config.minHeight) ? config.minHeight : 0;
        if (this._minCols > 0 && this._maxCols > 0 && this._minCols > this._maxCols)
            this._minCols = 0;
        if (this._minRows > 0 && this._maxRows > 0 && this._minRows > this._maxRows)
            this._minRows = 0;
        if (this._added) {
            this._ngGrid.updateItem(this);
        }
        this._size = this.fixResize(this._size);
        this._recalculatePosition();
        this._recalculateDimensions();
    };
    NgGridItem.prototype.ngDoCheck = function () {
        if (this._differ != null) {
            var changes = this._differ.diff(this._config);
            if (changes != null) {
                this._applyChanges(changes);
                return true;
            }
        }
        return false;
    };
    NgGridItem.prototype.setSize = function (newSize, update) {
        if (update === void 0) { update = true; }
        newSize = this.fixResize(newSize);
        this._size = newSize;
        if (update)
            this._recalculateDimensions();
        this.onItemChange.emit(this.getEventOutput());
    };
    NgGridItem.prototype.setGridPosition = function (gridPosition, update) {
        if (update === void 0) { update = true; }
        this._currentPosition = gridPosition;
        if (update)
            this._recalculatePosition();
        this.onItemChange.emit(this.getEventOutput());
    };
    NgGridItem.prototype.savePosition = function (newPosition) {
        this._position = newPosition;
        this._currentPosition = newPosition;
        this._recalculatePosition();
        this.onItemChange.emit(this.getEventOutput());
    };
    NgGridItem.prototype.getEventOutput = function () {
        return {
            payload: this._payload,
            col: this._currentPosition.col,
            row: this._currentPosition.row,
            sizex: this._size.x,
            sizey: this._size.y,
            width: this._elemWidth,
            height: this._elemHeight,
            left: this._elemLeft,
            top: this._elemTop
        };
    };
    NgGridItem.prototype.setPosition = function (x, y) {
        switch (this._cascadeMode) {
            case 'up':
            case 'left':
            default:
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'transform', 'translate(' + x + 'px, ' + y + 'px)');
                break;
            case 'right':
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'transform', 'translate(' + -x + 'px, ' + y + 'px)');
                break;
            case 'down':
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'transform', 'translate(' + x + 'px, ' + -y + 'px)');
                break;
        }
        this._elemLeft = x;
        this._elemTop = y;
    };
    NgGridItem.prototype.setCascadeMode = function (cascade) {
        this._cascadeMode = cascade;
        switch (cascade) {
            case 'up':
            case 'left':
            default:
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'left', '0px');
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'top', '0px');
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'right', null);
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'bottom', null);
                break;
            case 'right':
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'right', '0px');
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'top', '0px');
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'left', null);
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'bottom', null);
                break;
            case 'down':
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'left', '0px');
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'bottom', '0px');
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'right', null);
                this._renderer.setElementStyle(this._ngEl.nativeElement, 'top', null);
                break;
        }
    };
    NgGridItem.prototype.setDimensions = function (w, h) {
        if (w < this.minWidth)
            w = this.minWidth;
        if (h < this.minHeight)
            h = this.minHeight;
        this._renderer.setElementStyle(this._ngEl.nativeElement, 'width', w + 'px');
        this._renderer.setElementStyle(this._ngEl.nativeElement, 'height', h + 'px');
        this._elemWidth = w;
        this._elemHeight = h;
    };
    NgGridItem.prototype.startMoving = function () {
        this._renderer.setElementClass(this._ngEl.nativeElement, 'moving', true);
        var style = window.getComputedStyle(this._ngEl.nativeElement);
        if (this._ngGrid.autoStyle)
            this._renderer.setElementStyle(this._ngEl.nativeElement, 'z-index', (parseInt(style.getPropertyValue('z-index')) + 1).toString());
    };
    NgGridItem.prototype.stopMoving = function () {
        this._renderer.setElementClass(this._ngEl.nativeElement, 'moving', false);
        var style = window.getComputedStyle(this._ngEl.nativeElement);
        if (this._ngGrid.autoStyle)
            this._renderer.setElementStyle(this._ngEl.nativeElement, 'z-index', (parseInt(style.getPropertyValue('z-index')) - 1).toString());
    };
    NgGridItem.prototype.recalculateSelf = function () {
        this._recalculatePosition();
        this._recalculateDimensions();
    };
    NgGridItem.prototype.fixResize = function (newSize) {
        if (this._maxCols > 0 && newSize.x > this._maxCols)
            newSize.x = this._maxCols;
        if (this._maxRows > 0 && newSize.y > this._maxRows)
            newSize.y = this._maxRows;
        if (this._minCols > 0 && newSize.x < this._minCols)
            newSize.x = this._minCols;
        if (this._minRows > 0 && newSize.y < this._minRows)
            newSize.y = this._minRows;
        var itemWidth = (newSize.x * this._ngGrid.colWidth) + ((this._ngGrid.marginLeft + this._ngGrid.marginRight) * (newSize.x - 1));
        if (itemWidth < this.minWidth)
            newSize.x = Math.ceil((this.minWidth + this._ngGrid.marginRight + this._ngGrid.marginLeft) / (this._ngGrid.colWidth + this._ngGrid.marginRight + this._ngGrid.marginLeft));
        var itemHeight = (newSize.y * this._ngGrid.rowHeight) + ((this._ngGrid.marginTop + this._ngGrid.marginBottom) * (newSize.y - 1));
        if (itemHeight < this.minHeight)
            newSize.y = Math.ceil((this.minHeight + this._ngGrid.marginBottom + this._ngGrid.marginTop) / (this._ngGrid.rowHeight + this._ngGrid.marginBottom + this._ngGrid.marginTop));
        return newSize;
    };
    //	Private methods
    NgGridItem.prototype.elementMatches = function (element, selector) {
        if (element.matches)
            return element.matches(selector);
        if (element.oMatchesSelector)
            return element.oMatchesSelector(selector);
        if (element.msMatchesSelector)
            return element.msMatchesSelector(selector);
        if (element.mozMatchesSelector)
            return element.mozMatchesSelector(selector);
        if (element.webkitMatchesSelector)
            return element.webkitMatchesSelector(selector);
        var matches = (element.document || element.ownerDocument).querySelectorAll(selector);
        var i = matches.length;
        while (--i >= 0 && matches.item(i) !== element) { }
        return i > -1;
    };
    NgGridItem.prototype._recalculatePosition = function () {
        var x = (this._ngGrid.colWidth + this._ngGrid.marginLeft + this._ngGrid.marginRight) * (this._currentPosition.col - 1) + this._ngGrid.marginLeft;
        var y = (this._ngGrid.rowHeight + this._ngGrid.marginTop + this._ngGrid.marginBottom) * (this._currentPosition.row - 1) + this._ngGrid.marginTop;
        this.setPosition(x, y);
    };
    NgGridItem.prototype._recalculateDimensions = function () {
        if (this._size.x < this._ngGrid.minCols)
            this._size.x = this._ngGrid.minCols;
        if (this._size.y < this._ngGrid.minRows)
            this._size.y = this._ngGrid.minRows;
        var newWidth = (this._ngGrid.colWidth * this._size.x) + ((this._ngGrid.marginLeft + this._ngGrid.marginRight) * (this._size.x - 1));
        var newHeight = (this._ngGrid.rowHeight * this._size.y) + ((this._ngGrid.marginTop + this._ngGrid.marginBottom) * (this._size.y - 1));
        var w = Math.max(this.minWidth, this._ngGrid.minWidth, newWidth);
        var h = Math.max(this.minHeight, this._ngGrid.minHeight, newHeight);
        this.setDimensions(w, h);
    };
    NgGridItem.prototype._getMousePosition = function (e) {
        if (e.originalEvent && e.originalEvent.touches) {
            var oe = e.originalEvent;
            e = oe.touches.length ? oe.touches[0] : (oe.changedTouches.length ? oe.changedTouches[0] : e);
        }
        else if (e.touches) {
            e = e.touches.length ? e.touches[0] : (e.changedTouches.length ? e.changedTouches[0] : e);
        }
        var refPos = this._ngEl.nativeElement.getBoundingClientRect();
        return {
            left: e.clientX - refPos.left,
            top: e.clientY - refPos.top
        };
    };
    NgGridItem.prototype._applyChanges = function (changes) {
        var _this = this;
        changes.forEachAddedItem(function (record) { _this._config[record.key] = record.currentValue; });
        changes.forEachChangedItem(function (record) { _this._config[record.key] = record.currentValue; });
        changes.forEachRemovedItem(function (record) { delete _this._config[record.key]; });
        this.setConfig(this._config);
    };
    //	Default config
    NgGridItem.CONST_DEFAULT_CONFIG = {
        col: 1,
        row: 1,
        sizex: 1,
        sizey: 1,
        dragHandle: null,
        resizeHandle: null,
        fixed: false,
        draggable: true,
        resizable: true,
        borderSize: 25
    };
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_a = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _a) || Object)
    ], NgGridItem.prototype, "onItemChange", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_b = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _b) || Object)
    ], NgGridItem.prototype, "onDragStart", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_c = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _c) || Object)
    ], NgGridItem.prototype, "onDrag", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_d = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _d) || Object)
    ], NgGridItem.prototype, "onDragStop", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_e = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _e) || Object)
    ], NgGridItem.prototype, "onDragAny", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_f = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _f) || Object)
    ], NgGridItem.prototype, "onResizeStart", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_g = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _g) || Object)
    ], NgGridItem.prototype, "onResize", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_h = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _h) || Object)
    ], NgGridItem.prototype, "onResizeStop", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_j = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _j) || Object)
    ], NgGridItem.prototype, "onResizeAny", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_k = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _k) || Object)
    ], NgGridItem.prototype, "onChangeStart", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_l = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _l) || Object)
    ], NgGridItem.prototype, "onChange", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_m = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _m) || Object)
    ], NgGridItem.prototype, "onChangeStop", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_o = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _o) || Object)
    ], NgGridItem.prototype, "onChangeAny", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_p = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _p) || Object)
    ], NgGridItem.prototype, "ngGridItemChange", void 0);
    NgGridItem = __decorate([
        core_1.Directive({
            selector: '[ngGridItem]',
            inputs: ['config: ngGridItem']
        }), 
        __metadata('design:paramtypes', [(typeof (_q = typeof core_1.KeyValueDiffers !== 'undefined' && core_1.KeyValueDiffers) === 'function' && _q) || Object, (typeof (_r = typeof core_1.ElementRef !== 'undefined' && core_1.ElementRef) === 'function' && _r) || Object, (typeof (_s = typeof core_1.Renderer !== 'undefined' && core_1.Renderer) === 'function' && _s) || Object, (typeof (_t = typeof NgGrid_1.NgGrid !== 'undefined' && NgGrid_1.NgGrid) === 'function' && _t) || Object, (typeof (_u = typeof core_1.ViewContainerRef !== 'undefined' && core_1.ViewContainerRef) === 'function' && _u) || Object])
    ], NgGridItem);
    return NgGridItem;
    var _a, _b, _c, _d, _e, _f, _g, _h, _j, _k, _l, _m, _o, _p, _q, _r, _s, _t, _u;
}());
exports.NgGridItem = NgGridItem;


/***/ },

/***/ "./src/app/controls/nggrid/interfaces/INgGrid.ts":
/***/ function(module, exports) {

"use strict";
"use strict";


/***/ },

/***/ "./src/app/controls/nggrid/main.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
function __export(m) {
    for (var p in m) if (!exports.hasOwnProperty(p)) exports[p] = m[p];
}
__export(__webpack_require__("./src/app/controls/nggrid/directives/NgGrid.ts"));
__export(__webpack_require__("./src/app/controls/nggrid/directives/NgGridItem.ts"));
__export(__webpack_require__("./src/app/controls/nggrid/components/NgGridPlaceholder.ts"));
__export(__webpack_require__("./src/app/controls/nggrid/interfaces/INgGrid.ts"));
__export(__webpack_require__("./src/app/controls/nggrid/modules/NgGrid.module.ts"));


/***/ },

/***/ "./src/app/controls/nggrid/modules/NgGrid.module.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var main_1 = __webpack_require__("./src/app/controls/nggrid/main.ts");
var NgGridModule = (function () {
    function NgGridModule() {
    }
    NgGridModule = __decorate([
        core_1.NgModule({
            declarations: [main_1.NgGrid, main_1.NgGridItem, main_1.NgGridPlaceholder],
            entryComponents: [main_1.NgGridPlaceholder],
            exports: [main_1.NgGrid, main_1.NgGridItem]
        }), 
        __metadata('design:paramtypes', [])
    ], NgGridModule);
    return NgGridModule;
}());
exports.NgGridModule = NgGridModule;


/***/ }

});
//# sourceMappingURL=2.map