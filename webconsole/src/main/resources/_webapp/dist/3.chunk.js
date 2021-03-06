webpackJsonpac__name_([3],{

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
exports.push([module.i, ".cyObject {\r\n    width: 100%;\r\n    height: 100%;\r\n    position: absolute;\r\n    top: 0px;\r\n    left: 0px;\r\n}\r\n\r\n#viewMenu {\r\n  background: #fff;\r\n  border: 1px solid #E6E9ED;\r\n  overflow-y: scroll;\r\n  overflow-x: hidden;\r\n  height: 500px;\r\n  max-height: 700px;\r\n  color: rgb(115, 135, 156);\r\n  width: 500px;\r\n  position: absolute;\r\n  right: 7px;\r\n  margin-top: -10em !important;\r\n  z-index: 3;\r\n  padding: 9px;\r\n  border-radius: 5px;\r\n  opacity: 0.9;\r\n}\r\n\r\n#viewMenu .row {\r\n    margin-left: 10px !important;\r\n}\r\n\r\n#viewMenu ul.bar_tabs {\r\n background-color: inherit !important;\r\n}\r\n\r\n#viewMenu .navbar {\r\n    min-height: 0 !important;\r\n}\r\n\r\n.tab-pane {\r\n    margin-left: 20px !important;\r\n}\r\n\r\n.middleVertical {\r\n    margin-top: 7px;\r\n}\r\n\r\n.hided {\r\n    display: none;\r\n}", ""]);

// exports


/***/ },

/***/ "./src/app/analysis/analysis.add.view.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var app_restClient_1 = __webpack_require__("./src/app/services/app.restClient.ts");
var router_1 = __webpack_require__("./node_modules/@angular/router/index.js");
var objectFactory_1 = __webpack_require__("./src/app/analysis/model/objectFactory.ts");
var abstract_e2e_view_1 = __webpack_require__("./src/app/analysis/model/abstract.e2e.view.ts");
var app_viewService_1 = __webpack_require__("./src/app/services/app.viewService.ts");
__webpack_require__("./node_modules/rxjs/add/operator/publishLast.js");
__webpack_require__("./node_modules/rxjs/add/operator/cache.js");
__webpack_require__("./node_modules/rxjs/add/observable/forkJoin.js");
__webpack_require__("./node_modules/rxjs/add/observable/from.js");
__webpack_require__("./node_modules/rxjs/add/observable/of.js");
var AddView = (function () {
    function AddView(apiClient, _viewService, _router) {
        this._viewService = _viewService;
        this._router = _router;
        this.types = Description.createViewTypes();
        this.chosenComponent = undefined;
        this.viewType = undefined;
        this.viewName = undefined;
        this.shelfLife = 1;
        this.useShelfLife = false;
        this.periods = Description.createPeriodsTypes();
        this.http = apiClient;
    }
    AddView.prototype.ngOnInit = function () {
        this.components = this.http.get(app_restClient_1.REST.GROUPS_WEB_API)
            .map(function (res) { return res.json(); })
            .publishLast().refCount();
    };
    AddView.prototype.saveView = function () {
        var _view = objectFactory_1.Factory.createView(this.viewName, this.viewType.id, this.chosenComponent, this.useShelfLife ? this.shelfLife : undefined);
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
var Description = (function () {
    function Description(name, id, description, period) {
        this.name = "";
        this.id = "";
        this.description = "";
        this.period = 0; //in seconds
        this.name = name;
        this.id = id;
        this.description = description;
        this.period = period;
    }
    Description.createPeriodsTypes = function () {
        var result = [];
        result.push(new Description("", "", "1 second", 1));
        result.push(new Description("", "", "1 minute", 60));
        result.push(new Description("", "", "5 minutes", 300));
        result.push(new Description("", "", "15 minutes", 900));
        result.push(new Description("", "", "1 hour", 3600));
        result.push(new Description("", "", "12 hours", 43200));
        result.push(new Description("", "", "24 hours", 86400));
        return result;
    };
    Description.createViewTypes = function () {
        var result = [];
        result.push(new Description("Landscape view", abstract_e2e_view_1.E2EView.LANDSCAPE, "Represents E2E view of all components in IT landscape"));
        result.push(new Description("Child components view", abstract_e2e_view_1.E2EView.CHILD_COMPONENT, "Represents E2E view of child components"));
        result.push(new Description("Component modules view", abstract_e2e_view_1.E2EView.COMPONENT_MODULES, "Represents communications scheme between the modules within the component"));
        return result;
    };
    return Description;
}());
exports.Description = Description;


/***/ },

/***/ "./src/app/analysis/analysis.modules.ts":
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
var app_module_1 = __webpack_require__("./src/app/app.module.ts");
var analysis_template_1 = __webpack_require__("./src/app/analysis/analysis.template.ts");
var analysis_add_view_1 = __webpack_require__("./src/app/analysis/analysis.add.view.ts");
var analysis_view_1 = __webpack_require__("./src/app/analysis/analysis.view.ts");
var time_interval_component_1 = __webpack_require__("./src/app/analysis/components/time.interval.component.ts");
var checkbox_group_component_1 = __webpack_require__("./src/app/analysis/components/checkbox.group.component.ts");
var PROVIDERS = [
    app_restClient_1.ApiClient,
    vex_1.providers
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
var router_1 = __webpack_require__("./node_modules/@angular/router/index.js");
var app_viewService_1 = __webpack_require__("./src/app/services/app.viewService.ts");
__webpack_require__("./node_modules/rxjs/add/operator/publishLast.js");
__webpack_require__("./node_modules/rxjs/add/operator/cache.js");
__webpack_require__("./node_modules/rxjs/add/observable/forkJoin.js");
__webpack_require__("./node_modules/rxjs/add/observable/from.js");
__webpack_require__("./node_modules/rxjs/add/observable/of.js");
var angular2_modal_1 = __webpack_require__("./node_modules/angular2-modal/esm/index.js");
var vex_1 = __webpack_require__("./node_modules/angular2-modal/plugins/vex/index.js");
var analysis_add_view_1 = __webpack_require__("./src/app/analysis/analysis.add.view.ts");
var app_user_profile_1 = __webpack_require__("./src/app/services/app.user.profile.ts");
var MainView = (function () {
    function MainView(route, overlay, router, _viewService, modal, vcRef, ups) {
        this.route = route;
        this.router = router;
        this._viewService = _viewService;
        this.modal = modal;
        this.ups = ups;
        this.currentViewObs = undefined;
        this.currentView = undefined;
        this.metadata = undefined;
        this.currentNodeId = undefined;
        this._cyObject = undefined;
        this.nodeSelected = false;
        this.selectedLayout = "";
        this.textSize = "";
        this.textColor = "";
        this.backgroundColor = "";
        this.textOutlineColor = "";
        this.textOutlineWidth = 0;
        this.textWeight = 0;
        this.edgeWidth = 0;
        this.edgeLineColor = "";
        this.edgeArrowColor = "";
        this.edgeArrowShape = "";
        this.showSettings = false;
        this.shelfLife = 1;
        this.useShelfLife = false;
        this.oldValueShelfLife = 1;
        this.shelfLifeChanged = false;
        this.periods = analysis_add_view_1.Description.createPeriodsTypes();
        this.timerId = undefined;
        overlay.defaultViewContainer = vcRef;
    }
    MainView.prototype.ngAfterViewInit = function () {
        var _this = this;
        this.currentViewObs = this.route.params
            .map(function (params) {
            return _this._viewService.getViewByName(params['id']);
        });
        this.currentViewObs.publishLast().refCount();
        this.currentViewObs.subscribe(function (_view) {
            _this.currentView = _view;
            _this.selectedLayout = _view.getLayout();
            _this.textSize = _view.getTextSize();
            _this.textColor = _view.getTextColor();
            _this.backgroundColor = _view.getBackgroundColor();
            _this.textOutlineColor = _view.getTextOutlineColor();
            _this.textOutlineWidth = _view.getTextOutlineWidth();
            _this.textWeight = _view.getTextWeight();
            _this.edgeWidth = _view.getEdgeWidth();
            _this.edgeLineColor = _view.getEdgeLineColor();
            _this.edgeArrowColor = _view.getEdgeArrowColor();
            _this.edgeArrowShape = _view.getEdgeArrowShape();
            _this.shelfLife = _view.shelfLife;
            _this.useShelfLife = _view.isShelfLifeSet;
            _this.oldValueShelfLife = _view.shelfLife;
            console.debug(_this.selectedLayout, _this.textSize, _this.textColor, _this.backgroundColor, _this.textOutlineColor, _this.textOutlineWidth, _this.textWeight, _this.edgeWidth, _this.edgeLineColor, _this.edgeArrowColor, _this.edgeArrowShape);
            // set checkboxes according to preferences
            var _chbx = _view.getDisplayedMetadata();
            for (var ij = 0; ij < _chbx.length; ij++) {
                $("#myTabContent2 input[type='checkbox'][name='" + _chbx[ij] + "']").prop('checked', true);
            }
            _this._viewService.getDataForView(_view).subscribe(function (_data) {
                _this._cyObject = _view.draw(_data);
                _this.handleCy(_this._cyObject);
                var _thisReference = _this;
                _this.timerId = setInterval(function () {
                    if (!document.hidden) {
                        _thisReference._viewService.getDataForView(_view).subscribe(function (updateData) {
                            _view.updateData(updateData);
                            if (_thisReference.currentNodeId != undefined) {
                                _thisReference.metadata = _thisReference._cyObject.$('#' + _thisReference.currentNodeId).data('arrival');
                            }
                        });
                    }
                }, 3000);
            });
        });
    };
    MainView.prototype.saveCheckboxStatus = function () {
        var _cb = $("#myTabContent2 input[type=checkbox]:checked");
        var _array = $.map(_cb, function (element) {
            return $(element).attr("name");
        });
        if (this.currentView != undefined) {
            this.currentView.setDisplayedMetadata(_array);
            this._viewService.saveDashboard();
        }
    };
    MainView.prototype.onChangeLayout = function (event) {
        this.currentView.changeLayout(event);
        this._viewService.saveDashboard();
    };
    MainView.prototype.onChangeTextSize = function (event) {
        this.currentView.changeTextSize(event);
        this._viewService.saveDashboard();
    };
    MainView.prototype.onChangeTextColor = function (event) {
        this.currentView.changeTextColor(event);
        this._viewService.saveDashboard();
    };
    MainView.prototype.onChangeBackgroundColor = function (event) {
        this.currentView.changeBackgroundColor(event);
        this._viewService.saveDashboard();
    };
    MainView.prototype.onChangeTextOutlineColor = function (event) {
        this.currentView.changeTextOutlineColor(event);
        this._viewService.saveDashboard();
    };
    MainView.prototype.onChangeTextOutlineWidth = function (event) {
        this.currentView.changeTextOutlineWidth(event);
        this._viewService.saveDashboard();
    };
    MainView.prototype.onChangeTextWeight = function (event) {
        this.currentView.changeTextWeight(event);
        this._viewService.saveDashboard();
    };
    MainView.prototype.onChangeEdgeWidth = function (event) {
        this.currentView.changeEdgeWidth(event);
        this._viewService.saveDashboard();
    };
    MainView.prototype.onChangeEdgeLineColor = function (event) {
        this.currentView.changeEdgeLineColor(event);
        this._viewService.saveDashboard();
    };
    MainView.prototype.onChangeEdgeArrowColor = function (event) {
        this.currentView.changeEdgeArrowColor(event);
        this._viewService.saveDashboard();
    };
    MainView.prototype.onChangeEdgeArrowShape = function (event) {
        this.currentView.changeEdgeArrowShape(event);
        this._viewService.saveDashboard();
    };
    MainView.prototype.onChangeShelfLife = function (event) {
        this.currentView.shelfLife = event;
        this.shelfLifeChanged = false;
        this._viewService.saveDashboard();
    };
    MainView.prototype.saveShelfLife = function () {
        this.onChangeShelfLife(this.shelfLife);
    };
    MainView.prototype.triggerShelfLifeChanged = function (value) {
        this.shelfLifeChanged = (this.oldValueShelfLife != value);
    };
    MainView.prototype.triggerUseShelfLife = function (event) {
        this.currentView.isShelfLifeSet = event;
        console.debug("Current value from event: ", event, this.currentView.isShelfLifeSet);
        this._viewService.saveDashboard();
    };
    MainView.prototype.resetView = function () {
        var _this = this;
        this.modal.confirm()
            .className('default')
            .message('View will be reset to all users. Proceed?')
            .open()
            .then(function (resultPromise) {
            return resultPromise.result
                .then(function (response) {
                _this._viewService.resetView(_this.currentView).subscribe(function (data) {
                    console.debug("view has been reset: ", data);
                });
                return response;
            })
                .catch(function () {
                console.debug("user preferred to decline view reset");
            });
        }).catch(function () { });
    };
    MainView.prototype.handleCy = function (_cy) {
        var _thisReference = this;
        _cy.on('tap', function (event) {
            // cyTarget holds a reference to the originator
            // of the event (core or element)
            var evtTarget = event.cyTarget;
            _thisReference.nodeSelected = (evtTarget != _cy);
            if (evtTarget === _cy) {
                _thisReference.metadata = evtTarget;
                _thisReference.currentNodeId = undefined;
            }
            else {
                _thisReference.currentNodeId = evtTarget.data('id');
                _thisReference.metadata = _cy.$('#' + evtTarget.data('id')).data('arrival');
            }
        });
    };
    MainView.prototype.ngOnDestroy = function () {
        clearInterval(this.timerId);
    };
    MainView.prototype.removeView = function () {
        var _this = this;
        this.modal.confirm()
            .className('default')
            .message('View will be removed. Proceed?')
            .open()
            .then(function (resultPromise) {
            return resultPromise.result
                .then(function (response) {
                _this._viewService.removeView(_this.currentView.name);
                var _arr = _this._viewService.getViews();
                if (_arr.length > 0) {
                    _this.router.navigate(['view', _arr[0].name]);
                }
                else {
                    _this.router.navigate(['view']);
                }
                return response;
            })
                .catch(function () {
                console.debug("user preferred to decline view removing");
            });
        }).catch(function () { });
    };
    MainView.prototype.toggleShowSettings = function () {
        this.showSettings = !this.showSettings;
        var _btn = $("#btnSettings");
        var _menuBlock = $("#viewMenu");
        _menuBlock.css("top", (_btn.offset().top + _btn.height() + 150) + "px");
        _menuBlock.height(($("footer").offset().top - _menuBlock.offset().top - 150) + "px");
    };
    MainView.prototype.isAllowed = function () {
        return this.ups.isUserHasManagerOrAdminRole();
    };
    MainView = __decorate([
        core_1.Component({
            moduleId: module.i,
            template: __webpack_require__("./src/app/analysis/templates/view.html"),
            styles: [__webpack_require__("./src/app/analysis/templates/css/view.css")],
            encapsulation: core_1.ViewEncapsulation.None,
            entryComponents: [
                vex_1.DialogFormModal
            ]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof router_1.ActivatedRoute !== 'undefined' && router_1.ActivatedRoute) === 'function' && _a) || Object, (typeof (_b = typeof angular2_modal_1.Overlay !== 'undefined' && angular2_modal_1.Overlay) === 'function' && _b) || Object, (typeof (_c = typeof router_1.Router !== 'undefined' && router_1.Router) === 'function' && _c) || Object, (typeof (_d = typeof app_viewService_1.ViewService !== 'undefined' && app_viewService_1.ViewService) === 'function' && _d) || Object, (typeof (_e = typeof vex_1.Modal !== 'undefined' && vex_1.Modal) === 'function' && _e) || Object, (typeof (_f = typeof core_1.ViewContainerRef !== 'undefined' && core_1.ViewContainerRef) === 'function' && _f) || Object, (typeof (_g = typeof app_user_profile_1.UserProfileService !== 'undefined' && app_user_profile_1.UserProfileService) === 'function' && _g) || Object])
    ], MainView);
    return MainView;
    var _a, _b, _c, _d, _e, _f, _g;
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

module.exports = "<div class=\"right_col\" role=\"main\" style=\"min-height: 949px;\">\r\n  <div class=\"\">\r\n    <div class=\"page-title\">\r\n      <div class=\"title_left\">\r\n        <h3>Add E2E view</h3>\r\n      </div>\r\n    </div>\r\n\r\n    <div class=\"clearfix\"></div>\r\n\r\n    <div class=\"row\" style=\"margin-top: 30px\">\r\n\r\n      <panel [header]=\"'Gateway type'\" [column]=\"'8'\">\r\n        <div class=\"form leftAlign\">\r\n\r\n          <div class=\"form-group row\">\r\n            <label class=\"col-sm-2 col-form-label\" for=\"viewName\">\r\n              View name <span class=\"required\">*</span>\r\n            </label>\r\n            <div class=\"col-sm-10\">\r\n              <input type=\"text\" id=\"viewName\" [(ngModel)]=\"viewName\" required=\"required\" class=\"form-control\" placeholder=\"Input view name\"/>\r\n            </div>\r\n          </div>\r\n\r\n          <div class=\"form-group row\">\r\n            <label class=\"col-sm-2 col-form-label\" for=\"viewType\">\r\n              Type of view <span class=\"required\">*</span>\r\n            </label>\r\n            <div class=\"col-sm-10\">\r\n              <select class=\"form-control\" [(ngModel)]=\"viewType\" id=\"viewType\">\r\n                <option *ngFor=\"let type of types\" [ngValue]=\"type\">{{type.name}}</option>\r\n              </select>\r\n            </div>\r\n          </div>\r\n\r\n          <div class=\"form-group row\" *ngIf=\"viewType && viewType.id != 'landscape'\">\r\n            <label class=\"col-sm-2 col-form-label\" for=\"viewType\">\r\n              Component to analysis <span class=\"required\">*</span>\r\n            </label>\r\n            <div class=\"col-sm-10\">\r\n              <select class=\"form-control\" [(ngModel)]=\"chosenComponent\" id=\"component\">\r\n                <option *ngFor=\"let component of components | async\" [ngValue]=\"component\">{{component}}</option>\r\n              </select>\r\n            </div>\r\n          </div>\r\n\r\n          <div class=\"form-group row\">\r\n            <div class=\"item form-group\">\r\n              <label\r\n                      class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                      style=\"margin-top: 7px;\">\r\n                Use shelf life period\r\n              </label>\r\n\r\n              <div class=\"col-md-6 col-sm-6 col-xs-12\" >\r\n                <ui-switch\r\n                        [(ngModel)]=\"useShelfLife\"\r\n                        [size]=\"'small'\">\r\n                </ui-switch>\r\n              </div>\r\n            </div>\r\n          </div>\r\n\r\n          <div class=\"form-group row\">\r\n            <div class=\"item form-group\" *ngIf=\"useShelfLife\">\r\n              <label\r\n                      class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                      for=\"shelfLifeInputSelect\"\r\n                      style=\"margin-top: 7px;\">\r\n                Set shelf life duration (pick from list)\r\n              </label>\r\n\r\n              <div class=\"col-md-6 col-sm-6 col-xs-12\" >\r\n                <select class=\"form-control\" [(ngModel)]=\"shelfLife\" id=\"shelfLifeInputSelect\">\r\n                  <option *ngFor=\"let period of periods\" [ngValue]=\"period.period\">{{period.description}}</option>\r\n                </select>\r\n              </div>\r\n            </div>\r\n          </div>\r\n\r\n          <div class=\"form-group row\">\r\n            <div class=\"item form-group\" *ngIf=\"useShelfLife\">\r\n              <label\r\n                      class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                      for=\"shelfLifeInputText\"\r\n                      style=\"margin-top: 7px;\">\r\n                Set shelf life duration (set manually in seconds)\r\n              </label>\r\n\r\n              <div class=\"col-md-6 col-sm-6 col-xs-12\" >\r\n                <input type=\"number\" id=\"shelfLifeInputText\" [(ngModel)]=\"shelfLife\" class=\"form-control\" placeholder=\"Input shelf life in seconds\"/>\r\n              </div>\r\n            </div>\r\n          </div>\r\n\r\n          <div class=\"ln_solid\"></div>\r\n\r\n          <div class=\"row\">\r\n            <div class=\"col-sm-1 col-sm-offset-11\">\r\n              <div class=\"btn btn-primary\" (click)=\"saveView()\">Save</div>\r\n            </div>\r\n          </div>\r\n\r\n        </div>\r\n      </panel>\r\n\r\n      <panel [header]=\"'Description'\" [column]=\"'4'\">\r\n        <div class=\"row\">\r\n          <div class=\"col-sm-12 pagination-centered\" *ngIf=\"!viewType\">\r\n              Please select a type to get a description\r\n          </div>\r\n          <div class=\"col-sm-12\" *ngIf=\"viewType\">\r\n            {{viewType.description}}\r\n          </div>\r\n        </div>\r\n      </panel>\r\n\r\n    </div>\r\n  </div>\r\n</div>\r\n\r\n"

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

module.exports = "<div id=\"viewMenu\" [class.hided]=\"!showSettings\" *ngIf=\"isAllowed()\">\r\n    <div role=\"tabpanel\" data-example-id=\"togglable-tabs\">\r\n        <ul id=\"myTab1\" class=\"nav nav-tabs bar_tabs\" role=\"tablist\">\r\n            <li role=\"presentation\" class=\"\"><a href=\"#tab_info\" id=\"home-tabb\" role=\"tab\" data-toggle=\"tab\"\r\n                                                aria-controls=\"info\" aria-expanded=\"false\"\r\n                                                *ngIf=\"metadata && nodeSelected\">Information</a></li>\r\n            <li role=\"presentation\" class=\"active\"><a href=\"#tab_settings\" role=\"tab\" id=\"profile-tabb\"\r\n                                                      data-toggle=\"tab\" aria-controls=\"settings\" aria-expanded=\"true\">Nodes\r\n                settings</a></li>\r\n            <li role=\"presentation\" class=\"\"><a href=\"#tab_cy\" role=\"tab\" id=\"cy-tabb\" data-toggle=\"tab\"\r\n                                                aria-controls=\"cy\" aria-expanded=\"false\">Graph settings</a></li>\r\n        </ul>\r\n        <div id=\"myTabContent2\" class=\"tab-content\">\r\n            <div role=\"tabpanel\" class=\"tab-pane fade \" id=\"tab_info\" aria-labelledby=\"info-tab\"\r\n                 *ngIf=\"metadata && nodeSelected\">\r\n                <div>\r\n                    <timeInterval [id]=\"'availabilityToggle'\" [jsonObject]=\"metadata.availability\"\r\n                                  [title]=\"'Availability'\"></timeInterval>\r\n                    <timeInterval [id]=\"'maxRatePerSecondToggle'\" [jsonObject]=\"metadata.maxRatePerSecond\"\r\n                                  [title]=\"'Max rate per second'\"></timeInterval>\r\n                    <timeInterval [id]=\"'maxResponseTimeToggle'\" [jsonObject]=\"metadata.maxResponseTime\"\r\n                                  [title]=\"'Max response time'\"></timeInterval>\r\n                    <timeInterval [id]=\"'meanRateToggle'\" [jsonObject]=\"metadata.meanRate\"\r\n                                  [title]=\"'Mean rate'\"></timeInterval>\r\n                    <timeInterval [id]=\"'meanResponseTimeToggle'\" [jsonObject]=\"metadata.meanResponseTime\"\r\n                                  [title]=\"'Mean response time'\"></timeInterval>\r\n\r\n                    <div class=\"row\">\r\n                        <dt class=\"col-sm-4\">Channels:</dt>\r\n                        <dd class=\"col-sm-8\">{{metadata.channels}}</dd>\r\n                    </div>\r\n\r\n                    <div class=\"row\">\r\n                        <dt class=\"col-sm-4\">Efficiency:</dt>\r\n                        <dd class=\"col-sm-8\">{{metadata.efficiency}}</dd>\r\n                    </div>\r\n\r\n                    <div class=\"row\">\r\n                        <dt class=\"col-sm-4\">Scalability:</dt>\r\n                        <dd class=\"col-sm-8\">{{metadata.scalability}}</dd>\r\n                    </div>\r\n\r\n                    <div class=\"row\">\r\n                        <dt class=\"col-sm-4\">Response time (90):</dt>\r\n                        <dd class=\"col-sm-8\">{{metadata.responseTime90}}</dd>\r\n                    </div>\r\n\r\n                    <div class=\"row\">\r\n                        <dt class=\"col-sm-4\">Response time (95):</dt>\r\n                        <dd class=\"col-sm-8\">{{metadata.responseTime95}}</dd>\r\n                    </div>\r\n\r\n                    <div class=\"row\">\r\n                        <dt class=\"col-sm-4\">Response time (98):</dt>\r\n                        <dd class=\"col-sm-8\">{{metadata.responseTime98}}</dd>\r\n                    </div>\r\n\r\n                    <div class=\"row\">\r\n                        <dt class=\"col-sm-4\">Response time standard deviation:</dt>\r\n                        <dd class=\"col-sm-8\">{{metadata.responseTimeStdDev}}</dd>\r\n                    </div>\r\n                </div>\r\n                <div *ngIf=\"metadata && !nodeSelected\">{{metadata}}</div>\r\n                <div *ngIf=\"!metadata\">No data is available</div>\r\n            </div>\r\n            <div role=\"tabpanel\" class=\"tab-pane fade active in\" id=\"tab_settings\" aria-labelledby=\"settings-tab\">\r\n                <p>Select needful information to display on graph: </p>\r\n                <checkboxGroup [id]=\"'availabilityCheckboxToggle'\" [title]=\"'Availability'\"\r\n                               [formName]=\"'availability'\"></checkboxGroup>\r\n                <checkboxGroup [id]=\"'maxRatePerSecondCheckboxToggle'\" [title]=\"'Max rate per second'\"\r\n                               [formName]=\"'maxRatePerSecond'\"></checkboxGroup>\r\n                <checkboxGroup [id]=\"'maxResponseTimeCheckboxToggle'\" [title]=\"'Max response time'\"\r\n                               [formName]=\"'maxResponseTime'\"></checkboxGroup>\r\n                <checkboxGroup [id]=\"'meanRateCheckboxToggle'\" [title]=\"'Mean rate'\"\r\n                               [formName]=\"'meanRate'\"></checkboxGroup>\r\n                <checkboxGroup [id]=\"'meanResponseTimeCheckboxToggle'\" [title]=\"'Mean response time'\"\r\n                               [formName]=\"'meanResponseTime'\"></checkboxGroup>\r\n\r\n                <div class=\"row\">\r\n                    <div class=\"form-check\">\r\n                        <label class=\"form-check-label\">\r\n                            <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"'channels'\">\r\n                            Channels\r\n                        </label>\r\n                    </div>\r\n                </div>\r\n\r\n                <div class=\"row\">\r\n                    <div class=\"form-check\">\r\n                        <label class=\"form-check-label\">\r\n                            <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"'efficiency'\">\r\n                            Efficiency\r\n                        </label>\r\n                    </div>\r\n                </div>\r\n\r\n\r\n                <div class=\"row\">\r\n                    <div class=\"form-check\">\r\n                        <label class=\"form-check-label\">\r\n                            <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"'scalability'\">\r\n                            Scalability\r\n                        </label>\r\n                    </div>\r\n                </div>\r\n\r\n\r\n                <div class=\"row\">\r\n                    <div class=\"form-check\">\r\n                        <label class=\"form-check-label\">\r\n                            <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"'responseTime90'\">\r\n                            Response time (90)\r\n                        </label>\r\n                    </div>\r\n                </div>\r\n\r\n\r\n                <div class=\"row\">\r\n                    <div class=\"form-check\">\r\n                        <label class=\"form-check-label\">\r\n                            <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"'responseTime95'\">\r\n                            Response time (95)\r\n                        </label>\r\n                    </div>\r\n                </div>\r\n\r\n\r\n                <div class=\"row\">\r\n                    <div class=\"form-check\">\r\n                        <label class=\"form-check-label\">\r\n                            <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"'responseTime98'\">\r\n                            Response time (98)\r\n                        </label>\r\n                    </div>\r\n                </div>\r\n\r\n\r\n                <div class=\"row\">\r\n                    <div class=\"form-check\">\r\n                        <label class=\"form-check-label\">\r\n                            <input class=\"form-check-input\" type=\"checkbox\" [attr.name]=\"'responseTimeStdDev'\">\r\n                            Response time standard deviation\r\n                        </label>\r\n                    </div>\r\n                </div>\r\n\r\n                <div class=\"row\">\r\n                    <button class=\"btn btn-primary\" (click)=\"saveCheckboxStatus()\">Apply</button>\r\n                </div>\r\n\r\n            </div>\r\n            <div role=\"tabpanel\" class=\"tab-pane fade\" id=\"tab_cy\" aria-labelledby=\"cy-tab\">\r\n\r\n                <div class=\"form-group row\">\r\n                    <label class=\"control-label col-md-3 col-sm-3 col-xs-12 middleVertical\"\r\n                           for=\"layout-select\">Layout</label>\r\n                    <div class=\"col-md-9 col-sm-9 col-xs-12\">\r\n                        <select class=\"form-control\" [(ngModel)]=\"selectedLayout\"\r\n                                (ngModelChange)=\"onChangeLayout($event)\" id=\"layout-select\">\r\n                            <option value=\"random\">Random</option>\r\n                            <option value=\"grid\">Grid</option>\r\n                            <option value=\"circle\">Circle</option>\r\n                            <option value=\"concentric\">Concentric</option>\r\n                            <option value=\"breadthfirst\">Breadthfirst</option>\r\n                            <option value=\"cose\">Cose</option>\r\n                        </select>\r\n                    </div>\r\n                </div>\r\n\r\n                <div class=\"form-group row\">\r\n                    <label for=\"text-size-input\" class=\"col-md-3 col-sm-3 col-xs-12\">Text size (px)</label>\r\n                    <div class=\"col-md-9 col-sm-9 col-xs-12\">\r\n                        <input class=\"form-control\" type=\"number\" [(ngModel)]=\"textSize\" id=\"text-size-input\"\r\n                               placeholder=\"Input text size\" (ngModelChange)=\"onChangeTextSize($event)\">\r\n                    </div>\r\n                </div>\r\n\r\n                <div class=\"form-group row\">\r\n                    <label for=\"text-size-input\" class=\"col-md-3 col-sm-3 col-xs-12\">Text color</label>\r\n                    <div class=\"col-md-9 col-sm-9 col-xs-12\">\r\n                        <input class=\"form-control\" type=\"color\" [(ngModel)]=\"textColor\" id=\"text-color-input\"\r\n                               placeholder=\"Input text color\" (ngModelChange)=\"onChangeTextColor($event)\">\r\n                    </div>\r\n                </div>\r\n\r\n                <div class=\"form-group row\">\r\n                    <label for=\"text-size-input\" class=\"col-md-3 col-sm-3 col-xs-12\">Background color</label>\r\n                    <div class=\"col-md-9 col-sm-9 col-xs-12\">\r\n                        <input class=\"form-control\" type=\"color\" [(ngModel)]=\"backgroundColor\"\r\n                               id=\"background-color-input\" placeholder=\"Input background color\"\r\n                               (ngModelChange)=\"onChangeBackgroundColor($event)\">\r\n                    </div>\r\n                </div>\r\n\r\n                <div class=\"form-group row\">\r\n                    <label for=\"text-size-input\" class=\"col-md-3 col-sm-3 col-xs-12\">Text outline color</label>\r\n                    <div class=\"col-md-9 col-sm-9 col-xs-12\">\r\n                        <input class=\"form-control\" type=\"color\" [(ngModel)]=\"textOutlineColor\"\r\n                               id=\"text-outline-color-input\" placeholder=\"Input text outline color\"\r\n                               (ngModelChange)=\"onChangeTextOutlineColor($event)\">\r\n                    </div>\r\n                </div>\r\n\r\n                <div class=\"form-group row\">\r\n                    <label for=\"text-size-input\" class=\"col-md-3 col-sm-3 col-xs-12\">Text outline width</label>\r\n                    <div class=\"col-md-9 col-sm-9 col-xs-12\">\r\n                        <input class=\"form-control\" type=\"number\" [(ngModel)]=\"textOutlineWidth\"\r\n                               id=\"text-outline-width-input\" placeholder=\"Input text outline width\"\r\n                               (ngModelChange)=\"onChangeTextOutlineWidth($event)\">\r\n                    </div>\r\n                </div>\r\n\r\n                <div class=\"form-group row\">\r\n                    <label for=\"text-size-input\" class=\"col-md-3 col-sm-3 col-xs-12\">Text weight</label>\r\n                    <div class=\"col-md-9 col-sm-9 col-xs-12\">\r\n                        <input class=\"form-control\" type=\"number\" [(ngModel)]=\"textWeight\" id=\"text-weight-input\"\r\n                               placeholder=\"Input text weight\" (ngModelChange)=\"onChangeTextWeight($event)\" step=\"100\"\r\n                               min=\"100\" max=\"900\">\r\n                    </div>\r\n                </div>\r\n\r\n                <div class=\"row\">\r\n                    <h5>Edge settings</h5>\r\n                </div>\r\n\r\n                <div class=\"form-group row\">\r\n                    <label for=\"text-size-input\" class=\"col-md-3 col-sm-3 col-xs-12\">Edge line width</label>\r\n                    <div class=\"col-md-9 col-sm-9 col-xs-12\">\r\n                        <input class=\"form-control\" type=\"number\" [(ngModel)]=\"edgeWidth\" id=\"edge-width-input\"\r\n                               placeholder=\"Input edge line width\" (ngModelChange)=\"onChangeEdgeWidth($event)\">\r\n                    </div>\r\n                </div>\r\n\r\n                <div class=\"form-group row\">\r\n                    <label for=\"text-size-input\" class=\"col-md-3 col-sm-3 col-xs-12\">Edge line color</label>\r\n                    <div class=\"col-md-9 col-sm-9 col-xs-12\">\r\n                        <input class=\"form-control\" type=\"color\" [(ngModel)]=\"edgeLineColor\" id=\"edge-line-color-input\"\r\n                               placeholder=\"Input edge line color\" (ngModelChange)=\"onChangeEdgeLineColor($event)\">\r\n                    </div>\r\n                </div>\r\n\r\n                <div class=\"form-group row\">\r\n                    <label for=\"text-size-input\" class=\"col-md-3 col-sm-3 col-xs-12\">Edge arrow color</label>\r\n                    <div class=\"col-md-9 col-sm-9 col-xs-12\">\r\n                        <input class=\"form-control\" type=\"color\" [(ngModel)]=\"edgeArrowColor\"\r\n                               id=\"edge-arrow-color-input\" placeholder=\"Input edge arrow color\"\r\n                               (ngModelChange)=\"onChangeEdgeArrowColor($event)\">\r\n                    </div>\r\n                </div>\r\n\r\n                <div class=\"form-group row\">\r\n                    <label class=\"control-label col-md-3 col-sm-3 col-xs-12 middleVertical\" for=\"edge-shape-select\">Edge\r\n                        arrow shape</label>\r\n                    <div class=\"col-md-9 col-sm-9 col-xs-12\">\r\n                        <select class=\"form-control\" [(ngModel)]=\"edgeArrowShape\"\r\n                                (ngModelChange)=\"onChangeEdgeArrowShape($event)\" id=\"edge-shape-select\">\r\n                            <option value=\"tee\">Tee</option>\r\n                            <option value=\"triangle\">Triangle</option>\r\n                            <option value=\"triangle-tee\">Triangle tee</option>\r\n                            <option value=\"triangle-backcurve\">Triangle backcurve</option>\r\n                            <option value=\"square\">Square</option>\r\n                            <option value=\"circle\">Circle</option>\r\n                            <option value=\"diamond\">Diamond</option>\r\n                            <option value=\"none\">None</option>\r\n                        </select>\r\n                    </div>\r\n                </div>\r\n\r\n\r\n                <div class=\"form-group row\">\r\n                    <div class=\"item form-group\">\r\n                        <label\r\n                                class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                style=\"margin-top: 7px;\">\r\n                            Use shelf life period\r\n                        </label>\r\n\r\n                        <div class=\"col-md-6 col-sm-6 col-xs-12\" >\r\n                            <ui-switch\r\n                                    [(ngModel)]=\"useShelfLife\"\r\n                                    (ngModelChange)=\"triggerUseShelfLife($event)\"\r\n                                    [size]=\"'small'\">\r\n                            </ui-switch>\r\n                        </div>\r\n                    </div>\r\n                </div>\r\n\r\n                <div class=\"form-group row\">\r\n                    <div class=\"item form-group\" *ngIf=\"useShelfLife\">\r\n                        <label\r\n                                class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                for=\"shelfLifeInputSelect\"\r\n                                style=\"margin-top: 7px;\">\r\n                            Set shelf life duration (pick from list)\r\n                        </label>\r\n\r\n                        <div class=\"col-md-6 col-sm-6 col-xs-12\" >\r\n                            <select class=\"form-control\" [(ngModel)]=\"shelfLife\" id=\"shelfLifeInputSelect\" (ngModelChange)=\"onChangeShelfLife($event)\">\r\n                                <option *ngFor=\"let period of periods\" [ngValue]=\"period.period\">{{period.description}}</option>\r\n                            </select>\r\n                        </div>\r\n                    </div>\r\n                </div>\r\n\r\n                <div class=\"form-group row\">\r\n                    <div class=\"item form-group\" *ngIf=\"useShelfLife\">\r\n                        <label\r\n                                class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                for=\"shelfLifeInputText\"\r\n                                style=\"margin-top: 7px;\">\r\n                            Set shelf life duration (set manually in seconds)\r\n                        </label>\r\n\r\n                        <div class=\"col-md-9 col-sm-9 col-xs-12\" >\r\n                            <div class=\"input-group\">\r\n                                <input class=\"form-control\"\r\n                                       type=\"number\"\r\n                                       id=\"shelfLifeInputText\"\r\n                                       [(ngModel)]=\"shelfLife\"\r\n                                       (ngModelChange)=\"triggerShelfLifeChanged($event)\"\r\n                                       class=\"form-control\"\r\n                                       placeholder=\"Input shelf life in seconds\">\r\n                              <span class=\"input-group-btn\">\r\n                                 <button class=\"btn btn-secondary\"\r\n                                         [class.btn-primary]=\"shelfLifeChanged\"\r\n                                         type=\"button\"\r\n                                         (click)=\"saveShelfLife()\"\r\n                                         [disabled]=\"!shelfLifeChanged\">Save\r\n                                 </button>\r\n                              </span>\r\n                            </div>\r\n                        </div>\r\n                    </div>\r\n                </div>\r\n\r\n                <div class=\"row\">\r\n                    <button class=\"btn btn-primary\" (click)=\"resetView()\">Reset view</button>\r\n                </div>\r\n\r\n            </div>\r\n        </div>\r\n    </div>\r\n</div>\r\n\r\n<div class=\"right_col\" role=\"main\" style=\"min-height: 949px;\">\r\n\r\n    <div class=\"row\" style=\"padding-right: 10px;\">\r\n        <div class=\"col-md-4\" style=\"padding-top: 20px;\">\r\n            <h3>End-to-End {{(currentViewObs | async)?.name}} {{'(' + ((currentViewObs | async)?.type) + ')'}}</h3>\r\n        </div>\r\n\r\n        <div class=\"col-md-6\"></div>\r\n\r\n        <div class=\"col-md-2\" style=\"padding-top: 20px;\">\r\n\r\n            <div class=\"row\" style=\"float: right\" *ngIf=\"isAllowed()\">\r\n                <button class=\"btn btn-danger\" (click)=\"removeView()\"><i class=\"fa fa-trash\"></i></button>\r\n                <button class=\"btn btn-primary\" id=\"btnSettings\" (click)=\"toggleShowSettings()\"><i class=\"fa\" [class.fa-angle-up]=\"showSettings\" [class.fa-angle-down]=\"!showSettings\"></i> Settings</button>\r\n            </div>\r\n        </div>\r\n    </div>\r\n\r\n    <hr/>\r\n\r\n    <div class=\"clearfix\"></div>\r\n\r\n    <div class=\"row container\">\r\n        <div class=\"col-md-12\">\r\n            <div [attr.id]=\"(currentViewObs | async)?.id\" class=\"cyObject\" style=\"min-height: 900px;\"></div>\r\n        </div>\r\n    </div>\r\n\r\n</div>\r\n\r\n"

/***/ }

});
//# sourceMappingURL=3.map