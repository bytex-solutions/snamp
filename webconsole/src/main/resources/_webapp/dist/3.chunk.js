webpackJsonpac__name_([3],{

/***/ "./node_modules/css-loader/index.js!./src/app/watchers/templates/css/main.css":
/***/ function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("./node_modules/css-loader/lib/css-base.js")();
// imports


// module
exports.push([module.i, "", ""]);

// exports


/***/ },

/***/ "./src/app/watchers/templates/css/main.css":
/***/ function(module, exports, __webpack_require__) {


        var result = __webpack_require__("./node_modules/css-loader/index.js!./src/app/watchers/templates/css/main.css");

        if (typeof result === "string") {
            module.exports = result;
        } else {
            module.exports = result.toString();
        }
    

/***/ },

/***/ "./src/app/watchers/templates/main.html":
/***/ function(module, exports) {

module.exports = "<div class=\"right_col\" role=\"main\" style=\"min-height: 949px;\">\r\n  <div class=\"\">\r\n    <div class=\"page-title\">\r\n      <div class=\"title_left\">\r\n        <h3>Setup watchers</h3>\r\n      </div>\r\n    </div>\r\n\r\n    <div class=\"clearfix\"></div>\r\n\r\n    <div class=\"row\" style=\"margin-top: 30px\">\r\n\r\n      <panel [header]=\"'Main tab'\" [column]=\"'8'\">\r\n\r\n      </panel>\r\n\r\n      <panel [header]=\"'Some second tab'\" [column]=\"'4'\">\r\n\r\n      </panel>\r\n\r\n    </div>\r\n  </div>\r\n</div>\r\n\r\n"

/***/ },

/***/ "./src/app/watchers/templates/template.html":
/***/ function(module, exports) {

module.exports = "<router-outlet></router-outlet>"

/***/ },

/***/ "./src/app/watchers/watchers.modules.ts":
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
var watchers_template_1 = __webpack_require__("./src/app/watchers/watchers.template.ts");
var watchers_view_1 = __webpack_require__("./src/app/watchers/watchers.view.ts");
var PROVIDERS = [
    app_restClient_1.ApiClient,
    vex_1.providers
];
var WatchersModule = (function () {
    function WatchersModule() {
    }
    WatchersModule = __decorate([
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
                        path: '', component: watchers_template_1.TemplateComponent, children: [
                            { path: '', component: watchers_view_1.MainComponent }
                        ]
                    }])
            ],
            declarations: [watchers_template_1.TemplateComponent, watchers_view_1.MainComponent],
            providers: PROVIDERS
        }), 
        __metadata('design:paramtypes', [])
    ], WatchersModule);
    return WatchersModule;
}());
exports.WatchersModule = WatchersModule;


/***/ },

/***/ "./src/app/watchers/watchers.template.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var TemplateComponent = (function () {
    function TemplateComponent() {
    }
    TemplateComponent = __decorate([
        core_1.Component({
            moduleId: module.i,
            template: __webpack_require__("./src/app/watchers/templates/template.html"),
        }), 
        __metadata('design:paramtypes', [])
    ], TemplateComponent);
    return TemplateComponent;
}());
exports.TemplateComponent = TemplateComponent;


/***/ },

/***/ "./src/app/watchers/watchers.view.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var app_restClient_1 = __webpack_require__("./src/app/app.restClient.ts");
var router_1 = __webpack_require__("./node_modules/@angular/router/index.js");
var MainComponent = (function () {
    function MainComponent(apiClient, _router) {
        this._router = _router;
        this.http = apiClient;
    }
    MainComponent.prototype.ngOnInit = function () {
        this.http.get(app_restClient_1.REST.WATCHERS_LIST)
            .map(function (res) { return res.json(); })
            .subscribe(function (data) {
            console.log("Watchers list is: ", data);
        });
    };
    MainComponent = __decorate([
        core_1.Component({
            moduleId: module.i,
            template: __webpack_require__("./src/app/watchers/templates/main.html"),
            styles: [__webpack_require__("./src/app/watchers/templates/css/main.css")]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof app_restClient_1.ApiClient !== 'undefined' && app_restClient_1.ApiClient) === 'function' && _a) || Object, (typeof (_b = typeof router_1.Router !== 'undefined' && router_1.Router) === 'function' && _b) || Object])
    ], MainComponent);
    return MainComponent;
    var _a, _b;
}());
exports.MainComponent = MainComponent;


/***/ }

});
//# sourceMappingURL=3.map