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
exports.push([module.i, " #cy {\r\n    width: 100%;\r\n    height: 100%;\r\n    position: absolute;\r\n    top: 0px;\r\n    left: 0px;\r\n}", ""]);

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
            declarations: [analysis_template_1.TemplateView, analysis_add_view_1.AddView, analysis_view_1.MainView],
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
"use strict";
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
        this.http = apiClient;
    }
    MainView.prototype.ngOnInit = function () { };
    MainView.prototype.ngAfterViewInit = function () {
        var _this = this;
        this.currentViewObs = this.route.params
            .map(function (params) {
            console.log("Retrieving some view from id: ", params['id']);
            return _this._viewService.getViewByName(params['id']);
        });
        this.currentViewObs.publishLast().refCount();
        this.currentViewObs.subscribe(function (_view) {
            console.log("trying to receive some data for view: ", _view);
            _this._viewService.getDataForView(_view).subscribe(function (_data) {
                _view.draw(_data);
                var _thisReference = _this;
                setInterval(function () {
                    _thisReference._viewService.getDataForView(_view).subscribe(function (updateData) {
                        _view.updateData(updateData);
                    });
                }, 3000);
            });
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

module.exports = "<div class=\"right_col\" role=\"main\" style=\"min-height: 949px;\">\r\n  <div class=\"\">\r\n    <div class=\"page-title\">\r\n      <div class=\"title_left\">\r\n        <h3>End-to-End {{(currentViewObs | async)?.name}} {{'(' + ((currentViewObs | async)?.type) + ')'}}</h3>\r\n      </div>\r\n    </div>\r\n\r\n    <div class=\"clearfix\"></div>\r\n\r\n    <div class=\"row\" style=\"margin-top: 30px\">\r\n\r\n      <div class='container col-md-12' style=\"min-height: 900px;\">\r\n        <div id=\"cy\"></div>\r\n      </div>\r\n\r\n    </div>\r\n  </div>\r\n</div>\r\n\r\n"

/***/ }

});
//# sourceMappingURL=2.map