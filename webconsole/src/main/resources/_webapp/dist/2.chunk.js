webpackJsonpac__name_([2],{

/***/ "./node_modules/css-loader/index.js!./src/app/analysis/templates/css/addView.css":
/***/ function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("./node_modules/css-loader/lib/css-base.js")();
// imports


// module
exports.push([module.i, ".form-group {\r\n    margin-bottom: 30px;\r\n}\r\n\r\n.form {\r\n    margin-top: 20px;\r\n}", ""]);

// exports


/***/ },

/***/ "./node_modules/css-loader/index.js!./src/app/analysis/templates/css/view.css":
/***/ function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("./node_modules/css-loader/lib/css-base.js")();
// imports


// module
exports.push([module.i, " #cy {\r\n    width: 100%;\r\n    height: 100%;\r\n    position: absolute;\r\n    top: 0px;\r\n    left: 0px;\r\n}\r\n\r\n#viewMenu {\r\n  background: #fff;\r\n  border: 1px solid #E6E9ED;\r\n  overflow-y: scroll;\r\n  overflow-x: hidden;\r\n  max-height: 500px;\r\n  color: rgb(115, 135, 156);\r\n  width: 450px;\r\n  position: fixed;\r\n  right: 7px;\r\n  top: 35%;\r\n  margin-top: -10em !important;\r\n  z-index: 3;\r\n  padding: 9px;\r\n  border-radius: 5px;\r\n}\r\n\r\n#viewMenu .row {\r\n    margin-left: 10px !important;\r\n}\r\n\r\n#viewMenu ul.bar_tabs {\r\n background-color: inherit !important;\r\n}", ""]);

// exports


/***/ },

/***/ "./src/app/analysis/analysis.add.view.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var app_restClient_1 = __webpack_require__("./src/app/app.restClient.ts");
var router_1 = __webpack_require__("./node_modules/@angular/router/index.js");
var objectFactory_1 = __webpack_require__("./src/app/analysis/model/objectFactory.ts");
var abstract_e2e_view_1 = __webpack_require__("./src/app/analysis/model/abstract.e2e.view.ts");
var app_viewService_1 = __webpack_require__("./src/app/app.viewService.ts");
__webpack_require__("./node_modules/rxjs/add/operator/publishLast.js");
__webpack_require__("./node_modules/rxjs/add/operator/cache.js");
__webpack_require__("./node_modules/rxjs/add/observable/forkJoin.js");
__webpack_require__("./node_modules/rxjs/add/observable/from.js");
__webpack_require__("./node_modules/rxjs/add/observable/of.js");
var AddView = (function () {
    function AddView(apiClient, _viewService, _router) {
        this._viewService = _viewService;
        this._router = _router;
        this.types = ViewType.createViewTypes();
        this.chosenComponent = undefined;
        this.viewType = undefined;
        this.viewName = undefined;
        this.http = apiClient;
    }
    AddView.prototype.ngOnInit = function () {
        this.components = this.http.get(app_restClient_1.REST.CHART_COMPONENTS)
            .map(function (res) { return res.json(); })
            .publishLast().refCount();
    };
    AddView.prototype.saveView = function () {
        var _view = objectFactory_1.Factory.createView(this.viewName, this.viewType.id, this.chosenComponent);
        this._viewService.newView(_view);
        this._router.navigateByUrl('/view/' + _view.name);
    };
    AddView = __decorate([
        core_1.Component({
            moduleId: module.i,
            template: __webpack_require__("./src/app/analysis/templates/addView.html"),
            styles: [__webpack_require__("./src/app/analysis/templates/css/addView.css")]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof app_restClient_1.ApiClient !== 'undefined' && app_restClient_1.ApiClient) === 'function' && _a) || Object, (typeof (_b = typeof app_viewService_1.ViewService !== 'undefined' && app_viewService_1.ViewService) === 'function' && _b) || Object, (typeof (_c = typeof router_1.Router !== 'undefined' && router_1.Router) === 'function' && _c) || Object])
    ], AddView);
    return AddView;
    var _a, _b, _c;
}());
exports.AddView = AddView;
var ViewType = (function () {
    function ViewType(name, id, description) {
        this.name = "";
        this.id = "";
        this.description = "";
        this.name = name;
        this.id = id;
        this.description = description;
    }
    ViewType.createViewTypes = function () {
        var result = [];
        result.push(new ViewType("Landscape view", abstract_e2e_view_1.E2EView.LANDSCAPE, "Represents E2E view of all components in IT landscape"));
        result.push(new ViewType("Child components view", abstract_e2e_view_1.E2EView.CHILD_COMPONENT, "Represents E2E view of child components"));
        result.push(new ViewType("Component modules view", abstract_e2e_view_1.E2EView.COMPONENT_MODULES, "Represents communications scheme between the modules within the component"));
        return result;
    };
    return ViewType;
}());


/***/ },

/***/ "./src/app/analysis/analysis.modules.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var common_1 = __webpack_require__("./node_modules/@angular/common/index.js");
var app_restClient_1 = __webpack_require__("./src/app/app.restClient.ts");
var ng2_tooltip_1 = __webpack_require__("./node_modules/ng2-tooltip/index.js");
var forms_1 = __webpack_require__("./node_modules/@angular/forms/index.js");
var http_1 = __webpack_require__("./node_modules/@angular/http/index.js");
var router_1 = __webpack_require__("./node_modules/@angular/router/index.js");
var angular2_modal_1 = __webpack_require__("./node_modules/angular2-modal/esm/index.js");
var vex_1 = __webpack_require__("./node_modules/angular2-modal/plugins/vex/index.js");
var app_module_1 = __webpack_require__("./src/app/app.module.ts");
var analysis_template_1 = __webpack_require__("./src/app/analysis/analysis.template.ts");
var analysis_add_view_1 = __webpack_require__("./src/app/analysis/analysis.add.view.ts");
var analysis_view_1 = __webpack_require__("./src/app/analysis/analysis.view.ts");
var time_interval_component_1 = __webpack_require__("./src/app/analysis/components/time.interval.component.ts");
var checkbox_group_component_1 = __webpack_require__("./src/app/analysis/components/checkbox.group.component.ts");
var PROVIDERS = [
    app_restClient_1.ApiClient
];
var AnalysisModule = (function () {
    function AnalysisModule() {
    }
    AnalysisModule = __decorate([
        core_1.NgModule({
            imports: [
                common_1.CommonModule,
                ng2_tooltip_1.TooltipModule,
                forms_1.FormsModule,
                angular2_modal_1.ModalModule.forRoot(),
                vex_1.VexModalModule,
                http_1.HttpModule,
                app_module_1.CommonSnampUtilsModule,
                router_1.RouterModule.forChild([{
                        path: '', component: analysis_template_1.TemplateView, children: [
                            { path: '', component: analysis_add_view_1.AddView },
                            { path: ':id', component: analysis_view_1.MainView }
                        ]
                    }])
            ],
            declarations: [analysis_template_1.TemplateView, analysis_add_view_1.AddView, analysis_view_1.MainView, time_interval_component_1.TimeIntervalsView, checkbox_group_component_1.CheckboxGroupView],
            providers: PROVIDERS
        }), 
        __metadata('design:paramtypes', [])
    ], AnalysisModule);
    return AnalysisModule;
}());
exports.AnalysisModule = AnalysisModule;


/***/ },

/***/ "./src/app/analysis/analysis.template.ts":
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
            template: __webpack_require__("./src/app/analysis/templates/template.view.html"),
        }), 
        __metadata('design:paramtypes', [])
    ], TemplateView);
    return TemplateView;
}());
exports.TemplateView = TemplateView;


/***/ },

/***/ "./src/app/analysis/analysis.view.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function($) {"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var app_restClient_1 = __webpack_require__("./src/app/app.restClient.ts");
var router_1 = __webpack_require__("./node_modules/@angular/router/index.js");
var app_viewService_1 = __webpack_require__("./src/app/app.viewService.ts");
__webpack_require__("./node_modules/rxjs/add/operator/publishLast.js");
__webpack_require__("./node_modules/rxjs/add/operator/cache.js");
__webpack_require__("./node_modules/rxjs/add/observable/forkJoin.js");
__webpack_require__("./node_modules/rxjs/add/observable/from.js");
__webpack_require__("./node_modules/rxjs/add/observable/of.js");
var MainView = (function () {
    function MainView(apiClient, route, _viewService) {
        this.route = route;
        this._viewService = _viewService;
        this.currentViewObs = undefined;
        this.currentView = undefined;
        this.metadata = undefined;
        this.currentNodeId = undefined;
        this._cyObject = undefined;
        this.nodeSelected = false;
        this.http = apiClient;
    }
    MainView.prototype.ngOnInit = function () { };
    MainView.prototype.ngAfterViewInit = function () {
        var _this = this;
        $("#menu_toggle").trigger('click');
        this.currentViewObs = this.route.params
            .map(function (params) { return _this._viewService.getViewByName(params['id']); });
        this.currentViewObs.publishLast().refCount();
        this.currentViewObs.subscribe(function (_view) {
            _this.currentView = _view;
            _this._viewService.getDataForView(_view).subscribe(function (_data) {
                _this._cyObject = _view.draw(_data);
                _this.handleCy(_this._cyObject);
                var _thisReference = _this;
                setInterval(function () {
                    _thisReference._viewService.getDataForView(_view).subscribe(function (updateData) {
                        _view.updateData(updateData);
                        if (_thisReference.currentNodeId != undefined) {
                            _thisReference.metadata = _thisReference._cyObject.$('#' + _thisReference.currentNodeId).data('arrival');
                        }
                    });
                }, 1000);
            });
        });
    };
    MainView.prototype.saveCheckboxStatus = function () {
        var _cb = $("#myTabContent2 input[type=checkbox]:checked");
        var _array = $.map(_cb, function (element) { return $(element).attr("name"); });
        if (this.currentView != undefined) {
            this.currentView.setDisplayedMetadata(_array);
            this._viewService.saveDashboard();
        }
        console.log(_array);
    };
    MainView.prototype.handleCy = function (_cy) {
        var _thisReference = this;
        _cy.on('tap', function (event) {
            // cyTarget holds a reference to the originator
            // of the event (core or element)
            var evtTarget = event.cyTarget;
            _thisReference.nodeSelected = (evtTarget != _cy);
            if (evtTarget === _cy) {
                console.log('tap on background');
                _thisReference.metadata = evtTarget;
                _thisReference.currentNodeId = undefined;
            }
            else {
                console.log('tap on some element', evtTarget.data('arrival'));
                _thisReference.currentNodeId = evtTarget.data('id');
                _thisReference.metadata = _cy.$('#' + evtTarget.data('id')).data('arrival');
            }
        });
    };
    MainView = __decorate([
        core_1.Component({
            moduleId: module.i,
            template: __webpack_require__("./src/app/analysis/templates/view.html"),
            styles: [__webpack_require__("./src/app/analysis/templates/css/view.css")]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof app_restClient_1.ApiClient !== 'undefined' && app_restClient_1.ApiClient) === 'function' && _a) || Object, (typeof (_b = typeof router_1.ActivatedRoute !== 'undefined' && router_1.ActivatedRoute) === 'function' && _b) || Object, (typeof (_c = typeof app_viewService_1.ViewService !== 'undefined' && app_viewService_1.ViewService) === 'function' && _c) || Object])
    ], MainView);
    return MainView;
    var _a, _b, _c;
}());
exports.MainView = MainView;

/* WEBPACK VAR INJECTION */}.call(exports, __webpack_require__("./node_modules/jquery/dist/jquery.js")))

/***/ },

/***/ "./src/app/analysis/components/checkbox.group.component.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var CheckboxGroupView = (function () {
    function CheckboxGroupView() {
        this.formName = "";
        this.id = "";
        this.title = "";
    }
    __decorate([
        core_1.Input(), 
        __metadata('design:type', String)
    ], CheckboxGroupView.prototype, "formName", void 0);
    __decorate([
        core_1.Input(), 
        __metadata('design:type', String)
    ], CheckboxGroupView.prototype, "id", void 0);
    __decorate([
        core_1.Input(), 
        __metadata('design:type', String)
    ], CheckboxGroupView.prototype, "title", void 0);
    CheckboxGroupView = __decorate([
        core_1.Component({
            moduleId: module.i,
            selector: 'checkboxGroup',
            template: __webpack_require__("./src/app/analysis/components/templates/checkbox.group.html"),
            styles: ['.flatbar { width: 100% !important; text-align: left !important; margin-left: -15px !important; }']
        }), 
        __metadata('design:paramtypes', [])
    ], CheckboxGroupView);
    return CheckboxGroupView;
}());
exports.CheckboxGroupView = CheckboxGroupView;


/***/ },

/***/ "./src/app/analysis/components/templates/checkbox.group.html":
/***/ function(module, exports) {

module.exports = "<div>\r\n    <nav class=\"navbar\">\r\n        <button class=\"navbar-toggler flatbar\" type=\"button\" data-toggle=\"collapse\" [attr.data-target]=\"'#' + id\" [attr.aria-controls]=\"id\" aria-expanded=\"false\" aria-label=\"Toggle navigation\">\r\n            <span class=\"fa fa-chevron-down\"></span> {{title}}\r\n        </button>\r\n    </nav>\r\n    <div class=\"collapse\" [attr.id]=\"id\">\r\n        <div class=\"p-4\">\r\n            <div class=\"row\" style=\"margin-left: 10px;\">\r\n\r\n                <div class=\"form-check\">\r\n                    <label class=\"form-check-label\">\r\n                        <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"formName + '/' + 'Second'\">\r\n                        1 second\r\n                    </label>\r\n                </div>\r\n\r\n                <div class=\"form-check\">\r\n                    <label class=\"form-check-label\">\r\n                        <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"formName + '/' + 'Minute'\">\r\n                        1 minute\r\n                    </label>\r\n                </div>\r\n\r\n                <div class=\"form-check\">\r\n                    <label class=\"form-check-label\">\r\n                        <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"formName + '/' + '5Minutes'\">\r\n                        5 minutes\r\n                    </label>\r\n                </div>\r\n\r\n                <div class=\"form-check\">\r\n                    <label class=\"form-check-label\">\r\n                        <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"formName + '/' + '15Minutes'\">\r\n                        15 minutes\r\n                    </label>\r\n                </div>\r\n\r\n                <div class=\"form-check\">\r\n                    <label class=\"form-check-label\">\r\n                        <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"formName + '/' + 'Hour'\">\r\n                        1 hour\r\n                    </label>\r\n                </div>\r\n\r\n                <div class=\"form-check\">\r\n                    <label class=\"form-check-label\">\r\n                        <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"formName + '/' + '12Hours'\">\r\n                        12 hours\r\n                    </label>\r\n                </div>\r\n\r\n                <div class=\"form-check\">\r\n                    <label class=\"form-check-label\">\r\n                        <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"formName + '/' + 'Day'\">\r\n                        1 day\r\n                    </label>\r\n                </div>\r\n            </div>\r\n        </div>\r\n    </div>\r\n</div>"

/***/ },

/***/ "./src/app/analysis/components/templates/time.intervals.html":
/***/ function(module, exports) {

module.exports = "<div>\r\n    <nav class=\"navbar\">\r\n        <button class=\"navbar-toggler flatbar\" type=\"button\" data-toggle=\"collapse\" [attr.data-target]=\"'#' + id\" [attr.aria-controls]=\"id\" aria-expanded=\"false\" aria-label=\"Toggle navigation\">\r\n            <span class=\"fa fa-chevron-down\"></span> {{title}}\r\n        </button>\r\n    </nav>\r\n    <div class=\"collapse\" [attr.id]=\"id\">\r\n        <div class=\"p-4\">\r\n            <dl class=\"row\" style=\"margin-left: 10px;\">\r\n                <dt class=\"col-sm-4\">1 second: </dt>\r\n                <dd class=\"col-sm-8\">{{jsonObject['Second']}}</dd>\r\n\r\n                <dt class=\"col-sm-4\">1 minute: </dt>\r\n                <dd class=\"col-sm-8\">{{jsonObject['Minute']}}</dd>\r\n\r\n                <dt class=\"col-sm-4\">5 minutes: </dt>\r\n                <dd class=\"col-sm-8\">{{jsonObject['5Minutes']}}</dd>\r\n\r\n                <dt class=\"col-sm-4\">15 minutes: </dt>\r\n                <dd class=\"col-sm-8\">{{jsonObject['15Minutes']}}</dd>\r\n\r\n                <dt class=\"col-sm-4\">1 hour: </dt>\r\n                <dd class=\"col-sm-8\">{{jsonObject['Hour']}}</dd>\r\n\r\n                <dt class=\"col-sm-4\">12 hours: </dt>\r\n                <dd class=\"col-sm-8\">{{jsonObject['12Hours']}}</dd>\r\n\r\n                <dt class=\"col-sm-4\">1 day: </dt>\r\n                <dd class=\"col-sm-8\">{{jsonObject['Day']}}</dd>\r\n            </dl>\r\n        </div>\r\n    </div>\r\n</div>"

/***/ },

/***/ "./src/app/analysis/components/time.interval.component.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var TimeIntervalsView = (function () {
    function TimeIntervalsView() {
        this.jsonObject = {};
        this.id = "";
        this.title = "";
    }
    __decorate([
        core_1.Input(), 
        __metadata('design:type', Object)
    ], TimeIntervalsView.prototype, "jsonObject", void 0);
    __decorate([
        core_1.Input(), 
        __metadata('design:type', String)
    ], TimeIntervalsView.prototype, "id", void 0);
    __decorate([
        core_1.Input(), 
        __metadata('design:type', String)
    ], TimeIntervalsView.prototype, "title", void 0);
    TimeIntervalsView = __decorate([
        core_1.Component({
            moduleId: module.i,
            selector: 'timeInterval',
            template: __webpack_require__("./src/app/analysis/components/templates/time.intervals.html"),
            styles: ['.flatbar { width: 100% !important; text-align: left !important; margin-left: -15px !important; }']
        }), 
        __metadata('design:paramtypes', [])
    ], TimeIntervalsView);
    return TimeIntervalsView;
}());
exports.TimeIntervalsView = TimeIntervalsView;


/***/ },

/***/ "./src/app/analysis/templates/addView.html":
/***/ function(module, exports) {

module.exports = "<div class=\"right_col\" role=\"main\" style=\"min-height: 949px;\">\r\n  <div class=\"\">\r\n    <div class=\"page-title\">\r\n      <div class=\"title_left\">\r\n        <h3>Add E2E view</h3>\r\n      </div>\r\n    </div>\r\n\r\n    <div class=\"clearfix\"></div>\r\n\r\n    <div class=\"row\" style=\"margin-top: 30px\">\r\n\r\n      <panel [header]=\"'Gateway type'\" [column]=\"'8'\">\r\n        <div class=\"form leftAlign\">\r\n\r\n          <div class=\"form-group row\">\r\n            <label class=\"col-sm-2 col-form-label\" for=\"viewName\">\r\n              1. View name <span class=\"required\">*</span>\r\n            </label>\r\n            <div class=\"col-sm-10\">\r\n              <input type=\"text\" id=\"viewName\" [(ngModel)]=\"viewName\" required=\"required\" class=\"form-control\" placeholder=\"Input view name\"/>\r\n            </div>\r\n          </div>\r\n\r\n          <div class=\"form-group row\">\r\n            <label class=\"col-sm-2 col-form-label\" for=\"viewType\">\r\n              2. Type of view <span class=\"required\">*</span>\r\n            </label>\r\n            <div class=\"col-sm-10\">\r\n              <select class=\"form-control\" [(ngModel)]=\"viewType\" id=\"viewType\">\r\n                <option *ngFor=\"let type of types\" [ngValue]=\"type\">{{type.name}}</option>\r\n              </select>\r\n            </div>\r\n          </div>\r\n\r\n          <div class=\"form-group row\" *ngIf=\"viewType && viewType.id != 'landscape'\">\r\n            <label class=\"col-sm-2 col-form-label\" for=\"viewType\">\r\n              3. Component to analysis <span class=\"required\">*</span>\r\n            </label>\r\n            <div class=\"col-sm-10\">\r\n              <select class=\"form-control\" [(ngModel)]=\"chosenComponent\" id=\"component\">\r\n                <option *ngFor=\"let component of components | async\" [ngValue]=\"component\">{{component}}</option>\r\n              </select>\r\n            </div>\r\n          </div>\r\n\r\n          <div class=\"ln_solid\"></div>\r\n\r\n          <div class=\"row\">\r\n            <div class=\"col-sm-1 col-sm-offset-11\">\r\n              <div class=\"btn btn-primary\" (click)=\"saveView()\">Save</div>\r\n            </div>\r\n          </div>\r\n\r\n        </div>\r\n      </panel>\r\n\r\n      <panel [header]=\"'Description'\" [column]=\"'4'\">\r\n        <div class=\"row\">\r\n          <div class=\"col-sm-12 pagination-centered\" *ngIf=\"!viewType\">\r\n              Please select a type to get a description\r\n          </div>\r\n          <div class=\"col-sm-12\" *ngIf=\"viewType\">\r\n            {{viewType.description}}\r\n          </div>\r\n        </div>\r\n      </panel>\r\n\r\n    </div>\r\n  </div>\r\n</div>\r\n\r\n"

/***/ },

/***/ "./src/app/analysis/templates/css/addView.css":
/***/ function(module, exports, __webpack_require__) {


        var result = __webpack_require__("./node_modules/css-loader/index.js!./src/app/analysis/templates/css/addView.css");

        if (typeof result === "string") {
            module.exports = result;
        } else {
            module.exports = result.toString();
        }
    

/***/ },

/***/ "./src/app/analysis/templates/css/view.css":
/***/ function(module, exports, __webpack_require__) {


        var result = __webpack_require__("./node_modules/css-loader/index.js!./src/app/analysis/templates/css/view.css");

        if (typeof result === "string") {
            module.exports = result;
        } else {
            module.exports = result.toString();
        }
    

/***/ },

/***/ "./src/app/analysis/templates/template.view.html":
/***/ function(module, exports) {

module.exports = "<router-outlet></router-outlet>"

/***/ },

/***/ "./src/app/analysis/templates/view.html":
/***/ function(module, exports) {

module.exports = "<div id=\"viewMenu\">\r\n  <div class=\"\" role=\"tabpanel\" data-example-id=\"togglable-tabs\">\r\n    <ul id=\"myTab1\" class=\"nav nav-tabs bar_tabs\" role=\"tablist\">\r\n      <li role=\"presentation\" class=\"active\"><a href=\"#tab_info\" id=\"home-tabb\" role=\"tab\" data-toggle=\"tab\" aria-controls=\"info\" aria-expanded=\"true\">Information</a>\r\n      </li>\r\n      <li role=\"presentation\" class=\"\"><a href=\"#tab_settings\" role=\"tab\" id=\"profile-tabb\" data-toggle=\"tab\" aria-controls=\"settings\" aria-expanded=\"false\">Settings</a>\r\n      </li>\r\n    </ul>\r\n    <div id=\"myTabContent2\" class=\"tab-content\">\r\n      <div role=\"tabpanel\" class=\"tab-pane fade active in\" id=\"tab_info\" aria-labelledby=\"info-tab\">\r\n        <div *ngIf=\"metadata && nodeSelected\">\r\n          <timeInterval [id]=\"'availabilityToggle'\" [jsonObject]=\"metadata.availability\" [title]=\"'Availability'\"></timeInterval>\r\n          <timeInterval [id]=\"'maxRatePerSecondToggle'\" [jsonObject]=\"metadata.maxRatePerSecond\" [title]=\"'Max rate per second'\"></timeInterval>\r\n          <timeInterval [id]=\"'maxResponseTimeToggle'\" [jsonObject]=\"metadata.maxResponseTime\" [title]=\"'Max response time'\"></timeInterval>\r\n          <timeInterval [id]=\"'meanRateToggle'\" [jsonObject]=\"metadata.meanRate\" [title]=\"'Mean rate'\"></timeInterval>\r\n          <timeInterval [id]=\"'meanResponseTimeToggle'\" [jsonObject]=\"metadata.meanResponseTime\" [title]=\"'Mean response time'\"></timeInterval>\r\n\r\n          <div class=\"row\">\r\n            <dt class=\"col-sm-4\">Channels: </dt>\r\n            <dd class=\"col-sm-8\">{{metadata.channels}}</dd>\r\n          </div>\r\n\r\n          <div class=\"row\">\r\n            <dt class=\"col-sm-4\">Efficiency: </dt>\r\n            <dd class=\"col-sm-8\">{{metadata.efficiency}}</dd>\r\n          </div>\r\n\r\n          <div class=\"row\">\r\n            <dt class=\"col-sm-4\">Scalability: </dt>\r\n            <dd class=\"col-sm-8\">{{metadata.scalability}}</dd>\r\n          </div>\r\n\r\n          <div class=\"row\">\r\n            <dt class=\"col-sm-4\">Response time (90): </dt>\r\n            <dd class=\"col-sm-8\">{{metadata.responseTime90}}</dd>\r\n          </div>\r\n\r\n          <div class=\"row\">\r\n            <dt class=\"col-sm-4\">Response time (95): </dt>\r\n            <dd class=\"col-sm-8\">{{metadata.responseTime95}}</dd>\r\n          </div>\r\n\r\n          <div class=\"row\">\r\n            <dt class=\"col-sm-4\">Response time (98): </dt>\r\n            <dd class=\"col-sm-8\">{{metadata.responseTime98}}</dd>\r\n          </div>\r\n\r\n          <div class=\"row\">\r\n            <dt class=\"col-sm-4\">Response time standard deviation: </dt>\r\n            <dd class=\"col-sm-8\">{{metadata.responseTimeStdDev}}</dd>\r\n          </div>\r\n        </div>\r\n        <div *ngIf=\"metadata && !nodeSelected\">{{metadata}}</div>\r\n        <div *ngIf=\"!metadata\">No data is available</div>\r\n      </div>\r\n      <div role=\"tabpanel\" class=\"tab-pane fade\" id=\"tab_settings\" aria-labelledby=\"settings-tab\">\r\n        <p>Select needful information to display on graph: </p>\r\n          <checkboxGroup [id]=\"'availabilityCheckboxToggle'\" [title]=\"'Availability'\" [formName]=\"availability\"></checkboxGroup>\r\n          <checkboxGroup [id]=\"'maxRatePerSecondCheckboxToggle'\" [title]=\"'Max rate per second'\" [formName]=\"maxRatePerSecond\"></checkboxGroup>\r\n          <checkboxGroup [id]=\"'maxResponseTimeCheckboxToggle'\" [title]=\"'Max response time'\" [formName]=\"maxResponseTime\"></checkboxGroup>\r\n          <checkboxGroup [id]=\"'meanRateCheckboxToggle'\" [title]=\"'Mean rate'\" [formName]=\"meanRate\"></checkboxGroup>\r\n          <checkboxGroup [id]=\"'meanResponseTimeCheckboxToggle'\" [title]=\"'Mean response time'\" [formName]=\"meanResponseTime\"></checkboxGroup>\r\n\r\n          <div class=\"row\">\r\n            <div class=\"form-check\">\r\n              <label class=\"form-check-label\">\r\n                <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"'channels'\">\r\n                Channels\r\n              </label>\r\n            </div>\r\n          </div>\r\n\r\n        <div class=\"row\">\r\n          <div class=\"form-check\">\r\n            <label class=\"form-check-label\">\r\n              <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"'efficiency'\">\r\n              Efficiency\r\n            </label>\r\n          </div>\r\n        </div>\r\n\r\n\r\n        <div class=\"row\">\r\n          <div class=\"form-check\">\r\n            <label class=\"form-check-label\">\r\n              <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"'scalability'\">\r\n              Scalability\r\n            </label>\r\n          </div>\r\n        </div>\r\n\r\n\r\n        <div class=\"row\">\r\n          <div class=\"form-check\">\r\n            <label class=\"form-check-label\">\r\n              <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"'responseTime90'\">\r\n              Response time (90)\r\n            </label>\r\n          </div>\r\n        </div>\r\n\r\n\r\n        <div class=\"row\">\r\n          <div class=\"form-check\">\r\n            <label class=\"form-check-label\">\r\n              <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"'responseTime95'\">\r\n              Response time (95)\r\n            </label>\r\n          </div>\r\n        </div>\r\n\r\n\r\n        <div class=\"row\">\r\n          <div class=\"form-check\">\r\n            <label class=\"form-check-label\">\r\n              <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"'responseTime98'\">\r\n              Response time (98)\r\n            </label>\r\n          </div>\r\n        </div>\r\n\r\n\r\n        <div class=\"row\">\r\n          <div class=\"form-check\">\r\n            <label class=\"form-check-label\">\r\n              <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"'responseTimeStdDev'\">\r\n              Response time standard deviation\r\n            </label>\r\n          </div>\r\n        </div>\r\n\r\n        <div class=\"row\">\r\n          <button class=\"btn btn-primary\" (click)=\"saveCheckboxStatus()\">Apply</button>\r\n        </div>\r\n\r\n      </div>\r\n    </div>\r\n  </div>\r\n</div>\r\n\r\n<div class=\"right_col\" role=\"main\" style=\"min-height: 949px;\">\r\n  <div class=\"\">\r\n    <div class=\"page-title\">\r\n      <div class=\"title_left\">\r\n        <h3>End-to-End {{(currentViewObs | async)?.name}} {{'(' + ((currentViewObs | async)?.type) + ')'}}</h3>\r\n      </div>\r\n    </div>\r\n    <div class=\"clearfix\"></div>\r\n    <div class=\"row\">\r\n      <div class='container col-md-12' style=\"min-height: 900px;\">\r\n        <div id=\"cy\"></div>\r\n      </div>\r\n    </div>\r\n  </div>\r\n</div>\r\n\r\n"

/***/ }

});
//# sourceMappingURL=2.map