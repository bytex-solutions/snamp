webpackJsonpac__name_([2],{

/***/ "./node_modules/css-loader/index.js!./src/app/analysis/templates/css/addView.css":
/***/ function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("./node_modules/css-loader/lib/css-base.js")();
// imports


// module
exports.push([module.i, "", ""]);

// exports


/***/ },

/***/ "./node_modules/css-loader/index.js!./src/app/analysis/templates/css/view.css":
/***/ function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("./node_modules/css-loader/lib/css-base.js")();
// imports


// module
exports.push([module.i, "", ""]);

// exports


/***/ },

/***/ "./src/app/analysis/analysis.add.view.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function($) {"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var app_restClient_1 = __webpack_require__("./src/app/app.restClient.ts");
var angular2_modal_1 = __webpack_require__("./node_modules/angular2-modal/esm/index.js");
var vex_1 = __webpack_require__("./node_modules/angular2-modal/plugins/vex/index.js");
__webpack_require__("./node_modules/rxjs/add/operator/publishLast.js");
__webpack_require__("./node_modules/rxjs/add/operator/cache.js");
__webpack_require__("./node_modules/rxjs/add/observable/forkJoin.js");
__webpack_require__("./node_modules/rxjs/add/observable/from.js");
__webpack_require__("./node_modules/rxjs/add/observable/of.js");
__webpack_require__("./node_modules/smartwizard/js/jquery.smartWizard.min.js");
__webpack_require__("./node_modules/select2/dist/js/select2.js");
var AddView = (function () {
    function AddView(apiClient, overlay, vcRef, modal) {
        this.modal = modal;
        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;
    }
    AddView.prototype.ngOnInit = function () {
    };
    AddView.prototype.ngAfterViewInit = function () {
        var _thisReference = this;
        $(document).ready(function () {
        });
    };
    AddView = __decorate([
        core_1.Component({
            moduleId: module.i,
            template: __webpack_require__("./src/app/analysis/templates/addView.html"),
            styles: [__webpack_require__("./src/app/analysis/templates/css/addView.css")]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof app_restClient_1.ApiClient !== 'undefined' && app_restClient_1.ApiClient) === 'function' && _a) || Object, (typeof (_b = typeof angular2_modal_1.Overlay !== 'undefined' && angular2_modal_1.Overlay) === 'function' && _b) || Object, (typeof (_c = typeof core_1.ViewContainerRef !== 'undefined' && core_1.ViewContainerRef) === 'function' && _c) || Object, (typeof (_d = typeof vex_1.Modal !== 'undefined' && vex_1.Modal) === 'function' && _d) || Object])
    ], AddView);
    return AddView;
    var _a, _b, _c, _d;
}());
exports.AddView = AddView;

/* WEBPACK VAR INJECTION */}.call(exports, __webpack_require__("./node_modules/jquery/dist/jquery.js")))

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
/* WEBPACK VAR INJECTION */(function($) {"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var app_restClient_1 = __webpack_require__("./src/app/app.restClient.ts");
var angular2_modal_1 = __webpack_require__("./node_modules/angular2-modal/esm/index.js");
var vex_1 = __webpack_require__("./node_modules/angular2-modal/plugins/vex/index.js");
var router_1 = __webpack_require__("./node_modules/@angular/router/index.js");
__webpack_require__("./node_modules/rxjs/add/operator/publishLast.js");
__webpack_require__("./node_modules/rxjs/add/operator/cache.js");
__webpack_require__("./node_modules/rxjs/add/observable/forkJoin.js");
__webpack_require__("./node_modules/rxjs/add/observable/from.js");
__webpack_require__("./node_modules/rxjs/add/observable/of.js");
__webpack_require__("./node_modules/smartwizard/js/jquery.smartWizard.min.js");
__webpack_require__("./node_modules/select2/dist/js/select2.js");
var MainView = (function () {
    function MainView(apiClient, overlay, vcRef, modal, route) {
        this.modal = modal;
        this.route = route;
        this.mainView = "";
        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;
    }
    MainView.prototype.ngOnInit = function () {
        var _this = this;
        this.route.params
            .map(function (params) { return params['id']; })
            .subscribe(function (id) {
            _this.mainView = id;
        });
    };
    MainView.prototype.ngAfterViewInit = function () {
        var _thisReference = this;
        $(document).ready(function () {
        });
    };
    MainView = __decorate([
        core_1.Component({
            moduleId: module.i,
            template: __webpack_require__("./src/app/analysis/templates/view.html"),
            styles: [__webpack_require__("./src/app/analysis/templates/css/view.css")]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof app_restClient_1.ApiClient !== 'undefined' && app_restClient_1.ApiClient) === 'function' && _a) || Object, (typeof (_b = typeof angular2_modal_1.Overlay !== 'undefined' && angular2_modal_1.Overlay) === 'function' && _b) || Object, (typeof (_c = typeof core_1.ViewContainerRef !== 'undefined' && core_1.ViewContainerRef) === 'function' && _c) || Object, (typeof (_d = typeof vex_1.Modal !== 'undefined' && vex_1.Modal) === 'function' && _d) || Object, (typeof (_e = typeof router_1.ActivatedRoute !== 'undefined' && router_1.ActivatedRoute) === 'function' && _e) || Object])
    ], MainView);
    return MainView;
    var _a, _b, _c, _d, _e;
}());
exports.MainView = MainView;

/* WEBPACK VAR INJECTION */}.call(exports, __webpack_require__("./node_modules/jquery/dist/jquery.js")))

/***/ },

/***/ "./src/app/analysis/templates/addView.html":
/***/ function(module, exports) {

module.exports = "<div class=\"right_col\" role=\"main\" style=\"min-height: 949px;\">\r\n  <div class=\"\">\r\n    <div class=\"page-title\">\r\n      <div class=\"title_left\">\r\n        <h3>Add E2E view</h3>\r\n      </div>\r\n    </div>\r\n\r\n    <div class=\"clearfix\"></div>\r\n\r\n    <div class=\"row\" style=\"margin-top: 30px\">\r\n\r\n      <div class='container col-md-12' style=\"min-height: 900px;\">\r\n\r\n      </div>\r\n\r\n    </div>\r\n  </div>\r\n</div>\r\n\r\n"

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

module.exports = "<div class=\"right_col\" role=\"main\" style=\"min-height: 949px;\">\r\n  <div class=\"\">\r\n    <div class=\"page-title\">\r\n      <div class=\"title_left\">\r\n        <h3>View for {{mainView}}</h3>\r\n      </div>\r\n    </div>\r\n\r\n    <div class=\"clearfix\"></div>\r\n\r\n    <div class=\"row\" style=\"margin-top: 30px\">\r\n\r\n      <div class='container col-md-12' style=\"min-height: 900px;\">\r\n\r\n      </div>\r\n\r\n    </div>\r\n  </div>\r\n</div>\r\n\r\n"

/***/ }

});
//# sourceMappingURL=2.map