webpackJsonpac__name_([1],{

/***/ "./node_modules/css-loader/index.js!./src/app/watchers/templates/css/main.css":
/***/ function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("./node_modules/css-loader/lib/css-base.js")();
// imports


// module
exports.push([module.i, ".form-group {\r\n    margin-bottom: 10px;\r\n    display: flex;\r\n}\r\n\r\n.btn-inline {\r\n    display: inline-block;\r\n    margin-left: 15px;\r\n}\r\n\r\n.btn-pull-right {\r\n    margin-top:20px;\r\n    float:right;\r\n    margin-right: 20px;\r\n}\r\n\r\n.row-margin-bottom-sm {\r\n    margin-bottom: 15px;\r\n}\r\n\r\n.activeTr {\r\n    border-left: 2px solid rgb(30, 6, 255) !important;\r\n    background-color: #b9c7de;\r\n}\r\n\r\n.img-inline {\r\n    display: inline-block;\r\n    margin-right: 10px;\r\n    padding: 3px;\r\n}\r\n\r\n.clickableTr {\r\n    cursor: pointer;\r\n}\r\n\r\n.input-group-addon {\r\n    border: 0 !important;\r\n    background-color: rgba(0,0,0,0) !important;\r\n}\r\n\r\n.active-tr {\r\n    border-left: 2px solid rgb(30, 6, 255) !important;\r\n}", ""]);

// exports


/***/ },

/***/ "./node_modules/css-loader/index.js!./src/app/watchers/templates/css/statuses.css":
/***/ function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("./node_modules/css-loader/lib/css-base.js")();
// imports


// module
exports.push([module.i, "/* grid-item\r\n------------------------- */\r\n\r\n.grid-item {\r\n  position: relative;\r\n  float: left;\r\n  width: 123px;\r\n  height: 123px;\r\n  margin: 10px;\r\n  padding: 10px;\r\n  background: #888;\r\n  color: #262524;\r\n}\r\n\r\n.componentOk {\r\n  background: #228B22 !important;\r\n}\r\n\r\n.componentError  {\r\n  background: #F08080 !important;\r\n}\r\n\r\n.grid-item > * {\r\n  margin: 0;\r\n  padding: 0;\r\n}\r\n\r\n.grid-item .name {\r\n  position: absolute;\r\n\r\n  left: 10px;\r\n  top: 55px;\r\n  text-transform: none;\r\n  letter-spacing: 0;\r\n  font-weight: normal;\r\n}\r\n\r\n.grid-item .symbol {\r\n  position: absolute;\r\n  left: 10px;\r\n  top: 0px;\r\n  font-weight: bold;\r\n  color: white;\r\n}\r\n\r\n.grid-item .number {\r\n  position: absolute;\r\n  right: 8px;\r\n  top: 5px;\r\n}\r\n\r\n.grid-item .weight {\r\n  position: absolute;\r\n  left: 9px;\r\n  top: 107px;\r\n  font-size: 0.8em;\r\n}\r\n\r\n.grid-item .bundleInfo {\r\n  position: absolute;\r\n  left: 117px;\r\n  top: 117px;\r\n  font-size: medium;\r\n}\r\n\r\n.grid-item-link {\r\n    display: inline-block;\r\n    border: 1px solid #dddddd;\r\n    border-radius: 4px;\r\n    -webkit-transition: border 0.2s ease-in-out;\r\n    -o-transition: border 0.2s ease-in-out;\r\n    transition: border 0.2s ease-in-out;\r\n    margin: 5px 5px 20px 5px;\r\n}\r\n\r\na.grid-item-link:hover,\r\na.grid-item-link:focus,\r\na.grid-item-link.active {\r\n  border-color: #337ab7;\r\n}", ""]);

// exports


/***/ },

/***/ "./src/app/watchers/components/checkers.component.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var CheckersComponent = (function () {
    function CheckersComponent() {
        this.entity = {};
        this.hideDetails = false;
    }
    __decorate([
        core_1.Input(), 
        __metadata('design:type', Object)
    ], CheckersComponent.prototype, "entity", void 0);
    __decorate([
        core_1.Input(), 
        __metadata('design:type', Boolean)
    ], CheckersComponent.prototype, "hideDetails", void 0);
    CheckersComponent = __decorate([
        core_1.Component({
            moduleId: module.i,
            selector: 'checkers',
            template: __webpack_require__("./src/app/watchers/components/templates/checkers.html"),
            styles: ['.btn-inline { display: inline-block; margin-left: 15px; } .normalspaces {  white-space: normal; }']
        }), 
        __metadata('design:paramtypes', [])
    ], CheckersComponent);
    return CheckersComponent;
}());
exports.CheckersComponent = CheckersComponent;


/***/ },

/***/ "./src/app/watchers/components/condition.block.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var colored_predicate_1 = __webpack_require__("./src/app/watchers/model/colored.predicate.ts");
var constant_attribute_predicate_1 = __webpack_require__("./src/app/watchers/model/constant.attribute.predicate.ts");
var number_comparator_predicate_1 = __webpack_require__("./src/app/watchers/model/number.comparator.predicate.ts");
var range_comparator_1 = __webpack_require__("./src/app/watchers/model/range.comparator.ts");
var ColoredCondition = (function () {
    function ColoredCondition() {
        this.entity = undefined;
        this.notify = new core_1.EventEmitter();
        this.entityType = "";
        this.conditionsType = EntityWithDescription.generateConditionsTypes();
        this.constantExpressions = EntityWithDescription.generateTrueFalseTypes();
        this.operators = EntityWithDescription.generateOperatorsTypes();
        this.rangeOperators = EntityWithDescription.generateRangeTypes();
    }
    ColoredCondition.prototype.isConstantType = function () {
        return (this.entity instanceof constant_attribute_predicate_1.ConstantAttributePredicate);
    };
    ColoredCondition.prototype.isOperatorType = function () {
        return (this.entity instanceof number_comparator_predicate_1.NumberComparatorPredicate);
    };
    ColoredCondition.prototype.isRangeType = function () {
        return (this.entity instanceof range_comparator_1.IsInRangePredicate);
    };
    ColoredCondition.prototype.onTypeChange = function (event) {
        switch (event) {
            case "ConstantAttributePredicate":
                this.entity = new constant_attribute_predicate_1.ConstantAttributePredicate();
                break;
            case "NumberComparatorPredicate":
                this.entity = new number_comparator_predicate_1.NumberComparatorPredicate();
                break;
            case "IsInRangePredicate":
                this.entity = new range_comparator_1.IsInRangePredicate();
                break;
            default:
                throw new Error("Could not recognize yellow checker type: " + event);
        }
        this.entityType = event;
        this.notify.emit(this.entity);
    };
    ColoredCondition.prototype.onAnyChange = function () {
        this.notify.emit(this.entity);
    };
    ColoredCondition.prototype.ngOnInit = function () {
        this.entityType = (this.entity != undefined) ? this.entity.constructor.name : "";
    };
    ColoredCondition.prototype.ngAfterViewInit = function () {
        console.debug("Entity: ", this.entity, ", entityType: ", this.entityType);
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', (typeof (_a = typeof colored_predicate_1.ColoredAttributePredicate !== 'undefined' && colored_predicate_1.ColoredAttributePredicate) === 'function' && _a) || Object)
    ], ColoredCondition.prototype, "entity", void 0);
    __decorate([
        core_1.Output(), 
        __metadata('design:type', (typeof (_b = typeof core_1.EventEmitter !== 'undefined' && core_1.EventEmitter) === 'function' && _b) || Object)
    ], ColoredCondition.prototype, "notify", void 0);
    ColoredCondition = __decorate([
        core_1.Component({
            moduleId: module.i,
            selector: 'coloredCondition',
            template: __webpack_require__("./src/app/watchers/components/templates/condition.html")
        }), 
        __metadata('design:paramtypes', [])
    ], ColoredCondition);
    return ColoredCondition;
    var _a, _b;
}());
exports.ColoredCondition = ColoredCondition;
var EntityWithDescription = (function () {
    function EntityWithDescription(id, description) {
        this.id = id;
        this.description = description;
    }
    EntityWithDescription.generateConditionsTypes = function () {
        var _value = [];
        _value.push(new EntityWithDescription("ConstantAttributePredicate", "Boolean constant"));
        _value.push(new EntityWithDescription("NumberComparatorPredicate", "Compare with certain value"));
        _value.push(new EntityWithDescription("IsInRangePredicate", "Range comparator"));
        return _value;
    };
    EntityWithDescription.generateTrueFalseTypes = function () {
        var _value = [];
        _value.push(new EntityWithDescription(true, "True"));
        _value.push(new EntityWithDescription(false, "False"));
        return _value;
    };
    EntityWithDescription.generateOperatorsTypes = function () {
        var _value = [];
        _value.push(new EntityWithDescription("GREATER_THAN", ">"));
        _value.push(new EntityWithDescription("GREATER_THAN_OR_EQUAL", "≥"));
        _value.push(new EntityWithDescription("LESS_THAN", "<"));
        _value.push(new EntityWithDescription("LESS_THAN_OR_EQUAL", "≤"));
        _value.push(new EntityWithDescription("EQUAL", "="));
        _value.push(new EntityWithDescription("NOT_EQUAL", "≠"));
        return _value;
    };
    EntityWithDescription.generateRangeTypes = function () {
        var _value = [];
        _value.push(new EntityWithDescription(false, ">"));
        _value.push(new EntityWithDescription(true, "≥"));
        return _value;
    };
    return EntityWithDescription;
}());
exports.EntityWithDescription = EntityWithDescription;


/***/ },

/***/ "./src/app/watchers/components/policies.component.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var attribute_based_scaling_policy_1 = __webpack_require__("./src/app/watchers/model/policy/attribute.based.scaling.policy.ts");
var health_status_based_scaling_policy_1 = __webpack_require__("./src/app/watchers/model/policy/health.status.based.scaling.policy.ts");
var PoliciesComponent = (function () {
    function PoliciesComponent() {
        this.entity = {};
        this.hideDetails = false;
    }
    PoliciesComponent.prototype.isAttrType = function (policy) {
        return policy instanceof attribute_based_scaling_policy_1.AttributeBasedScalingPolicy;
    };
    PoliciesComponent.prototype.isHealthStatusType = function (policy) {
        return policy instanceof health_status_based_scaling_policy_1.HealthStatusBasedScalingPolicy;
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', Object)
    ], PoliciesComponent.prototype, "entity", void 0);
    __decorate([
        core_1.Input(), 
        __metadata('design:type', Boolean)
    ], PoliciesComponent.prototype, "hideDetails", void 0);
    PoliciesComponent = __decorate([
        core_1.Component({
            moduleId: module.i,
            selector: 'policies',
            template: __webpack_require__("./src/app/watchers/components/templates/policies.html"),
            styles: ['.btn-inline { display: inline-block; margin-left: 15px; } .normalspaces {  white-space: normal; }']
        }), 
        __metadata('design:paramtypes', [])
    ], PoliciesComponent);
    return PoliciesComponent;
}());
exports.PoliciesComponent = PoliciesComponent;


/***/ },

/***/ "./src/app/watchers/components/ptable.component.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var model_entity_1 = __webpack_require__("./src/app/configuration/model/model.entity.ts");
var inline_edit_component_1 = __webpack_require__("./src/app/controls/editor/inline-edit.component.ts");
var vex_1 = __webpack_require__("./node_modules/angular2-modal/plugins/vex/index.js");
var PTable = (function () {
    function PTable(modal, cd) {
        this.modal = modal;
        this.cd = cd;
    }
    PTable.prototype.ngOnInit = function () { };
    PTable.prototype.checkAndRemoveParameter = function (parameter) {
        var _this = this;
        this.modal.confirm()
            .className('default')
            .isBlocking(true)
            .keyboard(27)
            .message("Remove parameter " + parameter.key + "?")
            .open()
            .then(function (resultPromise) {
            return resultPromise.result
                .then(function (response) {
                _this.entity.removeParameter(parameter.key);
                return response;
            })
                .catch(function () { return false; });
        }).catch(function () { return false; });
    };
    PTable.prototype.addNewParameter = function () {
        this.entity.parameters.push(new model_entity_1.KeyValue("newParamKey", "newParamValue"));
        this.cd.detectChanges();
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', (typeof (_a = typeof model_entity_1.Entity !== 'undefined' && model_entity_1.Entity) === 'function' && _a) || Object)
    ], PTable.prototype, "entity", void 0);
    __decorate([
        core_1.ViewChildren(inline_edit_component_1.InlineEditComponent), 
        __metadata('design:type', (typeof (_b = typeof core_1.QueryList !== 'undefined' && core_1.QueryList) === 'function' && _b) || Object)
    ], PTable.prototype, "editComponents", void 0);
    PTable = __decorate([
        core_1.Component({
            moduleId: module.i,
            selector: 'ptable',
            template: __webpack_require__("./src/app/watchers/components/templates/ptable.component.html"),
            encapsulation: core_1.ViewEncapsulation.None
        }), 
        __metadata('design:paramtypes', [(typeof (_c = typeof vex_1.Modal !== 'undefined' && vex_1.Modal) === 'function' && _c) || Object, (typeof (_d = typeof core_1.ChangeDetectorRef !== 'undefined' && core_1.ChangeDetectorRef) === 'function' && _d) || Object])
    ], PTable);
    return PTable;
    var _a, _b, _c, _d;
}());
exports.PTable = PTable;


/***/ },

/***/ "./src/app/watchers/components/templates/checkers.html":
/***/ function(module, exports) {

module.exports = "<div>\r\n    <dl class=\"row\" *ngFor=\"let entry of entity | keys\">\r\n\r\n        <div class=\"modal fade\" *ngIf=\"!hideDetails\" [attr.id]=\"'details_' + entry.value.id\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"detailsLabel\">\r\n            <div class=\"modal-dialog modal-lg\" role=\"document\">\r\n                <div class=\"modal-content\">\r\n                    <div class=\"modal-header\">\r\n                        <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>\r\n                        <h4 class=\"modal-title leftAlign\" id=\"detailsLabel\">Entity {{entry.value.name}} details</h4>\r\n                    </div>\r\n                    <br/>\r\n                    <div class=\"modal-body\" *ngIf=\"entry.value.object == undefined\">\r\n                        <pre class=\"normalspaces\"><code [innerHTML]=\"entry.value.script\"></code></pre>\r\n                    </div>\r\n                    <div class=\"modal-body\" *ngIf=\"entry.value.object != undefined\">\r\n                        <div class=\"alert alert-success\">\r\n                            <strong>Green condition: </strong>\r\n                            <div>\r\n                                <strong>{{entry.value.object.green.represent()}}</strong>\r\n                            </div>\r\n                        </div>\r\n\r\n                        <div class=\"alert alert-warning\">\r\n                            <strong>Yellow condition: </strong>\r\n                            <div>\r\n                                <strong>{{entry.value.object.yellow.represent()}}</strong>\r\n                            </div>\r\n                        </div>\r\n                    </div>\r\n                    <div class=\"modal-footer\">\r\n                        <button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">Close</button>\r\n                    </div>\r\n                </div>\r\n            </div>\r\n        </div>\r\n\r\n        <dt class=\"col-sm-3\">Attribute:</dt>\r\n        <dd class=\"col-sm-9\">{{entry.key}}</dd>\r\n\r\n        <dt class=\"col-sm-3\">Language:</dt>\r\n        <dd class=\"col-sm-9\">{{entry.value.language}}</dd>\r\n\r\n        <dt class=\"col-sm-3\">Is url:</dt>\r\n        <dd class=\"col-sm-9\">{{entry.value.isURL}}</dd>\r\n\r\n        <dt class=\"col-sm-3\">Script:</dt>\r\n        <dd class=\"col-sm-9\">\r\n            <button *ngIf=\"!hideDetails\"\r\n                    class=\"center-block btn btn-inline btn-sm\"\r\n                    data-toggle=\"modal\"\r\n                    [attr.data-target]=\"'#details_' + entry.value.id\">\r\n                <i class=\"fa fa-search\"></i> Details\r\n            </button>\r\n        </dd>\r\n    </dl>\r\n</div>"

/***/ },

/***/ "./src/app/watchers/components/templates/condition.html":
/***/ function(module, exports) {

module.exports = "<div class=\"item form-inline\" style=\"margin-bottom: 15px;\">\r\n    <label\r\n            class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n            style=\"margin-top: 7px;\">\r\n        Type of condition <span class=\"required\">*</span>\r\n    </label>\r\n    <select class=\"form-control\" [(ngModel)]=\"entityType\" (ngModelChange)=\"onTypeChange($event)\">\r\n        <option *ngFor=\"let type of conditionsType\" [ngValue]=\"type.id\">{{type.description}}</option>\r\n    </select>\r\n</div>\r\n\r\n<div *ngIf=\"isConstantType()\">\r\n    <div class=\"item form-inline\">\r\n        <label\r\n                class=\"custom-control col-md-3 col-sm-3 col-xs-8\"\r\n                style=\"margin-top: 7px;\">\r\n            Value is <span class=\"required\">*</span>\r\n        </label>\r\n        <select class=\"form-control col-md-1 col-sm-1 col-xs-4\" [(ngModel)]=\"entity.value\" (ngModelChange)=\"onAnyChange()\">\r\n            <option *ngFor=\"let exp of constantExpressions\" [ngValue]=\"exp.id\">{{exp.description}}</option>\r\n        </select>\r\n    </div>\r\n</div>\r\n\r\n<div *ngIf=\"isOperatorType()\">\r\n    <div class=\"item form-inline\">\r\n        <label\r\n                class=\"col-md-2 col-sm-2 col-xs-5\"\r\n                style=\"margin-top: 7px;\">\r\n            Value\r\n        </label>\r\n        <select class=\"form-control col-md-1 col-sm-1 col-xs-1\" [(ngModel)]=\"entity.operator\" (ngModelChange)=\"onAnyChange()\">\r\n            <option *ngFor=\"let operator of operators\" [ngValue]=\"operator.id\">{{operator.description}}</option>\r\n        </select>\r\n        <input type=\"number\" class=\"form-control col-md-offset-1 col-md-3 col-sm-3 col-xs-6\" [(ngModel)]=\"entity.value\" (ngModelChange)=\"onAnyChange()\"/>\r\n    </div>\r\n</div>\r\n\r\n<div *ngIf=\"isRangeType()\">\r\n    <div class=\"item form-inline\">\r\n        <input type=\"number\" class=\"form-control col-md-2 col-sm-2 col-xs-2\" [(ngModel)]=\"entity.rangeEnd\" (ngModelChange)=\"onAnyChange()\"/>\r\n        <select class=\"form-control col-md-1 col-sm-1 col-xs-1\" [(ngModel)]=\"entity.isRangeEndInclusive\" (ngModelChange)=\"onAnyChange()\">\r\n            <option *ngFor=\"let operator of rangeOperators\" [ngValue]=\"operator.id\">{{operator.description}}</option>\r\n        </select>\r\n        <label class=\"control-label col-md-1 col-sm-1 col-xs-1\" style=\"margin-top: 7px;\">\r\n            value\r\n        </label>\r\n        <select class=\"form-control col-md-1 col-sm-1 col-xs-1\" [(ngModel)]=\"entity.isRangeStartInclusive\" (ngModelChange)=\"onAnyChange()\">\r\n            <option *ngFor=\"let operator of rangeOperators\" [ngValue]=\"operator.id\">{{operator.description}}</option>\r\n        </select>\r\n        <input type=\"number\" class=\"form-control col-md-2 col-sm-2 col-xs-2\" [(ngModel)]=\"entity.rangeStart\" (ngModelChange)=\"onAnyChange()\"/>\r\n    </div>\r\n</div>"

/***/ },

/***/ "./src/app/watchers/components/templates/policies.html":
/***/ function(module, exports) {

module.exports = "<div>\r\n    <dl class=\"row\" *ngFor=\"let entry of entity | keys\">\r\n\r\n        <div class=\"modal fade\" *ngIf=\"!hideDetails\" [attr.id]=\"'details_' + entry.value.id\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"detailsLabel\">\r\n            <div class=\"modal-dialog modal-lg\" role=\"document\">\r\n                <div class=\"modal-content\">\r\n                    <div class=\"modal-header\">\r\n                        <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>\r\n                        <h4 class=\"modal-title leftAlign\" id=\"detailsLabel\">Entity {{entry.value.name}} details</h4>\r\n                    </div>\r\n                    <br/>\r\n                    <div class=\"modal-body\" *ngIf=\"entry.value.policyObject == undefined\">\r\n                        <pre class=\"normalspaces\"><code [innerHTML]=\"entry.value.script\"></code></pre>\r\n                    </div>\r\n                    <div *ngIf=\"entry.value.policyObject != undefined\">\r\n                        <div class=\"modal-body\" *ngIf=\"isAttrType(entry.value.policyObject)\">\r\n                            <dl class=\"row\">\r\n                                <dt class=\"col-sm-3\">Attribute name:</dt>\r\n                                <dd class=\"col-sm-9\">{{entry.value.policyObject.attributeName}}</dd>\r\n\r\n                                <dt class=\"col-sm-3\">Operational range:</dt>\r\n                                <dd class=\"col-sm-9\">{{entry.value.policyObject.operationalRange.toString()}}</dd>\r\n\r\n                                <dt class=\"col-sm-3\">Aggregation:</dt>\r\n                                <dd class=\"col-sm-9\">{{entry.value.policyObject.aggregation}}</dd>\r\n\r\n                                <dt class=\"col-sm-3\">Analysis depth:</dt>\r\n                                <dd class=\"col-sm-9\">{{entry.value.policyObject.formatAnalysisDepth()}}</dd>\r\n\r\n                                <dt class=\"col-sm-3\">Vote weight:</dt>\r\n                                <dd class=\"col-sm-9\">{{entry.value.policyObject.voteWeight}}</dd>\r\n\r\n                                <dt class=\"col-sm-3\">Incremental weight:</dt>\r\n                                <dd class=\"col-sm-9\">{{entry.value.policyObject.incrementalWeight}}</dd>\r\n\r\n                                <dt class=\"col-sm-3\">Observation time:</dt>\r\n                                <dd class=\"col-sm-9\">{{entry.value.policyObject.formatObservationTime()}}</dd>\r\n                            </dl>\r\n                        </div>\r\n                        <div class=\"modal-body\" *ngIf=\"isHealthStatusType(entry.value.policyObject)\">\r\n                            <dl class=\"row\">\r\n                                <dt class=\"col-sm-3\">Level:</dt>\r\n                                <dd class=\"col-sm-9\">{{entry.value.policyObject.level}}</dd>\r\n\r\n                                <dt class=\"col-sm-3\">Vote weight:</dt>\r\n                                <dd class=\"col-sm-9\">{{entry.value.policyObject.voteWeight}}</dd>\r\n\r\n                                <dt class=\"col-sm-3\">Incremental weight:</dt>\r\n                                <dd class=\"col-sm-9\">{{entry.value.policyObject.incrementalWeight}}</dd>\r\n\r\n                                <dt class=\"col-sm-3\">Observation time:</dt>\r\n                                <dd class=\"col-sm-9\">{{entry.value.policyObject.formatObservationTime()}}</dd>\r\n                            </dl>\r\n                        </div>\r\n                    </div>\r\n                    <div class=\"modal-footer\">\r\n                        <button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">Close</button>\r\n                    </div>\r\n                </div>\r\n            </div>\r\n        </div>\r\n\r\n        <dt class=\"col-sm-3\">Policy type:</dt>\r\n        <dd class=\"col-sm-9\">{{entry.value.getPolicyType()}}</dd>\r\n\r\n        <dt class=\"col-sm-3\">Language:</dt>\r\n        <dd class=\"col-sm-9\">{{entry.value.language}}</dd>\r\n\r\n        <dt class=\"col-sm-3\">Is url:</dt>\r\n        <dd class=\"col-sm-9\">{{entry.value.isURL}}</dd>\r\n\r\n        <dt class=\"col-sm-3\">Script:</dt>\r\n        <dd class=\"col-sm-9\">\r\n            <button *ngIf=\"!hideDetails\"\r\n                    class=\"center-block btn btn-inline btn-sm\"\r\n                    data-toggle=\"modal\"\r\n                    [attr.data-target]=\"'#details_' + entry.value.id\">\r\n                <i class=\"fa fa-search\"></i> Details\r\n            </button>\r\n        </dd>\r\n    </dl>\r\n</div>"

/***/ },

/***/ "./src/app/watchers/components/templates/ptable.component.html":
/***/ function(module, exports) {

module.exports = "<table class=\"table\" *ngIf=\"entity.parameters && entity.parameters.length > 0\">\r\n    <thead>\r\n    <tr>\r\n        <th>#</th>\r\n        <th>Key</th>\r\n        <th>Value</th>\r\n    </tr>\r\n    </thead>\r\n    <tbody>\r\n    <tr *ngFor=\"let param of entity.parameters\">\r\n        <td>\r\n            <span\r\n                class=\"glyphicon glyphicon-remove-circle btn btn-xs btn-danger\"\r\n                (click)=\"checkAndRemoveParameter(param)\"\r\n                aria-hidden=\"true\">\r\n            </span>\r\n        </td>\r\n        <td>\r\n            <inline-edit [(ngModel)]=\"param.key\"\r\n                         [ngModelOptions]=\"{standalone: true}\">\r\n            </inline-edit>\r\n        </td>\r\n        <td>\r\n            <inline-edit [(ngModel)]=\"param.value\"\r\n                         [ngModelOptions]=\"{standalone: true}\">\r\n            </inline-edit>\r\n        </td>\r\n    </tr>\r\n    </tbody>\r\n</table>\r\n\r\n<button\r\n        type=\"button\"\r\n        class=\"btn btn-primary btn-sm\"\r\n        (click)=\"addNewParameter()\">\r\n    Add new\r\n</button>"

/***/ },

/***/ "./src/app/watchers/components/templates/trigger.html":
/***/ function(module, exports) {

module.exports = "<div class=\"modal fade\" *ngIf=\"!hideDetails\" [attr.id]=\"'details_' + entity.id\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"addEntityLabel\">\r\n    <div class=\"modal-dialog modal-lg\" role=\"document\">\r\n        <div class=\"modal-content\">\r\n            <div class=\"modal-header\">\r\n                <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>\r\n                <h4 class=\"modal-title leftAlign\" id=\"addEntityLabel\">Entity {{entity.name}} details</h4>\r\n            </div>\r\n            <br/>\r\n            <div class=\"modal-body\">\r\n                <pre class=\"normalspaces\"><code [innerHTML]=\"entity.script\"></code></pre>\r\n            </div>\r\n            <div class=\"modal-footer\">\r\n                <button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">Close</button>\r\n            </div>\r\n        </div>\r\n    </div>\r\n</div>\r\n\r\n\r\n<div>\r\n    <dl class=\"row\" *ngIf=\"!isEmpty()\">\r\n        <dt class=\"col-sm-5\">Language:</dt>\r\n        <dd class=\"col-sm-7\">{{entity.language}}</dd>\r\n\r\n        <dt class=\"col-sm-5\">Is url:</dt>\r\n        <dd class=\"col-sm-7\">{{entity.isURL}}</dd>\r\n\r\n        <dt class=\"col-sm-5\">Script:</dt>\r\n        <dd class=\"col-sm-7\">\r\n            <button *ngIf=\"!hideDetails\"\r\n                    class=\"center-block btn btn-inline btn-sm\"\r\n                    data-toggle=\"modal\"\r\n                    [attr.data-target]=\"'#details_' + entity.id\">\r\n                <i class=\"fa fa-search\"></i> Details\r\n            </button>\r\n        </dd>\r\n    </dl>\r\n</div>"

/***/ },

/***/ "./src/app/watchers/components/trigger.component.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var scriptlet_data_object_1 = __webpack_require__("./src/app/watchers/model/scriptlet.data.object.ts");
var util_1 = __webpack_require__("./node_modules/util/util.js");
var TriggerComponent = (function () {
    function TriggerComponent() {
        this.entity = undefined;
        this.hideDetails = false;
    }
    TriggerComponent.prototype.isEmpty = function () {
        return (this.entity.language != "Groovy" && util_1.isNullOrUndefined(this.entity.object))
            || (this.entity.language == "Groovy" && (util_1.isNullOrUndefined(this.entity.script) || this.entity.script.length < 3));
    };
    __decorate([
        core_1.Input(), 
        __metadata('design:type', (typeof (_a = typeof scriptlet_data_object_1.ScriptletDataObject !== 'undefined' && scriptlet_data_object_1.ScriptletDataObject) === 'function' && _a) || Object)
    ], TriggerComponent.prototype, "entity", void 0);
    __decorate([
        core_1.Input(), 
        __metadata('design:type', Boolean)
    ], TriggerComponent.prototype, "hideDetails", void 0);
    TriggerComponent = __decorate([
        core_1.Component({
            moduleId: module.i,
            selector: 'trigger',
            template: __webpack_require__("./src/app/watchers/components/templates/trigger.html"),
            styles: ['.btn-inline { display: inline-block; margin-left: 15px; } .normalspaces {  white-space: normal; }']
        }), 
        __metadata('design:paramtypes', [])
    ], TriggerComponent);
    return TriggerComponent;
    var _a;
}());
exports.TriggerComponent = TriggerComponent;


/***/ },

/***/ "./src/app/watchers/model/colored.checker.ts":
/***/ function(module, exports) {

"use strict";
"use strict";
var ColoredAttributeChecker = (function () {
    function ColoredAttributeChecker() {
        this.green = undefined;
        this.yellow = undefined;
    }
    ColoredAttributeChecker.prototype.toJSON = function () {
        var _value = {};
        _value["green"] = this.green.toJSON();
        _value["yellow"] = this.yellow.toJSON();
        return _value;
    };
    return ColoredAttributeChecker;
}());
exports.ColoredAttributeChecker = ColoredAttributeChecker;


/***/ },

/***/ "./src/app/watchers/model/colored.predicate.ts":
/***/ function(module, exports) {

"use strict";
"use strict";
var ColoredAttributePredicate = (function () {
    function ColoredAttributePredicate() {
    }
    ColoredAttributePredicate.CONSTANT = "constant";
    ColoredAttributePredicate.COMPARATOR = "comparator";
    ColoredAttributePredicate.RANGE = "isInRange";
    return ColoredAttributePredicate;
}());
exports.ColoredAttributePredicate = ColoredAttributePredicate;


/***/ },

/***/ "./src/app/watchers/model/constant.attribute.predicate.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var colored_predicate_1 = __webpack_require__("./src/app/watchers/model/colored.predicate.ts");
var ConstantAttributePredicate = (function (_super) {
    __extends(ConstantAttributePredicate, _super);
    function ConstantAttributePredicate() {
        _super.apply(this, arguments);
        this.type = colored_predicate_1.ColoredAttributePredicate.CONSTANT;
    }
    ConstantAttributePredicate.prototype.toJSON = function () {
        var _value = {};
        _value["@type"] = this.type;
        _value["value"] = this.value;
        return _value;
    };
    ConstantAttributePredicate.prototype.represent = function () {
        return "value = " + (new Boolean(this.value)).toString();
    };
    return ConstantAttributePredicate;
}(colored_predicate_1.ColoredAttributePredicate));
exports.ConstantAttributePredicate = ConstantAttributePredicate;


/***/ },

/***/ "./src/app/watchers/model/entity.ts":
/***/ function(module, exports) {

"use strict";
"use strict";
var Entity = (function () {
    function Entity(name, parameters) {
        this.parameters = [];
        this.name = name;
        this.guid = Guid.newGuid();
        for (var key in parameters) {
            this.parameters.push(new KeyValue(key, parameters[key]));
        }
    }
    Entity.prototype.getParameter = function (key) {
        return KeyValue.getParameterByName(this.parameters, key);
    };
    Entity.prototype.contains = function (key) {
        return this.getParameter(key) != undefined;
    };
    Entity.prototype.removeParameter = function (key) {
        for (var i = 0; i < this.parameters.length; i++) {
            if (this.parameters[i].key == key) {
                this.parameters.splice(i, 1);
                break;
            }
        }
    };
    Entity.prototype.setParameter = function (parameter) {
        var found = false;
        for (var i = 0; i < this.parameters.length; i++) {
            if (this.parameters[i].key == parameter.key) {
                this.parameters[i].value = parameter.value;
                found = true;
                break;
            }
        }
        // if nothing is found - just push it into the array
        if (!found) {
            this.parameters.push(parameter);
        }
    };
    Entity.prototype.clearParameters = function () {
        this.parameters = [];
    };
    Entity.prototype.decapitalizeFirstLetter = function (object) {
        return object.charAt(0).toLowerCase() + object.slice(1);
    };
    // see https://www.stevefenton.co.uk/2013/04/obtaining-a-class-name-at-runtime-in-typescript/
    Entity.prototype.getName = function () {
        var funcNameRegex = /function (.{1,})\(/;
        var results = (funcNameRegex).exec(this.constructor.toString());
        return (results && results.length > 1) ? this.decapitalizeFirstLetter(results[1]) : "";
    };
    Entity.prototype.stringifyParameters = function () {
        return KeyValue.stringifyParametersStatic(this.parameters);
    };
    return Entity;
}());
exports.Entity = Entity;
var KeyValue = (function () {
    function KeyValue(key, value) {
        this.key = key;
        this.value = value;
    }
    ;
    KeyValue.getParameterByName = function (inputParams, inputName) {
        var result = undefined;
        if (inputParams != undefined) {
            for (var i = 0; i < inputParams.length; i++) {
                if (inputName === inputParams[i].key) {
                    result = inputParams[i];
                    break;
                }
            }
        }
        return result;
    };
    KeyValue.stringifyParametersStatic = function (value) {
        var returnValue = {};
        value.forEach(function (obj) {
            returnValue[obj.key] = obj.value;
        });
        return returnValue;
    };
    return KeyValue;
}());
exports.KeyValue = KeyValue;
// http://stackoverflow.com/questions/26501688/a-typescript-guid-class
var Guid = (function () {
    function Guid() {
    }
    Guid.newGuid = function () {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    };
    return Guid;
}());
exports.Guid = Guid;


/***/ },

/***/ "./src/app/watchers/model/factory.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function($) {"use strict";
var watcher_1 = __webpack_require__("./src/app/watchers/model/watcher.ts");
var scriptlet_data_object_1 = __webpack_require__("./src/app/watchers/model/scriptlet.data.object.ts");
var util_1 = __webpack_require__("./node_modules/util/util.js");
var app_utils_1 = __webpack_require__("./src/app/services/app.utils.ts");
var Factory = (function () {
    function Factory() {
    }
    Factory.watcherFromJSON = function (name, json) {
        console.debug("Watcher: ", JSON.stringify(json));
        var _pType = "all";
        if (!util_1.isNullOrUndefined(json["parameters"]) && !util_1.isNullOrUndefined(json["parameters"]["$strategy$"])) {
            _pType = json["parameters"]["$strategy$"];
            delete json["parameters"]["$strategy$"];
        }
        var _watcher = new watcher_1.Watcher(name, json["parameters"]);
        _watcher.votingStrategy = _pType;
        if (json["attributeCheckers"] != undefined && !$.isEmptyObject(json["attributeCheckers"])) {
            for (var key in json["attributeCheckers"]) {
                if (json["attributeCheckers"][key]["language"] != undefined
                    && json["attributeCheckers"][key]["language"].length > 0) {
                    _watcher.attributeCheckers[key] = scriptlet_data_object_1.ScriptletDataObject.fromJSON(json["attributeCheckers"][key]);
                }
            }
        }
        if (json["scalingPolicies"] != undefined && !$.isEmptyObject(json["scalingPolicies"])) {
            for (var key in json["scalingPolicies"]) {
                if (json["scalingPolicies"][key]["language"] != undefined
                    && json["scalingPolicies"][key]["language"].length > 0) {
                    _watcher.scalingPolicies[key] = scriptlet_data_object_1.ScriptletDataObject.fromJSON(json["scalingPolicies"][key]);
                }
            }
        }
        if (json["trigger"] != undefined
            && !$.isEmptyObject(json["trigger"])
            && json["trigger"]["language"] != undefined
            && json["trigger"]["language"].length > 0) {
            _watcher.trigger = scriptlet_data_object_1.ScriptletDataObject.fromJSON(json["trigger"]);
        }
        if (json["connectionStringTemplate"] != undefined) {
            _watcher.connectionStringTemplate = json["connectionStringTemplate"];
        }
        if (json["scalingSize"] != undefined) {
            _watcher.scalingSize = json["scalingSize"];
        }
        if (json["maxClusterSize"] != undefined) {
            _watcher.maxClusterSize = json["maxClusterSize"];
        }
        if (json["minClusterSize"] != undefined) {
            _watcher.minClusterSize = json["minClusterSize"];
        }
        if (json["cooldownTime"] != undefined) {
            _watcher.cooldownTime = app_utils_1.SnampUtils.parseDuration(json["cooldownTime"]);
        }
        if (json["type"] != undefined) {
            _watcher.type = json["type"];
        }
        if (json["autoScaling"] != undefined) {
            _watcher.autoScaling = json["autoScaling"];
        }
        return _watcher;
    };
    Factory.watchersArrayFromJSON = function (json) {
        var result = [];
        for (var key in json) {
            result.push(Factory.watcherFromJSON(key, json[key]));
        }
        return result;
    };
    return Factory;
}());
exports.Factory = Factory;

/* WEBPACK VAR INJECTION */}.call(exports, __webpack_require__("./node_modules/jquery/dist/jquery.js")))

/***/ },

/***/ "./src/app/watchers/model/number.comparator.predicate.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var colored_predicate_1 = __webpack_require__("./src/app/watchers/model/colored.predicate.ts");
var NumberComparatorPredicate = (function (_super) {
    __extends(NumberComparatorPredicate, _super);
    function NumberComparatorPredicate() {
        _super.apply(this, arguments);
        this.type = colored_predicate_1.ColoredAttributePredicate.COMPARATOR;
    }
    NumberComparatorPredicate.prototype.toJSON = function () {
        var _value = {};
        _value["@type"] = this.type;
        _value["operator"] = this.operator;
        _value["value"] = this.value;
        return _value;
    };
    NumberComparatorPredicate.prototype.represent = function () {
        var _value = "value ";
        switch (this.operator) {
            case "GREATER_THAN":
                _value += ">";
                break;
            case "GREATER_THAN_OR_EQUAL":
                _value += "≥";
                break;
            case "LESS_THAN":
                _value += "<";
                break;
            case "LESS_THAN_OR_EQUAL":
                _value += "≤";
                break;
            case "EQUAL":
                _value += "=";
                break;
            case "NOT_EQUAL":
                _value += "≠";
                break;
            default:
                throw new Error("Operator " + this.operator + "cannot be recognized");
        }
        _value += " " + this.value;
        return _value;
    };
    return NumberComparatorPredicate;
}(colored_predicate_1.ColoredAttributePredicate));
exports.NumberComparatorPredicate = NumberComparatorPredicate;


/***/ },

/***/ "./src/app/watchers/model/policy/abstract.policy.ts":
/***/ function(module, exports) {

"use strict";
"use strict";
var AbstractPolicy = (function () {
    function AbstractPolicy() {
    }
    AbstractPolicy.getReduceOperations = function () {
        return ["MAX", "MIN", "MEAN", "MEDIAN", "PERCENTILE_90", "PERCENTILE_95", "PERCENTILE_97", "SUM"];
    };
    AbstractPolicy.prototype.getPoliticType = function () {
        return "Groovy policy";
    };
    AbstractPolicy.prototype.getPolicyWeight = function () {
        return "N/A";
    };
    return AbstractPolicy;
}());
exports.AbstractPolicy = AbstractPolicy;


/***/ },

/***/ "./src/app/watchers/model/policy/abstract.weighted.scaling.policy.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var abstract_policy_1 = __webpack_require__("./src/app/watchers/model/policy/abstract.policy.ts");
var app_utils_1 = __webpack_require__("./src/app/services/app.utils.ts");
var util_1 = __webpack_require__("./node_modules/util/util.js");
var AbstractWeightedScalingPolicy = (function (_super) {
    __extends(AbstractWeightedScalingPolicy, _super);
    function AbstractWeightedScalingPolicy() {
        _super.call(this);
        this.voteWeight = 0;
        this.observationTime = 0;
        this.incrementalWeight = false;
    }
    Object.defineProperty(AbstractWeightedScalingPolicy.prototype, "voteWeight", {
        get: function () {
            return this._voteWeight;
        },
        set: function (value) {
            this._voteWeight = value;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(AbstractWeightedScalingPolicy.prototype, "observationTime", {
        get: function () {
            return this._observationTime;
        },
        set: function (value) {
            this._observationTime = value;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(AbstractWeightedScalingPolicy.prototype, "incrementalWeight", {
        get: function () {
            return this._incrementalWeight;
        },
        set: function (value) {
            this._incrementalWeight = value;
        },
        enumerable: true,
        configurable: true
    });
    AbstractWeightedScalingPolicy.prototype.formatObservationTime = function () {
        return app_utils_1.SnampUtils.toHumanizedDuration(this.observationTime);
    };
    AbstractWeightedScalingPolicy.prototype.getPolicyWeight = function () {
        return util_1.isNullOrUndefined(this.voteWeight) ? "N/A" : this.voteWeight.toString();
    };
    return AbstractWeightedScalingPolicy;
}(abstract_policy_1.AbstractPolicy));
exports.AbstractWeightedScalingPolicy = AbstractWeightedScalingPolicy;


/***/ },

/***/ "./src/app/watchers/model/policy/attribute.based.scaling.policy.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var abstract_weighted_scaling_policy_1 = __webpack_require__("./src/app/watchers/model/policy/abstract.weighted.scaling.policy.ts");
var operational_range_1 = __webpack_require__("./src/app/watchers/model/policy/operational.range.ts");
var app_utils_1 = __webpack_require__("./src/app/services/app.utils.ts");
var AttributeBasedScalingPolicy = (function (_super) {
    __extends(AttributeBasedScalingPolicy, _super);
    function AttributeBasedScalingPolicy() {
        _super.call(this);
        this.attributeName = "";
        this.operationalRange = new operational_range_1.OpRange(0, 0);
        this.analysisDepth = 0;
        this.aggregation = "MAX";
    }
    Object.defineProperty(AttributeBasedScalingPolicy.prototype, "attributeName", {
        get: function () {
            return this._attributeName;
        },
        set: function (value) {
            this._attributeName = value;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(AttributeBasedScalingPolicy.prototype, "operationalRange", {
        get: function () {
            return this._operationalRange;
        },
        set: function (value) {
            this._operationalRange = value;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(AttributeBasedScalingPolicy.prototype, "aggregation", {
        get: function () {
            return this._aggregation;
        },
        set: function (value) {
            this._aggregation = value;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(AttributeBasedScalingPolicy.prototype, "analysisDepth", {
        get: function () {
            return this._analysisDepth;
        },
        set: function (value) {
            this._analysisDepth = value;
        },
        enumerable: true,
        configurable: true
    });
    AttributeBasedScalingPolicy.prototype.formatAnalysisDepth = function () {
        return app_utils_1.SnampUtils.toHumanizedDuration(this.analysisDepth);
    };
    AttributeBasedScalingPolicy.prototype.toJSON = function () {
        var _value = {};
        _value["voteWeight"] = this.voteWeight;
        _value["incrementalWeight"] = this.incrementalWeight;
        _value["observationTime"] = app_utils_1.SnampUtils.toDurationString(this.observationTime);
        _value["attributeName"] = this.attributeName;
        _value["operationalRange"] = this.operationalRange.toString();
        _value["analysisDepth"] = app_utils_1.SnampUtils.toDurationString(this.analysisDepth);
        _value["aggregation"] = this.aggregation;
        return _value;
    };
    AttributeBasedScalingPolicy.prototype.getPoliticType = function () {
        return "Attribute based scaling policy";
    };
    return AttributeBasedScalingPolicy;
}(abstract_weighted_scaling_policy_1.AbstractWeightedScalingPolicy));
exports.AttributeBasedScalingPolicy = AttributeBasedScalingPolicy;


/***/ },

/***/ "./src/app/watchers/model/policy/health.status.based.scaling.policy.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var abstract_weighted_scaling_policy_1 = __webpack_require__("./src/app/watchers/model/policy/abstract.weighted.scaling.policy.ts");
var app_utils_1 = __webpack_require__("./src/app/services/app.utils.ts");
var HealthStatusBasedScalingPolicy = (function (_super) {
    __extends(HealthStatusBasedScalingPolicy, _super);
    function HealthStatusBasedScalingPolicy() {
        _super.call(this);
        this.level = "CRITICAL";
    }
    Object.defineProperty(HealthStatusBasedScalingPolicy.prototype, "level", {
        get: function () {
            return this._level;
        },
        set: function (value) {
            this._level = value;
        },
        enumerable: true,
        configurable: true
    });
    HealthStatusBasedScalingPolicy.prototype.toJSON = function () {
        var _value = {};
        _value["voteWeight"] = this.voteWeight;
        _value["incrementalWeight"] = this.incrementalWeight;
        _value["observationTime"] = app_utils_1.SnampUtils.toDurationString(this.observationTime);
        _value["level"] = this.level;
        return _value;
    };
    HealthStatusBasedScalingPolicy.prototype.getPoliticType = function () {
        return "Health status based scaling policy";
    };
    return HealthStatusBasedScalingPolicy;
}(abstract_weighted_scaling_policy_1.AbstractWeightedScalingPolicy));
exports.HealthStatusBasedScalingPolicy = HealthStatusBasedScalingPolicy;


/***/ },

/***/ "./src/app/watchers/model/policy/operational.range.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var util_1 = __webpack_require__("./node_modules/util/util.js");
var OpRange = (function () {
    function OpRange(begin, end) {
        this._begin = 0.0;
        this._end = 1.0;
        this.isBeginInfinite = false;
        this.isEndInfinite = false;
        this.isBeginIncluding = true;
        this.isEndIncluding = true;
        if (!util_1.isNullOrUndefined(begin)) {
            this.begin = begin;
        }
        if (!util_1.isNullOrUndefined(end)) {
            this.end = end;
        }
    }
    Object.defineProperty(OpRange.prototype, "begin", {
        get: function () {
            return this._begin;
        },
        set: function (value) {
            this._begin = value;
        },
        enumerable: true,
        configurable: true
    });
    Object.defineProperty(OpRange.prototype, "end", {
        get: function () {
            return this._end;
        },
        set: function (value) {
            this._end = value;
        },
        enumerable: true,
        configurable: true
    });
    OpRange.prototype.getLeftBracket = function () {
        return (this.isBeginIncluding && !this.isBeginInfinite) ? "[" : "(";
    };
    OpRange.prototype.getRightBracket = function () {
        return (this.isEndIncluding && !this.isEndInfinite) ? "]" : ")";
    };
    OpRange.prototype.getBeginString = function () {
        if (this.isBeginInfinite || util_1.isNullOrUndefined(this.begin)) {
            return OpRange.MINUS_INFINITE;
        }
        else {
            return this.begin.toString();
        }
    };
    OpRange.prototype.getEndString = function () {
        if (this.isEndInfinite || util_1.isNullOrUndefined(this.end)) {
            return OpRange.PLUS_INFINITE;
        }
        else {
            return this.end.toString();
        }
    };
    OpRange.prototype.toString = function () {
        return this.getLeftBracket() + this.getBeginString() + OpRange.DELIMITER + this.getEndString() + this.getRightBracket();
    };
    OpRange.fromString = function (str) {
        console.debug("String to parse operational: ", str);
        var splits = str.split(OpRange.DELIMITER);
        var _result = new OpRange(0.0, 0.0);
        // begining parsing
        if (splits[0].substr(0, 1) == "[") {
            _result.isBeginInfinite = false;
            _result.isBeginIncluding = true;
        }
        else {
            _result.isBeginIncluding = false;
        }
        var beginStr = splits[0].substr(1);
        if (beginStr == OpRange.MINUS_INFINITE) {
            _result.isBeginInfinite = true;
        }
        else {
            _result.begin = Number.parseFloat(beginStr);
        }
        // ending parse
        if (splits[1].substr(splits[1].length - 1, 1) == "]") {
            _result.isEndInfinite = false;
            _result.isEndIncluding = true;
        }
        else {
            _result.isEndIncluding = false;
        }
        var endStr = splits[1].substr(0, splits[1].length - 1);
        if (endStr == OpRange.PLUS_INFINITE) {
            _result.isEndInfinite = true;
        }
        else {
            _result.end = Number.parseFloat(endStr);
        }
        return _result;
    };
    OpRange.DELIMITER = "‥";
    OpRange.MINUS_INFINITE = "-∞";
    OpRange.PLUS_INFINITE = "+∞";
    return OpRange;
}());
exports.OpRange = OpRange;


/***/ },

/***/ "./src/app/watchers/model/range.comparator.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var colored_predicate_1 = __webpack_require__("./src/app/watchers/model/colored.predicate.ts");
var IsInRangePredicate = (function (_super) {
    __extends(IsInRangePredicate, _super);
    function IsInRangePredicate() {
        _super.apply(this, arguments);
        this.type = colored_predicate_1.ColoredAttributePredicate.RANGE;
    }
    IsInRangePredicate.prototype.toJSON = function () {
        var _value = {};
        _value["@type"] = this.type;
        _value["rangeStart"] = this.rangeStart;
        _value["rangeEnd"] = this.rangeEnd;
        _value["isRangeStartInclusive"] = this.rangeStart;
        _value["isRangeEndInclusive"] = this.rangeEnd;
        return _value;
    };
    IsInRangePredicate.prototype.represent = function () {
        var _value = "";
        _value += this.rangeEnd + " " + (this.isRangeEndInclusive ? "≥" : ">")
            + " value " + (this.isRangeStartInclusive ? "≥" : ">") + " " + this.rangeStart;
        return _value;
    };
    return IsInRangePredicate;
}(colored_predicate_1.ColoredAttributePredicate));
exports.IsInRangePredicate = IsInRangePredicate;


/***/ },

/***/ "./src/app/watchers/model/scriptlet.data.object.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var colored_predicate_1 = __webpack_require__("./src/app/watchers/model/colored.predicate.ts");
var constant_attribute_predicate_1 = __webpack_require__("./src/app/watchers/model/constant.attribute.predicate.ts");
var number_comparator_predicate_1 = __webpack_require__("./src/app/watchers/model/number.comparator.predicate.ts");
var range_comparator_1 = __webpack_require__("./src/app/watchers/model/range.comparator.ts");
var entity_1 = __webpack_require__("./src/app/watchers/model/entity.ts");
var colored_checker_1 = __webpack_require__("./src/app/watchers/model/colored.checker.ts");
var model_entity_1 = __webpack_require__("./src/app/configuration/model/model.entity.ts");
var health_status_based_scaling_policy_1 = __webpack_require__("./src/app/watchers/model/policy/health.status.based.scaling.policy.ts");
var attribute_based_scaling_policy_1 = __webpack_require__("./src/app/watchers/model/policy/attribute.based.scaling.policy.ts");
var operational_range_1 = __webpack_require__("./src/app/watchers/model/policy/operational.range.ts");
var app_utils_1 = __webpack_require__("./src/app/services/app.utils.ts");
var ScriptletDataObject = (function (_super) {
    __extends(ScriptletDataObject, _super);
    function ScriptletDataObject(params) {
        _super.call(this, "", params);
        this.id = entity_1.Guid.newGuid();
        this.language = "Groovy";
        this.script = "";
        this.isURL = false;
        this.object = undefined;
        this.policyObject = undefined;
    }
    ScriptletDataObject.prototype.shortScript = function () {
        return ((this.script.length > 60) ? this.script.substring(0, 60) + '...' : this.script);
    };
    // add "MetricBased"(AttributeBasedScalingPolicy) and
    ScriptletDataObject.fromJSON = function (json) {
        console.debug("Json from data object is: ", json);
        var instance = new ScriptletDataObject(json["parameters"]);
        if (json["language"] != undefined) {
            instance.language = json["language"];
        }
        if (json["script"] != undefined) {
            instance.script = json["script"];
        }
        if (json["isURL"] != undefined) {
            instance.isURL = (json["url"] == 'true');
        }
        switch (instance.language) {
            case "Groovy":
            case "JavaScript":
                instance.object = undefined;
                break;
            case "HealthStatusBased":
                var _jsonHSB = JSON.parse(instance.script);
                instance.policyObject = new health_status_based_scaling_policy_1.HealthStatusBasedScalingPolicy();
                instance.policyObject.level = _jsonHSB["level"];
                instance.policyObject.observationTime = app_utils_1.SnampUtils.parseDuration(_jsonHSB["observationTime"]);
                instance.policyObject.incrementalWeight = _jsonHSB["incrementalWeight"];
                instance.policyObject.voteWeight = _jsonHSB["voteWeight"];
                break;
            case "MetricBased":
                var _jsonMB = JSON.parse(instance.script);
                instance.policyObject = new attribute_based_scaling_policy_1.AttributeBasedScalingPolicy();
                instance.policyObject.analysisDepth = app_utils_1.SnampUtils.parseDuration(_jsonMB["observationTime"]);
                instance.policyObject.incrementalWeight = _jsonMB["incrementalWeight"];
                instance.policyObject.voteWeight = _jsonMB["voteWeight"];
                instance.policyObject.aggregation = _jsonMB["aggregation"];
                instance.policyObject.attributeName = _jsonMB["attributeName"];
                instance.policyObject.operationalRange = operational_range_1.OpRange.fromString(_jsonMB["operationalRange"]);
                instance.policyObject.analysisDepth = app_utils_1.SnampUtils.parseDuration(_jsonMB["analysisDepth"]);
                break;
            case "ColoredAttributeChecker":
                instance.object = new colored_checker_1.ColoredAttributeChecker();
                if (instance.script == undefined || instance.script.length < 5) {
                    return instance;
                }
                var _jsonChecker = JSON.parse(instance.script);
                var _yellow = _jsonChecker["yellow"];
                switch (_yellow["@type"]) {
                    case colored_predicate_1.ColoredAttributePredicate.CONSTANT:
                        instance.object.yellow = new constant_attribute_predicate_1.ConstantAttributePredicate();
                        instance.object.yellow.value = _yellow["value"];
                        break;
                    case colored_predicate_1.ColoredAttributePredicate.COMPARATOR:
                        instance.object.yellow = new number_comparator_predicate_1.NumberComparatorPredicate();
                        instance.object.yellow.value = _yellow["value"];
                        instance.object.yellow.operator = _yellow["operator"];
                        break;
                    case colored_predicate_1.ColoredAttributePredicate.RANGE:
                        instance.object.yellow = new range_comparator_1.IsInRangePredicate();
                        instance.object.yellow.rangeStart = _yellow["rangeStart"];
                        instance.object.yellow.rangeEnd = _yellow["rangeEnd"];
                        instance.object.yellow.isRangeEndInclusive = _yellow["isRangeEndInclusive"];
                        instance.object.yellow.isRangeStartInclusive = _yellow["isRangeStartInclusive"];
                        break;
                    default:
                        throw new Error("Could not recognize yellow checker type: " + _yellow["@type"]);
                }
                var _green = _jsonChecker["green"];
                switch (_green["@type"]) {
                    case colored_predicate_1.ColoredAttributePredicate.CONSTANT:
                        instance.object.green = new constant_attribute_predicate_1.ConstantAttributePredicate();
                        instance.object.green.value = _green["value"];
                        break;
                    case colored_predicate_1.ColoredAttributePredicate.COMPARATOR:
                        instance.object.green = new number_comparator_predicate_1.NumberComparatorPredicate();
                        instance.object.green.value = _green["value"];
                        instance.object.green.operator = _green["operator"];
                        break;
                    case colored_predicate_1.ColoredAttributePredicate.RANGE:
                        instance.object.green = new range_comparator_1.IsInRangePredicate();
                        instance.object.green.rangeStart = _green["rangeStart"];
                        instance.object.green.rangeEnd = _green["rangeEnd"];
                        instance.object.green.isRangeEndInclusive = _green["isRangeEndInclusive"];
                        instance.object.green.isRangeStartInclusive = _green["isRangeStartInclusive"];
                        break;
                    default:
                        throw new Error("Could not recognize green checker type: " + _green["@type"]);
                }
                break;
            default:
                throw new Error("Cannot recognize language type: " + instance.language);
        }
        return instance;
    };
    ScriptletDataObject.prototype.toJSON = function () {
        var _value = {};
        _value["language"] = this.language;
        _value["script"] = this.script;
        _value["url"] = this.isURL;
        _value["parameters"] = this.stringifyParameters();
        if (this.language == "ColoredAttributeChecker") {
            if (this.object == undefined) {
                throw new Error("Trying to serialize ColoredAttributeChecker instance without the object");
            }
            else {
                this.script = JSON.stringify(this.object.toJSON());
            }
        }
        else if (this.language == "HealthStatusBased" || this.language == "MetricBased") {
            if (this.policyObject == undefined) {
                throw new Error("Trying to serialize " + this.language + " instance without the policyObject");
            }
            else {
                this.script = JSON.stringify(this.policyObject.toJSON());
            }
        }
        console.debug("Trying to stringify current scriptlet object: ", _value);
        return _value;
    };
    ScriptletDataObject.prototype.getPolicyType = function () {
        return this.policyObject == undefined ? "Groovy policy" : this.policyObject.getPoliticType();
    };
    ScriptletDataObject.prototype.getPolicyWeight = function () {
        return this.policyObject == undefined ? "N/A" : this.policyObject.getPolicyWeight();
    };
    return ScriptletDataObject;
}(model_entity_1.Entity));
exports.ScriptletDataObject = ScriptletDataObject;


/***/ },

/***/ "./src/app/watchers/model/watcher.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var entity_1 = __webpack_require__("./src/app/watchers/model/entity.ts");
var scriptlet_data_object_1 = __webpack_require__("./src/app/watchers/model/scriptlet.data.object.ts");
var util_1 = __webpack_require__("./node_modules/util/util.js");
var app_utils_1 = __webpack_require__("./src/app/services/app.utils.ts");
var Watcher = (function (_super) {
    __extends(Watcher, _super);
    function Watcher() {
        _super.apply(this, arguments);
        this.attributeCheckers = {};
        this.trigger = new scriptlet_data_object_1.ScriptletDataObject({});
        this.scalingPolicies = {};
        this.connectionStringTemplate = "";
        this.scalingSize = 0;
        this.maxClusterSize = 10;
        this.minClusterSize = 0;
        this.cooldownTime = 0;
        this.type = "default";
        this.autoScaling = false;
        this.votingStrategy = "all";
    }
    Watcher.prototype.toJSON = function () {
        // console.debug("JSONify the watcher from the watcher class: ", this);
        var _value = {};
        _value["attributeCheckers"] = {};
        for (var key in this.attributeCheckers) {
            _value["attributeCheckers"][key] = this.attributeCheckers[key].toJSON();
        }
        _value["scalingPolicies"] = {};
        for (var key in this.scalingPolicies) {
            _value["scalingPolicies"][key] = this.scalingPolicies[key].toJSON();
        }
        _value["trigger"] = this.trigger.toJSON();
        _value["parameters"] = this.stringifyParameters();
        _value["parameters"]["$strategy$"] = this.votingStrategy;
        _value["connectionStringTemplate"] = this.connectionStringTemplate;
        _value["autoScaling"] = this.autoScaling;
        if (this.autoScaling) {
            _value["cooldownTime"] = app_utils_1.SnampUtils.toDurationString(this.cooldownTime);
            _value["scalingSize"] = this.scalingSize;
            _value["minClusterSize"] = this.minClusterSize;
            _value["maxClusterSize"] = this.maxClusterSize;
        }
        _value["type"] = this.type;
        return _value;
    };
    Watcher.prototype.checkerExists = function (attributeName) {
        return this.attributeCheckers[attributeName] != undefined && this.attributeCheckers[attributeName].script.length > 0;
    };
    Watcher.prototype.checkerTypeForAttributeName = function (attributeName) {
        return this.checkerExists(attributeName) ? this.attributeCheckers[attributeName].language : "n/a";
    };
    Watcher.prototype.recalculateVotes = function () {
        var _voteWeight = 0;
        var _count = this.getPoliciesCount();
        switch (this.votingStrategy) {
            case "all":
                _voteWeight = 0.5 + Number.EPSILON;
                break;
            case "any":
                _voteWeight = (_count / 2) + Number.EPSILON;
                break;
            case "most":
                _voteWeight = 1;
                break;
            default:
                console.debug("Do nothing - custom type is used");
                return;
        }
        for (var key in this.scalingPolicies) {
            if (this.scalingPolicies[key].language != "Groovy" && !util_1.isNullOrUndefined(this.scalingPolicies[key].policyObject)) {
                this.scalingPolicies[key].policyObject.voteWeight = _voteWeight;
            }
        }
    };
    Watcher.prototype.getCheckersCount = function () {
        return Object.keys(this.attributeCheckers).length;
    };
    Watcher.prototype.getPoliciesCount = function () {
        return Object.keys(this.scalingPolicies).length;
    };
    return Watcher;
}(entity_1.Entity));
exports.Watcher = Watcher;


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

/***/ "./src/app/watchers/templates/css/statuses.css":
/***/ function(module, exports, __webpack_require__) {


        var result = __webpack_require__("./node_modules/css-loader/index.js!./src/app/watchers/templates/css/statuses.css");

        if (typeof result === "string") {
            module.exports = result;
        } else {
            module.exports = result.toString();
        }
    

/***/ },

/***/ "./src/app/watchers/templates/main.html":
/***/ function(module, exports) {

module.exports = "<div class=\"right_col\" role=\"main\" style=\"min-height: 949px;\">\r\n    <div class=\"page-title\">\r\n      <div class=\"title_left\">\r\n        <h3>{{getMainHeader()}}</h3>\r\n      </div>\r\n    </div>\r\n\r\n    <div class=\"clearfix\"></div>\r\n\r\n    <hr/>\r\n\r\n\r\n    <!-- Modal for edit the trigger for activeWatcher -->\r\n    <div class=\"modal fade\" id=\"editTriggerModal\" role=\"dialog\" aria-labelledby=\"editTriggerLabel\"  *ngIf=\"activeWatcher != undefined\">\r\n      <div class=\"modal-dialog modal-xlg modal-lg\" role=\"document\">\r\n        <div class=\"modal-content\">\r\n          <div class=\"modal-header\">\r\n            <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>\r\n            <h4 class=\"modal-title leftAlign\" id=\"editTriggerLabel\">Edit trigger</h4>\r\n            <br/>\r\n            <div class=\"modal-body\">\r\n              <div id=\"smartwizardForTrigger\" style=\"height: 95%\">\r\n                <ul>\r\n                  <li>\r\n                    <a [attr.href]=\"'#language'\">\r\n                      Language<br />\r\n                      <small>Select language for trigger</small>\r\n                    </a>\r\n                  </li>\r\n                  <li>\r\n                    <a [attr.href]=\"'#url'\">\r\n                      Use url<br />\r\n                      <small>Use URL for loading script</small>\r\n                    </a>\r\n                  </li>\r\n                  <li>\r\n                    <a [attr.href]=\"'#ptable'\">\r\n                      Parameters<br />\r\n                      <small>Append parameters</small>\r\n                    </a>\r\n                  </li>\r\n                  <li>\r\n                    <a [attr.href]=\"'#script'\">\r\n                      Script<br />\r\n                      <small>Trigger's script</small>\r\n                    </a>\r\n                  </li>\r\n                </ul>\r\n\r\n                <div style=\"height:100%\">\r\n                  <div id=\"language\" class=\"row\" style=\"margin-top: 100px;\">\r\n                    <div class=\"item form-group\">\r\n                      <label\r\n                              class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                              for=\"languageSelect\"\r\n                              style=\"margin-top: 7px;\">\r\n                        Language <span class=\"required\">*</span>\r\n                      </label>\r\n                      <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                        <select class=\"form-control\" [(ngModel)]=\"activeWatcher.trigger.language\" id=\"languageSelect\">\r\n                          <option *ngFor=\"let language of triggerLanguages\" [ngValue]=\"language\">{{language}}</option>\r\n                        </select>\r\n                      </div>\r\n                    </div>\r\n                  </div>\r\n\r\n                  <div id=\"url\" class=\"row\" style=\"margin-top: 100px;\">\r\n                    <div class=\"item form-group\">\r\n                      <label\r\n                              class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                              style=\"margin-top: 7px;\">\r\n                        Load script from external URL <span class=\"required\">*</span>\r\n                      </label>\r\n\r\n                      <div class=\"col-md-6 col-sm-6 col-xs-12\" >\r\n                        <ui-switch\r\n                                [(ngModel)]=\"activeWatcher.trigger.isURL\"\r\n                                [size]=\"'small'\">\r\n                        </ui-switch>\r\n                      </div>\r\n                    </div>\r\n                  </div>\r\n\r\n                  <div id=\"ptable\" class=\"row\" style=\"margin-top: 30px;\">\r\n                    <div>\r\n                      <ptable [entity]=\"activeWatcher.trigger\"></ptable>\r\n                    </div>\r\n                  </div>\r\n\r\n                  <div id=\"script\" class=\"row\" style=\"margin-top: 20px;\">\r\n                    <div *ngIf=\"activeWatcher.trigger.isURL\">\r\n                      <div class=\"item form-group\">\r\n                        <label\r\n                                class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                for=\"urlForTrigger\"\r\n                                style=\"margin-top: 7px;\">\r\n                          Url for script loading <span class=\"required\">*</span>\r\n                        </label>\r\n                        <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                          <input type=\"text\" class=\"form-control\" id=\"urlForTrigger\" placeholder=\"Input url for script to be loaded from\" [(ngModel)]=\"activeWatcher.trigger.script\"/>\r\n                        </div>\r\n                      </div>\r\n                    </div>\r\n                    <div *ngIf=\"!activeWatcher.trigger.isURL\">\r\n                      <div>\r\n                        <div class=\"row\">\r\n                          <label\r\n                                  class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                  for=\"scriptBodyForTrigger\"\r\n                                  style=\"margin-top: 7px;\">\r\n                            Script body for the trigger <span class=\"required\">*</span>\r\n                          </label>\r\n                        </div>\r\n                        <div class=\"row\">\r\n                          <div class=\"col-md-12 col-sm-12 col-xs-12\">\r\n                            <textarea id=\"scriptBodyForTrigger\" rows=\"10\" class=\"form-control\" [(ngModel)]=\"activeWatcher.trigger.script\"></textarea>\r\n                          </div>\r\n                        </div>\r\n                      </div>\r\n                    </div>\r\n                    <div class=\"col-md-2\" style=\"float:right; margin-top: 30px;\">\r\n                      <button class=\"btn btn-primary\" data-dismiss=\"modal\" (click)=\"saveCurrentTrigger()\">Save trigger</button>\r\n                    </div>\r\n                  </div>\r\n                </div>\r\n              </div>\r\n            </div>\r\n          </div>\r\n        </div>\r\n      </div>\r\n    </div>\r\n\r\n    <!-- Modal for edit checkers for activeWatcher -->\r\n    <div class=\"modal fade\" id=\"editCheckerModal\" role=\"dialog\" aria-labelledby=\"editCheckerLabel\" *ngIf=\"activeWatcher != undefined\">\r\n      <div class=\"modal-dialog modal-xlg modal-lg\" role=\"document\">\r\n        <div class=\"modal-content\">\r\n          <div class=\"modal-header\">\r\n            <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>\r\n            <h4 class=\"modal-title leftAlign\" id=\"editCheckerLabel\">Edit attribute checkers</h4>\r\n            <br/>\r\n            <div class=\"modal-body\">\r\n              <div id=\"smartwizardForCheckers\" style=\"height: 95%\">\r\n                <ul>\r\n                  <li>\r\n                    <a [attr.href]=\"'#checkerTable'\">\r\n                      Select checker<br />\r\n                      <small>Select type of checker</small>\r\n                    </a>\r\n                  </li>\r\n                  <li>\r\n                    <a [attr.href]=\"'#checkerType'\">\r\n                      Type<br />\r\n                      <small>Select type of checker</small>\r\n                    </a>\r\n                  </li>\r\n                  <li>\r\n                    <a [attr.href]=\"'#checkerPTable'\">\r\n                      Parameters<br />\r\n                      <small>Set parameters</small>\r\n                    </a>\r\n                  </li>\r\n                  <li>\r\n                    <a [attr.href]=\"'#checkerBody'\">\r\n                      Checker's logic<br />\r\n                      <small>What action will be performed</small>\r\n                    </a>\r\n                  </li>\r\n                </ul>\r\n\r\n                <div style=\"height:100%\">\r\n\r\n                  <div id=\"checkerTable\" class=\"row\" style=\"margin-top: 5px;\">\r\n                    <table class=\"table table-bordered\">\r\n                      <thead class=\"thead-inverse\">\r\n                      <tr>\r\n                        <th>Actions</th>\r\n                        <th>Attribute</th>\r\n                        <th>Binding</th>\r\n                        <th>Checker type</th>\r\n                      </tr>\r\n                      </thead>\r\n                      <tbody>\r\n                      <tr *ngFor=\"let attribute of attributes\" class=\"clickableTr\" [class.activeTr]=\"((selectedAttribute != undefined) && (selectedAttribute.name == attribute.name))\" (click)=\"editCheckerForAttribute(attribute)\">\r\n                        <td>\r\n                                  <span\r\n                                          class=\"glyphicon glyphicon-remove-circle btn btn-xs btn-danger\"\r\n                                          (click)=\"removeCheckerForAttribute(attribute)\"\r\n                                          aria-hidden=\"true\">\r\n                                  </span>\r\n                        </td>\r\n                        <th scope=\"row\">{{attribute.name}}</th>\r\n                        <td>\r\n                                <span class=\"input-group-addon\"  *ngIf=\"activeWatcher.checkerExists(attribute.name)\">\r\n                                      <span class=\"glyphicon glyphicon-ok glyph-icon-appended\" aria-hidden=\"true\"></span>\r\n                                 </span>\r\n                        </td>\r\n                        <td>\r\n                          {{activeWatcher.checkerTypeForAttributeName(attribute.name)}}\r\n                        </td>\r\n                      </tr>\r\n                      </tbody>\r\n                    </table>\r\n                  </div>\r\n\r\n                  <div id=\"checkerPTable\" class=\"row\" style=\"margin-top: 30px;\">\r\n                    <div>\r\n                      <ptable [entity]=\"activeChecker\"></ptable>\r\n                    </div>\r\n                  </div>\r\n\r\n                  <div id=\"checkerType\" class=\"row\" style=\"margin-top: 100px;\">\r\n                    <div class=\"item form-group\">\r\n                      <label\r\n                              class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                              for=\"checkerTypeSelect\"\r\n                              style=\"margin-top: 7px;\">\r\n                        Set type for attribute checker <span class=\"required\">*</span>\r\n                      </label>\r\n                      <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                        <select class=\"form-control\" [(ngModel)]=\"activeChecker.language\" id=\"checkerTypeSelect\" (ngModelChange)=\"selectCheckerType($event)\">\r\n                          <option *ngFor=\"let type of checkersType\" [ngValue]=\"type.id\">{{type.description}}</option>\r\n                        </select>\r\n                      </div>\r\n                    </div>\r\n                  </div>\r\n\r\n\r\n                  <div id=\"checkerBody\" class=\"row\" style=\"margin-top: 5px;\">\r\n                    <div *ngIf=\"activeChecker?.language != 'ColoredAttributeChecker'\" style=\"margin-top: 20px;\">\r\n\r\n                      <div class=\"item form-group\" style=\"margin-bottom: 15px;\">\r\n                        <label\r\n                                class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                style=\"margin-top: 7px;\">\r\n                          Load script from external URL <span class=\"required\">*</span>\r\n                        </label>\r\n\r\n                        <div class=\"col-md-6 col-sm-6 col-xs-12\" >\r\n                          <ui-switch\r\n                                  [(ngModel)]=\"activeChecker.isURL\"\r\n                                  [size]=\"'small'\">\r\n                          </ui-switch>\r\n                        </div>\r\n                      </div>\r\n\r\n                      <div *ngIf=\"activeChecker?.isURL\">\r\n                        <div class=\"item form-group\">\r\n                          <label\r\n                                  class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                  for=\"urlForChecker\"\r\n                                  style=\"margin-top: 7px;\">\r\n                            Url for script loading <span class=\"required\">*</span>\r\n                          </label>\r\n                          <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                            <input type=\"text\" class=\"form-control\" id=\"urlForChecker\" placeholder=\"Input url for script to be loaded from\" [(ngModel)]=\"activeChecker.script\"/>\r\n                          </div>\r\n                        </div>\r\n                      </div>\r\n                      <div *ngIf=\"!activeChecker?.isURL\">\r\n                        <div class=\"item form-group\">\r\n                          <label\r\n                                  class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                  for=\"scriptBodyForChecker\"\r\n                                  style=\"margin-top: 7px;\">\r\n                            Script body for the checker <span class=\"required\">*</span>\r\n                          </label>\r\n                          <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                            <textarea id=\"scriptBodyForChecker\" rows=\"5\" class=\"form-control\" [(ngModel)]=\"activeChecker.script\"></textarea>\r\n                          </div>\r\n                        </div>\r\n                      </div>\r\n                    </div>\r\n\r\n                    <div *ngIf=\"activeChecker?.language == 'ColoredAttributeChecker'\">\r\n                      <div class=\"panel-group group-accordeon\" id=\"accordionChecker\" role=\"tablist\" aria-multiselectable=\"true\">\r\n                        <div class=\"panel panel-default\" >\r\n                          <div class=\"panel-heading\" role=\"tab\" id=\"headingOne\">\r\n                            <h4 class=\"panel-title\">\r\n                              <a role=\"button\" data-toggle=\"collapse\" data-parent=\"#accordionChecker\" href=\"#collapseGreen\" aria-expanded=\"true\" aria-controls=\"collapseGreen\">\r\n                                <img src=\"assets/img/green-circle.png\" class=\"img-inline\"> Green condition\r\n                              </a>\r\n                            </h4>\r\n                          </div>\r\n                          <div id=\"collapseGreen\" class=\"panel-collapse collapse in\" role=\"tabpanel\" aria-labelledby=\"headingOne\">\r\n                            <div class=\"panel-body\">\r\n                              <coloredCondition [entity]=\"activeChecker.object.green\" (notify)=\"onGreenNotify($event)\"></coloredCondition>\r\n                            </div>\r\n                          </div>\r\n                        </div>\r\n                        <div class=\"panel panel-default\">\r\n                          <div class=\"panel-heading\" role=\"tab\" id=\"headingTwo\">\r\n                            <h4 class=\"panel-title\">\r\n                              <a class=\"collapsed\" role=\"button\" data-toggle=\"collapse\" data-parent=\"#accordionChecker\" href=\"#collapseYellow\" aria-expanded=\"false\" aria-controls=\"collapseYellow\">\r\n                                <img src=\"assets/img/yellow-circle.png\" class=\"img-inline\"> Yellow condition\r\n                              </a>\r\n                            </h4>\r\n                          </div>\r\n                          <div id=\"collapseYellow\" class=\"panel-collapse collapse\" role=\"tabpanel\" aria-labelledby=\"headingTwo\">\r\n                            <div class=\"panel-body\">\r\n                              <coloredCondition [entity]=\"activeChecker.object.yellow\" (notify)=\"onYellowNotify($event)\"></coloredCondition>\r\n                            </div>\r\n                          </div>\r\n                        </div>\r\n                      </div>\r\n                    </div>\r\n\r\n                    <div class=\"col-md-2\" style=\"float:right;\">\r\n                      <button class=\"btn btn-primary\" data-dismiss=\"modal\" (click)=\"saveCurrentChecker()\">Save checker</button>\r\n                    </div>\r\n                  </div>\r\n                </div>\r\n              </div>\r\n            </div>\r\n          </div>\r\n        </div>\r\n      </div>\r\n    </div>\r\n\r\n    <!-- Modal for edit policies for activeWatcher -->\r\n    <div class=\"modal fade\" id=\"editPolicyModal\" role=\"dialog\" aria-labelledby=\"editPolicyLabel\" *ngIf=\"activeWatcher != undefined\">\r\n      <div class=\"modal-dialog modal-xlg modal-lg\" role=\"document\">\r\n        <div class=\"modal-content\">\r\n          <div class=\"modal-header\">\r\n            <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>\r\n            <h4 class=\"modal-title leftAlign\" id=\"editPolicyLabel\">Edit scaling policies</h4>\r\n            <br/>\r\n            <div class=\"modal-body\">\r\n              <div id=\"smartwizardForPolicies\" style=\"height: 95%\">\r\n                <ul>\r\n                  <li>\r\n                    <a [attr.href]=\"'#votingStrategy'\">\r\n                      Voting strategy<br />\r\n                      <small>Select voting strategy</small>\r\n                    </a>\r\n                  </li>\r\n                  <li>\r\n                    <a [attr.href]=\"'#policyTable'\">\r\n                      Policy selection<br />\r\n                      <small>Select or add a policy</small>\r\n                    </a>\r\n                  </li>\r\n                  <li>\r\n                    <a [attr.href]=\"'#policyType'\">\r\n                      Type<br />\r\n                      <small>Select type of policy</small>\r\n                    </a>\r\n                  </li>\r\n                  <li>\r\n                    <a [attr.href]=\"'#policyPTable'\">\r\n                      Parameters<br />\r\n                      <small>Set parameters</small>\r\n                    </a>\r\n                  </li>\r\n                  <li>\r\n                    <a [attr.href]=\"'#policyBody'\">\r\n                      Policy's logic<br />\r\n                      <small>How the policy works</small>\r\n                    </a>\r\n                  </li>\r\n                </ul>\r\n\r\n                <div style=\"height:100%\">\r\n\r\n                  <div id=\"votingStrategy\" class=\"row\" style=\"margin-top: 100px;\">\r\n                    <div class=\"item form-group\">\r\n                      <label\r\n                              class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                              for=\"strategySelect\"\r\n                              style=\"margin-top: 7px;\">\r\n                        Set strategy <span class=\"required\">*</span>\r\n                      </label>\r\n                      <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                        <select class=\"form-control\" [(ngModel)]=\"activeWatcher.votingStrategy\" id=\"strategySelect\" (ngModelChange)=\"selectVotingStrategy($event)\">\r\n                          <option *ngFor=\"let type of strategyTypes\" [ngValue]=\"type.id\">{{type.description}}</option>\r\n                        </select>\r\n                      </div>\r\n                    </div>\r\n                  </div>\r\n\r\n                  <div id=\"policyTable\" class=\"row\" style=\"margin-top: 5px;\">\r\n                    <table class=\"table table-bordered\">\r\n                      <thead class=\"thead-inverse\">\r\n                      <tr>\r\n                        <th>Actions</th>\r\n                        <th>Name</th>\r\n                        <th>Type</th>\r\n                        <th *ngIf=\"activeWatcher.votingStrategy == 'custom'\">Weight</th>\r\n                      </tr>\r\n                      </thead>\r\n                      <tbody>\r\n                      <tr *ngFor=\"let entry of activeWatcher.scalingPolicies | keys\" class=\"clickableTr\" [class.activeTr]=\"isPolicyActive(entry.key)\" (click)=\"editPolicy(entry.key, entry.value)\">\r\n                        <td>\r\n                                  <span\r\n                                          class=\"glyphicon glyphicon-remove-circle btn btn-xs btn-danger\"\r\n                                          (click)=\"removePolicy(entry.key)\"\r\n                                          aria-hidden=\"true\">\r\n                                  </span>\r\n                        </td>\r\n                        <th scope=\"row\">{{entry.key}}</th>\r\n                        <td>\r\n                          {{entry.value.getPolicyType()}}\r\n                        </td>\r\n                        <td *ngIf=\"activeWatcher.votingStrategy == 'custom'\">\r\n                          {{entry.value.getPolicyWeight()}}\r\n                        </td>\r\n                      </tr>\r\n                      </tbody>\r\n                    </table>\r\n                    <button class=\"btn btn-primary btn-xs\" (click)=\"addNewPolicy()\" *ngIf=\"!newPolicyAppended\">\r\n                      Add new policy\r\n                    </button>\r\n                  </div>\r\n\r\n                  <div id=\"policyType\" class=\"row\" style=\"margin-top: 100px;\">\r\n                    <div class=\"item form-group\">\r\n                      <label\r\n                              class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                              for=\"policyTypeSelect\"\r\n                              style=\"margin-top: 7px;\">\r\n                        Set type for policy <span class=\"required\">*</span>\r\n                      </label>\r\n                      <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                        <select class=\"form-control\" [(ngModel)]=\"activePolicy.language\" id=\"policyTypeSelect\" (ngModelChange)=\"selectPolicyType($event)\">\r\n                          <option *ngFor=\"let type of policyTypes\" [ngValue]=\"type.id\">{{type.description}}</option>\r\n                        </select>\r\n                      </div>\r\n                    </div>\r\n                  </div>\r\n\r\n                  <div id=\"policyPTable\" class=\"row\" style=\"margin-top: 30px;\">\r\n                    <div>\r\n                      <ptable [entity]=\"activePolicy\"></ptable>\r\n                    </div>\r\n                  </div>\r\n\r\n                  <div id=\"policyBody\" class=\"row\" style=\"margin-top: 5px;\">\r\n                    <div *ngIf=\"activePolicy?.language == 'Groovy'\" style=\"margin-top: 20px;\">\r\n\r\n                      <div class=\"item form-group\" style=\"margin-bottom: 15px;\">\r\n                        <label\r\n                                class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                style=\"margin-top: 7px;\">\r\n                          Load script from external URL <span class=\"required\">*</span>\r\n                        </label>\r\n\r\n                        <div class=\"col-md-6 col-sm-6 col-xs-12\" >\r\n                          <ui-switch\r\n                                  [(ngModel)]=\"activePolicy.isURL\"\r\n                                  [size]=\"'small'\">\r\n                          </ui-switch>\r\n                        </div>\r\n                      </div>\r\n\r\n                      <div *ngIf=\"activePolicy?.isURL\">\r\n                        <div class=\"item form-group\">\r\n                          <label\r\n                                  class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                  for=\"urlForPolicy\"\r\n                                  style=\"margin-top: 7px;\">\r\n                            Url for script loading <span class=\"required\">*</span>\r\n                          </label>\r\n                          <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                            <input type=\"text\" class=\"form-control\" id=\"urlForPolicy\" placeholder=\"Input url for script to be loaded from\" [(ngModel)]=\"activePolicy.script\"/>\r\n                          </div>\r\n                        </div>\r\n                      </div>\r\n                      <div *ngIf=\"!activePolicy?.isURL\">\r\n                        <div class=\"item form-group\">\r\n                          <label\r\n                                  class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                  for=\"scriptBodyForPolicy\"\r\n                                  style=\"margin-top: 7px;\">\r\n                            Script body for the policy <span class=\"required\">*</span>\r\n                          </label>\r\n                          <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                            <textarea id=\"scriptBodyForPolicy\" rows=\"5\" class=\"form-control\" [(ngModel)]=\"activePolicy.script\"></textarea>\r\n                          </div>\r\n                        </div>\r\n                      </div>\r\n                    </div>\r\n\r\n                    <div *ngIf=\"activePolicy?.language == 'HealthStatusBased'\">\r\n\r\n                      <div class=\"form-group row\">\r\n                        <label for=\"healthStatusTypeSelection\" class=\"col-md-2 col-form-label\">Level</label>\r\n                        <div class=\"col-md-10\">\r\n                          <select class=\"form-control\" [(ngModel)]=\"activePolicy.policyObject.level\" id=\"healthStatusTypeSelection\">\r\n                            <option  *ngFor=\"let level of healthStatusLevels\" [ngValue]=\"level\">{{level}}</option>\r\n                          </select>\r\n                        </div>\r\n                      </div>\r\n\r\n                      <div class=\"form-group row\" *ngIf=\"activeWatcher.votingStrategy == 'custom'\">\r\n                        <label for=\"voteWeightHSBPolicy\" class=\"col-md-2 col-form-label\">Vote weight</label>\r\n                        <div class=\"col-md-10\">\r\n                          <input class=\"form-control\" type=\"number\"  min=\"0\" [(ngModel)]=\"activePolicy.policyObject.voteWeight\" id=\"voteWeightHSBPolicy\">\r\n                        </div>\r\n                      </div>\r\n\r\n                      <div class=\"form-group row\">\r\n                        <label for=\"observationTimeHSBPolicy\" class=\"col-md-2 col-form-label\">Observation time (ms)</label>\r\n                        <div class=\"col-md-10\">\r\n                          <input class=\"form-control\" type=\"number\" min=\"0\" [(ngModel)]=\"activePolicy.policyObject.observationTime\" id=\"observationTimeHSBPolicy\">\r\n                        </div>\r\n                      </div>\r\n\r\n                      <div class=\"form-group row\">\r\n                        <label class=\"col-md-2 col-form-label\">Incremental weight</label>\r\n                        <div class=\"col-md-10\">\r\n                          <ui-switch\r\n                                  [(ngModel)]=\"activePolicy.policyObject.incrementalWeight\"\r\n                                  [size]=\"'small'\">\r\n                          </ui-switch>\r\n                        </div>\r\n                      </div>\r\n\r\n                    </div>\r\n\r\n                    <div *ngIf=\"activePolicy?.language == 'MetricBased'\">\r\n\r\n                      <div class=\"form-group row\" style=\"margin-top: 15px\">\r\n                        <label for=\"operationalRangePolicy\" class=\"col-md-2 col-form-label\">Operational range</label>\r\n                        <div class=\"col-md-6\">\r\n                          <input class=\"form-control\" disabled type=\"text\" min=\"0\" [attr.value]=\"activePolicy.policyObject.operationalRange.toString()\" id=\"operationalRangePolicy\">\r\n                        </div>\r\n                        <div class=\"col-md-2\">\r\n                          <button class=\"btn btn-primary\" (click)=\"toggleOperationalRangeDialog()\">{{ operationalRangeVisible ? 'Hide' : 'Edit' }}</button>\r\n                        </div>\r\n                        <div class=\"col-md-4\">\r\n                          <button class=\"btn\" (click)=\"updatePolicyRecommendation()\">Recommendation</button>\r\n                        </div>\r\n                      </div>\r\n\r\n                      <div class=\"form-group row\" *ngIf=\"currentRecommendation != undefined\">\r\n                        <label for=\"operationalRangePolicyRec\" class=\"col-md-2 col-form-label\">Recommendation for operational range</label>\r\n                        <div class=\"col-md-8\">\r\n                          <input class=\"form-control\" disabled type=\"text\" min=\"0\" [attr.value]=\"currentRecommendation.toString()\" id=\"operationalRangePolicyRec\">\r\n                        </div>\r\n                        <div class=\"col-md-2\">\r\n                          <button class=\"btn btn-primary\" (click)=\"applyRecommendation()\">Apply</button>\r\n                        </div>\r\n                      </div>\r\n\r\n                      <div class=\"form-group row\" *ngIf=\"operationalRangeVisible\">\r\n                        <label for=\"operationalRangePolicy\" class=\"col-md-2 col-form-label\">Operational settings</label>\r\n                        <div class=\"col-md-10\">\r\n                          <div class=\"row\">\r\n                            <br/><br/>\r\n                          </div>\r\n                          <div class=\"row\">\r\n                            <label class=\"col-md-3\">Lower bound infinite</label>\r\n                            <div class=\"col-md-9\">\r\n                              <ui-switch\r\n                                      [(ngModel)]=\"activePolicy.policyObject.operationalRange.isBeginInfinite\"\r\n                                      [size]=\"'small'\">\r\n                              </ui-switch>\r\n                            </div>\r\n                          </div>\r\n\r\n                          <div class=\"row\" *ngIf=\"!activePolicy.policyObject.operationalRange.isBeginInfinite\">\r\n                            <label class=\"col-md-3\">Lower bound value</label>\r\n                            <div class=\"col-md-4\">\r\n                              <input type=\"number\" class=\"form-control\" [(ngModel)]=\"activePolicy.policyObject.operationalRange.begin\"/>\r\n                            </div>\r\n                            <label class=\"col-md-1\">including</label>\r\n                            <div class=\"col-md-4\">\r\n                              <ui-switch\r\n                                      [(ngModel)]=\"activePolicy.policyObject.operationalRange.isBeginIncluding\"\r\n                                      [size]=\"'small'\">\r\n                              </ui-switch>\r\n                            </div>\r\n                          </div>\r\n\r\n                          <div class=\"row\">\r\n                            <label class=\"col-md-3\">Upper bound infinite</label>\r\n                            <div class=\"col-md-9\">\r\n                              <ui-switch\r\n                                      [(ngModel)]=\"activePolicy.policyObject.operationalRange.isEndInfinite\"\r\n                                      [size]=\"'small'\">\r\n                              </ui-switch>\r\n                            </div>\r\n                          </div>\r\n\r\n                          <div class=\"row\" *ngIf=\"!activePolicy.policyObject.operationalRange.isEndInfinite\">\r\n                            <label class=\"col-md-3\">Upper bound value</label>\r\n                            <div class=\"col-md-4\">\r\n                              <input type=\"number\" class=\"form-control\" [(ngModel)]=\"activePolicy.policyObject.operationalRange.end\"/>\r\n                            </div>\r\n                            <label class=\"col-md-1\">including</label>\r\n                            <div class=\"col-md-4\">\r\n                              <ui-switch\r\n                                      [(ngModel)]=\"activePolicy.policyObject.operationalRange.isEndIncluding\"\r\n                                      [size]=\"'small'\">\r\n                              </ui-switch>\r\n                            </div>\r\n                          </div>\r\n\r\n                        </div>\r\n                      </div>\r\n\r\n                      <hr/>\r\n\r\n                      <div class=\"form-group row\">\r\n                        <label for=\"metricAttributeSelection\" class=\"col-md-2 col-form-label\">Attribute</label>\r\n                        <div class=\"col-md-10\">\r\n                          <select class=\"form-control\" [(ngModel)]=\"activePolicy.policyObject.attributeName\" id=\"metricAttributeSelection\">\r\n                              <option  *ngFor=\"let attribute of attributes\" [ngValue]=\"attribute.name\">{{attribute.name}}</option>\r\n                          </select>\r\n                        </div>\r\n                      </div>\r\n\r\n                      <div class=\"form-group row\">\r\n                        <label for=\"metricAggregationSelection\" class=\"col-md-2 col-form-label\">Aggregation</label>\r\n                        <div class=\"col-md-10\">\r\n                          <select class=\"form-control\" [(ngModel)]=\"activePolicy.policyObject.aggregation\" id=\"metricAggregationSelection\">\r\n                            <option  *ngFor=\"let aggregation of aggregations\" [ngValue]=\"aggregation\">{{aggregation}}</option>\r\n                          </select>\r\n                        </div>\r\n                      </div>\r\n\r\n                      <div class=\"form-group row\">\r\n                        <label for=\"analysisDepthPolicy\" class=\"col-md-2 col-form-label\">Analysis depth (ms)</label>\r\n                        <div class=\"col-md-10\">\r\n                          <input class=\"form-control\" type=\"number\" min=\"0\" [(ngModel)]=\"activePolicy.policyObject.analysisDepth\" id=\"analysisDepthPolicy\">\r\n                        </div>\r\n                      </div>\r\n\r\n                      <div class=\"form-group row\" *ngIf=\"activeWatcher.votingStrategy == 'custom'\">\r\n                        <label for=\"voteWeightAttrPolicy\" class=\"col-md-2 col-form-label\">Vote weight</label>\r\n                        <div class=\"col-md-10\">\r\n                          <input class=\"form-control\" type=\"number\"  min=\"0\" [(ngModel)]=\"activePolicy.policyObject.voteWeight\" id=\"voteWeightAttrPolicy\">\r\n                        </div>\r\n                      </div>\r\n\r\n                      <div class=\"form-group row\">\r\n                        <label for=\"observationTimeAttrPolicy\" class=\"col-md-2 col-form-label\">Observation time (ms)</label>\r\n                        <div class=\"col-md-10\">\r\n                          <input class=\"form-control\" type=\"number\" min=\"0\" [(ngModel)]=\"activePolicy.policyObject.observationTime\" id=\"observationTimeAttrPolicy\">\r\n                        </div>\r\n                      </div>\r\n\r\n                      <div class=\"form-group row\">\r\n                        <label class=\"col-md-2 col-form-label\">Incremental weight</label>\r\n                        <div class=\"col-md-10\">\r\n                          <ui-switch\r\n                                  [(ngModel)]=\"activePolicy.policyObject.incrementalWeight\"\r\n                                  [size]=\"'small'\">\r\n                          </ui-switch>\r\n                        </div>\r\n                      </div>\r\n\r\n                    </div>\r\n\r\n                    <div class=\"col-md-2\" style=\"float:right;\">\r\n                      <button class=\"btn btn-primary\" data-dismiss=\"modal\" (click)=\"saveCurrentPolicy()\">Save policy</button>\r\n                    </div>\r\n                  </div>\r\n                </div>\r\n              </div>\r\n            </div>\r\n          </div>\r\n        </div>\r\n      </div>\r\n    </div>\r\n\r\n    <!-- Supervisors subentities (checkers, triggers, policies) and supervisor's inner settings -->\r\n    <div class=\"row\" style=\"margin-top: 30px\">\r\n\r\n      <div class=\"col-md-8\">\r\n        <div class=\"row\">\r\n          <!-- Inner settings -->\r\n          <panel [header]=\"'Supervisor settings'\" [column]=\"'12'\" *ngIf=\"activeWatcher != undefined\">\r\n            <div class=\"form-group row\">\r\n              <label for=\"watcherTypeSelection\" class=\"col-md-2 col-form-label\">Type</label>\r\n              <div class=\"col-md-10\">\r\n                <select class=\"form-control\" [(ngModel)]=\"activeWatcher.type\" id=\"watcherTypeSelection\">\r\n                  <option  *ngFor=\"let supervisor of availableSupervisors\" [ngValue]=\"supervisor.type\">{{supervisor.type}}</option>\r\n                </select>\r\n              </div>\r\n            </div>\r\n\r\n            <div class=\"form-group row\">\r\n              <label for=\"connectionStringTmpl\" class=\"col-md-2 col-form-label\">Connection string template</label>\r\n              <div class=\"col-md-10\">\r\n                <input class=\"form-control\" type=\"text\" [(ngModel)]=\"activeWatcher.connectionStringTemplate\" id=\"connectionStringTmpl\">\r\n              </div>\r\n            </div>\r\n\r\n            <div class=\"form-group row\" *ngIf=\"activeWatcher.autoScaling\">\r\n              <label for=\"scalingSizeInput\" class=\"col-md-2 col-form-label\">Scaling size</label>\r\n              <div class=\"col-md-10\">\r\n                <input class=\"form-control\" type=\"number\" min=\"0\" [attr.max]=\"activeWatcher.maxClusterSize\" [(ngModel)]=\"activeWatcher.scalingSize\" id=\"scalingSizeInput\">\r\n              </div>\r\n            </div>\r\n\r\n            <div class=\"form-group row\" *ngIf=\"activeWatcher.autoScaling\">\r\n              <label for=\"minClusterSizeInput\" class=\"col-md-2 col-form-label\">Minimum cluster size</label>\r\n              <div class=\"col-md-10\">\r\n                <input class=\"form-control\" type=\"number\" min=\"0\" [(ngModel)]=\"activeWatcher.minClusterSize\" id=\"minClusterSizeInput\">\r\n              </div>\r\n            </div>\r\n\r\n            <div class=\"form-group row\" *ngIf=\"activeWatcher.autoScaling\">\r\n              <label for=\"maxClusterSizeInput\" class=\"col-md-2 col-form-label\">Maximum cluster size</label>\r\n              <div class=\"col-md-10\">\r\n                <input class=\"form-control\" type=\"number\" [attr.min]=\"activeWatcher.minClusterSize\" [(ngModel)]=\"activeWatcher.maxClusterSize\" id=\"maxClusterSizeInput\">\r\n              </div>\r\n            </div>\r\n\r\n            <div class=\"form-group row\" *ngIf=\"activeWatcher.autoScaling\">\r\n              <label for=\"cooldownTimeInput\" class=\"col-md-2 col-form-label\">Cooldown time (ms)</label>\r\n              <div class=\"col-md-10\">\r\n                <input class=\"form-control\" type=\"number\" [(ngModel)]=\"activeWatcher.cooldownTime\" id=\"cooldownTimeInput\">\r\n              </div>\r\n            </div>\r\n\r\n            <div class=\"form-group row\">\r\n              <label class=\"col-md-2 col-form-label\">Autoscaling</label>\r\n              <div class=\"col-md-10\">\r\n                <ui-switch\r\n                        [(ngModel)]=\"activeWatcher.autoScaling\"\r\n                        [size]=\"'small'\">\r\n                </ui-switch>\r\n              </div>\r\n            </div>\r\n          </panel>\r\n        </div>\r\n        <div class=\"row\">\r\n          <!-- Set the component -->\r\n          <panel [header]=\"'Supervisor group'\" [column]=\"'12'\" *ngIf=\"activeWatcher != undefined\">\r\n\r\n            <div class=\"form-group row\">\r\n              <label class=\"col-md-2 col-form-label\">Select group from list</label>\r\n              <div class=\"col-md-10\">\r\n                <ui-switch\r\n                        [(ngModel)]=\"groupSelection\"\r\n                        [disabled]=\"!components || components.length == 0\"\r\n                        [size]=\"'small'\">\r\n                </ui-switch>\r\n              </div>\r\n            </div>\r\n\r\n            <select\r\n                    class=\"form-control\"\r\n                    [(ngModel)]=\"selectedComponent\"\r\n                    id=\"componentSelection\"\r\n                    *ngIf=\"groupSelection && components && components.length > 0\"\r\n                    (ngModelChange)=\"selectCurrentComponent($event)\">\r\n              <option  *ngFor=\"let component of components\" [ngValue]=\"component\">{{component}}</option>\r\n            </select>\r\n\r\n            <div class=\"form-group row\" *ngIf=\"!groupSelection\">\r\n              <label for=\"manualGroupName\" class=\"col-md-2 col-form-label\">Input group name</label>\r\n              <div class=\"col-md-10\">\r\n                <div class=\"input-group\">\r\n                  <input class=\"form-control\"\r\n                         type=\"text\"\r\n                         [(ngModel)]=\"selectedComponent\"\r\n                         (ngModelChange)=\"triggerGroupNameChanged($event)\"\r\n                         id=\"manualGroupName\"\r\n                         placeholder=\"Input group name manually...\">\r\n                  <span class=\"input-group-btn\">\r\n                     <button class=\"btn btn-secondary\" [class.btn-primary]=\"groupNameChanged\" type=\"button\" (click)=\"saveManualGroupName()\" [disabled]=\"!groupNameChanged\">Save</button>\r\n                  </span>\r\n                </div>\r\n              </div>\r\n            </div>\r\n\r\n            <div class=\"row row-margin-bottom-sm\" *ngIf=\"!(attributes && attributes.length > 0)\">\r\n              <h5>Selected group does not contain attributes - please select another component or set some attributes</h5>\r\n            </div>\r\n\r\n          </panel>\r\n        </div>\r\n      </div>\r\n\r\n      <div class=\"col-md-4\" *ngIf=\"activeWatcher != undefined\">\r\n        <div class=\"row\">\r\n          <div class=\"animated flipInY col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n            <div class=\"tile-stats\">\r\n              <div class=\"icon\" style=\"cursor: pointer;\" (click)=\"initTriggerModal()\"><i class=\"fa fa-pencil-square-o\"></i></div>\r\n              <div class=\"count\">1</div>\r\n              <h3>Trigger</h3>\r\n              <p>Edit trigger for supervisor</p>\r\n            </div>\r\n          </div>\r\n        </div>\r\n\r\n        <div class=\"row\">\r\n          <div class=\"animated flipInY col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n            <div class=\"tile-stats\">\r\n              <div class=\"icon\" style=\"cursor: pointer;\" (click)=\"initCheckersModal()\"><i class=\"fa fa-pencil-square-o\"></i></div>\r\n              <div class=\"count\">{{activeWatcher.getCheckersCount()}}</div>\r\n              <h3>Checkers</h3>\r\n              <p>Edit checkers for supervisor</p>\r\n            </div>\r\n          </div>\r\n        </div>\r\n\r\n        <div class=\"row\">\r\n          <div class=\"animated flipInY col-lg-12 col-md-12 col-sm-12 col-xs-12\">\r\n            <div class=\"tile-stats\">\r\n              <div class=\"icon\" style=\"cursor: pointer;\" (click)=\"initPoliciesModal()\"><i class=\"fa fa-pencil-square-o\"></i></div>\r\n              <div class=\"count\">{{activeWatcher.getPoliciesCount()}}</div>\r\n              <h3>Policies</h3>\r\n              <p>Edit policies for supervisor</p>\r\n            </div>\r\n          </div>\r\n        </div>\r\n      </div>\r\n    </div>\r\n\r\n    <!-- Supervisor's parameter table -->\r\n    <div class=\"row\" style=\"margin-top: 10px\">\r\n      <panel [header]=\"'Supervisor parameters'\" [column]=\"'12'\" *ngIf=\"activeWatcher != undefined\">\r\n        <ptable [entity]=\"activeWatcher\"></ptable>\r\n      </panel>\r\n    </div>\r\n\r\n    <!-- Save/cancel buttons -->\r\n    <div class=\"row\" style=\"margin-top: 20px; float:right;\" *ngIf=\"activeWatcher != undefined\">\r\n      <hr/>\r\n      <div>\r\n        <button class=\"btn btn-primary\" (click)=\"saveActiveWatcher()\">Save</button>\r\n        <button class=\"btn\" (click)=\"cleanSelection()\">Cancel/clean</button>\r\n      </div>\r\n    </div>\r\n\r\n    <!-- Supervisors table -->\r\n    <div class=\"row\" style=\"margin-top: 10px\" *ngIf=\"activeWatcher == undefined\">\r\n      <panel [header]=\"'List of supervisors'\" [column]=\"'12'\">\r\n        <table class=\"table table-bordered\">\r\n          <thead class=\"thead-inverse\">\r\n          <tr>\r\n            <th style=\"width:90px;\">Actions</th>\r\n            <th>Name</th>\r\n            <th>Type</th>\r\n            <th>Autoscaling</th>\r\n            <th>Scaling size</th>\r\n            <th>Cooldown time</th>\r\n            <th>Min/max cluster size</th>\r\n            <th>Checkers</th>\r\n            <th>Trigger</th>\r\n            <th>Scaling policy</th>\r\n          </tr>\r\n          </thead>\r\n          <tbody>\r\n          <tr *ngFor=\"let watcher of watchers\" [class.active-tr]=\"isWatcherActive(watcher)\">\r\n            <td>\r\n              <span\r\n                  class=\"glyphicon glyphicon-remove-circle btn btn-xs btn-danger\"\r\n                  (click)=\"removeWatcher(watcher)\"\r\n                  aria-hidden=\"true\">\r\n              </span>\r\n              <span\r\n                  class=\"glyphicon glyphicon-pencil btn btn-xs btn-primary\"\r\n                  (click)=\"editWatcher(watcher)\"\r\n                  aria-hidden=\"true\">\r\n              </span>\r\n            </td>\r\n            <th scope=\"row\">{{watcher.name}}</th>\r\n            <th scope=\"row\">{{watcher.type}}</th>\r\n            <th scope=\"row\">{{watcher.autoScaling}}</th>\r\n            <th scope=\"row\">{{watcher.scalingSize}}</th>\r\n            <th scope=\"row\">{{watcher.cooldownTime}}</th>\r\n            <th scope=\"row\">{{watcher.minClusterSize}}/{{watcher.maxClusterSize}}</th>\r\n            <td>\r\n              <checkers [entity]=\"watcher.attributeCheckers\"></checkers>\r\n            </td>\r\n            <td>\r\n              <trigger [entity]=\"watcher.trigger\"></trigger>\r\n            </td>\r\n            <td>\r\n              <policies [entity]=\"watcher.scalingPolicies\"></policies>\r\n            </td>\r\n          </tr>\r\n          <tr>\r\n            <td></td>\r\n            <td></td>\r\n            <td></td>\r\n            <td></td>\r\n            <td></td>\r\n            <td></td>\r\n            <td></td>\r\n            <td></td>\r\n            <td></td>\r\n            <td>\r\n              <button\r\n                      style=\"float:right;\"\r\n                      class=\"btn btn-primary btn-sm\"\r\n                      (click)=\"addNewWatcher()\">+ New supervisor</button>\r\n            </td>\r\n          </tr>\r\n          </tbody>\r\n        </table>\r\n\r\n      </panel>\r\n    </div>\r\n</div>\r\n\r\n"

/***/ },

/***/ "./src/app/watchers/templates/statuses.html":
/***/ function(module, exports) {

module.exports = "<div class=\"right_col\" role=\"main\" style=\"min-height: 949px;\">\r\n  <div class=\"\">\r\n    <div class=\"page-title\">\r\n      <div class=\"title_left\">\r\n        <h3>Resource groups statuses</h3>\r\n      </div>\r\n    </div>\r\n\r\n    <div class=\"clearfix\"></div>\r\n\r\n    <div class=\"row\" style=\"margin-top: 90px\">\r\n\r\n      <panel [header]=\"'Statuses of groups'\" [column]=\"'12'\" *ngIf=\"statuses && statuses.length > 0\">\r\n\r\n        <a *ngFor=\"let status of statuses\"\r\n           class=\"grid-item-link pointerElement\"\r\n           (click)=\"showDetails(status)\">\r\n\r\n          <div\r\n                   [class.componentError]=\"status.code != 0\"\r\n                   [class.componentOk]=\"status.code == 0\"\r\n                   class=\"grid-item\"\r\n                   [tooltip]=\"status.details()\">\r\n            <h5 class=\"name\">{{status.name}}</h5>\r\n            <p class=\"symbol\">{{status.resourceName}}</p>\r\n            <p class=\"weight\" *ngIf=\"status.code != 0\">{{status.getShortDescription()}}</p>\r\n          </div>\r\n        </a>\r\n      </panel>\r\n     </div>\r\n  </div>\r\n</div>"

/***/ },

/***/ "./src/app/watchers/templates/template.html":
/***/ function(module, exports) {

module.exports = "<router-outlet></router-outlet>"

/***/ },

/***/ "./src/app/watchers/watchers.dashboard.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var app_restClient_1 = __webpack_require__("./src/app/services/app.restClient.ts");
__webpack_require__("./node_modules/rxjs/add/operator/publishLast.js");
var angular2_modal_1 = __webpack_require__("./node_modules/angular2-modal/esm/index.js");
var index_1 = __webpack_require__("./node_modules/angular2-modal/plugins/bootstrap/index.js");
var factory_1 = __webpack_require__("./src/app/services/model/healthstatus/factory.ts");
var WatcherDashboard = (function () {
    function WatcherDashboard(apiClient, modal, overlay, vcRef) {
        this.modal = modal;
        this.timerId = undefined;
        this.statuses = [];
        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;
    }
    WatcherDashboard.prototype.ngOnInit = function () { };
    WatcherDashboard.prototype.ngAfterViewInit = function () {
        var _thisReference = this;
        // load the list of watchers
        this.timerId = setInterval(function () {
            _thisReference.http.get(app_restClient_1.REST.SUPERVISORS_STATUS)
                .map(function (res) { return res.json(); })
                .subscribe(function (data) {
                _thisReference.statuses = factory_1.StatusFactory.parseAllStatuses(data);
            });
        }, 2000);
    };
    WatcherDashboard.prototype.ngOnDestroy = function () {
        clearInterval(this.timerId);
    };
    WatcherDashboard.prototype.showDetails = function (status) {
        this.modal.alert()
            .size('lg')
            .title("Details for health status")
            .body(status.htmlDetails())
            .isBlocking(false)
            .keyboard(27)
            .open();
    };
    WatcherDashboard = __decorate([
        core_1.Component({
            moduleId: module.i,
            template: __webpack_require__("./src/app/watchers/templates/statuses.html"),
            styles: [__webpack_require__("./src/app/watchers/templates/css/statuses.css")]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof app_restClient_1.ApiClient !== 'undefined' && app_restClient_1.ApiClient) === 'function' && _a) || Object, (typeof (_b = typeof index_1.Modal !== 'undefined' && index_1.Modal) === 'function' && _b) || Object, (typeof (_c = typeof angular2_modal_1.Overlay !== 'undefined' && angular2_modal_1.Overlay) === 'function' && _c) || Object, (typeof (_d = typeof core_1.ViewContainerRef !== 'undefined' && core_1.ViewContainerRef) === 'function' && _d) || Object])
    ], WatcherDashboard);
    return WatcherDashboard;
    var _a, _b, _c, _d;
}());
exports.WatcherDashboard = WatcherDashboard;


/***/ },

/***/ "./src/app/watchers/watchers.modules.ts":
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
var watchers_template_1 = __webpack_require__("./src/app/watchers/watchers.template.ts");
var watchers_view_1 = __webpack_require__("./src/app/watchers/watchers.view.ts");
var watchers_dashboard_1 = __webpack_require__("./src/app/watchers/watchers.dashboard.ts");
var checkers_component_1 = __webpack_require__("./src/app/watchers/components/checkers.component.ts");
var trigger_component_1 = __webpack_require__("./src/app/watchers/components/trigger.component.ts");
var condition_block_1 = __webpack_require__("./src/app/watchers/components/condition.block.ts");
var watchers_pipes_1 = __webpack_require__("./src/app/watchers/watchers.pipes.ts");
var ptable_component_1 = __webpack_require__("./src/app/watchers/components/ptable.component.ts");
var policies_component_1 = __webpack_require__("./src/app/watchers/components/policies.component.ts");
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
                            { path: '', component: watchers_view_1.MainComponent },
                            { path: 'dashboard', component: watchers_dashboard_1.WatcherDashboard }
                        ]
                    }])
            ],
            declarations: [
                watchers_template_1.TemplateComponent,
                watchers_view_1.MainComponent,
                watchers_dashboard_1.WatcherDashboard,
                checkers_component_1.CheckersComponent,
                trigger_component_1.TriggerComponent,
                policies_component_1.PoliciesComponent,
                condition_block_1.ColoredCondition,
                ptable_component_1.PTable,
                watchers_pipes_1.KeysPipe
            ],
            providers: PROVIDERS
        }), 
        __metadata('design:paramtypes', [])
    ], WatchersModule);
    return WatchersModule;
}());
exports.WatchersModule = WatchersModule;


/***/ },

/***/ "./src/app/watchers/watchers.pipes.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var KeysPipe = (function () {
    function KeysPipe() {
    }
    KeysPipe.prototype.transform = function (value, args) {
        var keys = [];
        for (var key in value) {
            keys.push({ key: key, value: value[key] });
        }
        return keys;
    };
    KeysPipe = __decorate([
        core_1.Pipe({ name: 'keys' }), 
        __metadata('design:paramtypes', [])
    ], KeysPipe);
    return KeysPipe;
}());
exports.KeysPipe = KeysPipe;


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
/* WEBPACK VAR INJECTION */(function($) {"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var app_restClient_1 = __webpack_require__("./src/app/services/app.restClient.ts");
var Observable_1 = __webpack_require__("./node_modules/rxjs/Observable.js");
var factory_1 = __webpack_require__("./src/app/watchers/model/factory.ts");
var watcher_1 = __webpack_require__("./src/app/watchers/model/watcher.ts");
var scriptlet_data_object_1 = __webpack_require__("./src/app/watchers/model/scriptlet.data.object.ts");
var colored_checker_1 = __webpack_require__("./src/app/watchers/model/colored.checker.ts");
var attribute_1 = __webpack_require__("./src/app/charts/model/attribute.ts");
var angular2_modal_1 = __webpack_require__("./node_modules/angular2-modal/esm/index.js");
var vex_1 = __webpack_require__("./node_modules/angular2-modal/plugins/vex/index.js");
__webpack_require__("./node_modules/rxjs/add/operator/publishLast.js");
__webpack_require__("./node_modules/smartwizard/js/jquery.smartWizard.min.js");
var health_status_based_scaling_policy_1 = __webpack_require__("./src/app/watchers/model/policy/health.status.based.scaling.policy.ts");
var attribute_based_scaling_policy_1 = __webpack_require__("./src/app/watchers/model/policy/attribute.based.scaling.policy.ts");
var util_1 = __webpack_require__("./node_modules/util/util.js");
var operational_range_1 = __webpack_require__("./src/app/watchers/model/policy/operational.range.ts");
var MainComponent = (function () {
    function MainComponent(http, modal, overlay, vcRef, cd) {
        this.http = http;
        this.modal = modal;
        this.cd = cd;
        this.components = [];
        this.watchers = [];
        this.activeWatcher = undefined;
        this.isNewEntity = true;
        this.selectedComponent = undefined;
        this.triggerInitialized = false;
        this.checkersInitialized = false;
        this.policiesInitialized = false;
        this.attributes = [];
        this.selectedAttribute = undefined;
        this.activeChecker = new scriptlet_data_object_1.ScriptletDataObject({});
        this.activePolicy = new scriptlet_data_object_1.ScriptletDataObject({});
        this.activePolicyName = "";
        this.checkersType = EntityWithDescription.generateCheckersTypes();
        this.policyTypes = EntityWithDescription.generatePoliciesTypes();
        this.strategyTypes = EntityWithDescription.generateStrategyTypes();
        this.triggerLanguages = ["Groovy" /*, "JavaScript"*/];
        this.availableSupervisors = [];
        this.healthStatusLevels = ["LOW", "MODERATE", "SUBSTANTIAL", "SEVERE", "CRITICAL"];
        this.aggregations = ["MAX", "MIN", "MEAN", "MEDIAN", "PERCENTILE_90", "PERCENTILE_95", "PERCENTILE_97", "SUM"];
        this.defaultGroovyCheckerScript = "";
        this.defaultGroovyTriggerScript = "";
        this.defaultGroovyPolicyScript = "";
        this.operationalRangeVisible = false;
        this.currentRecommendation = undefined;
        this.newPolicyAppended = false;
        this.groupSelection = false;
        this.groupNameChanged = false;
        this.oldSelectedComponent = "";
        overlay.defaultViewContainer = vcRef;
    }
    MainComponent.prototype.toggleOperationalRangeDialog = function () {
        this.operationalRangeVisible = !this.operationalRangeVisible;
    };
    MainComponent.prototype.saveCurrentTrigger = function () {
        console.debug("Trigger has been saved: ", this.activeWatcher);
    };
    MainComponent.prototype.saveCurrentChecker = function () {
        this.activeWatcher.attributeCheckers[this.selectedAttribute.name] = this.activeChecker;
        console.debug("Checker has been saved", this.activeWatcher);
    };
    MainComponent.prototype.getMainHeader = function () {
        return util_1.isNullOrUndefined(this.activeWatcher) ? "Setup supervisors" : "Setup " + this.activeWatcher.name + " supervisor";
    };
    MainComponent.prototype.ngOnInit = function () {
        var _this = this;
        // load the list of watchers
        this.http.get(app_restClient_1.REST.SUPERVISORS_CONFIG)
            .map(function (res) { return res.json(); })
            .subscribe(function (data) {
            _this.watchers = factory_1.Factory.watchersArrayFromJSON(data);
            console.debug("All the watchers list: ", _this.watchers);
        });
        // find all the components
        this.http.get(app_restClient_1.REST.GROUPS_WEB_API)
            .map(function (res) {
            return res.json();
        })
            .subscribe(function (data) {
            _this.components = data;
        });
        // fill available supervisors list
        this.http.get(app_restClient_1.REST.AVAILABLE_SUPERVISORS_LIST)
            .map(function (res) { return res.json(); })
            .subscribe(function (data) {
            console.debug("Available supervisors list is: ", data);
            _this.availableSupervisors = data;
        });
        this.http.get(app_restClient_1.REST.GROOVY_PATH + "/AttributeChecker.groovy")
            .map(function (res) { return res.text(); })
            .subscribe(function (data) {
            _this.defaultGroovyCheckerScript = data;
            _this.activeChecker.script = data;
        });
        this.http.get(app_restClient_1.REST.GROOVY_PATH + "/HealthTrigger.groovy")
            .map(function (res) { return res.text(); })
            .subscribe(function (data) {
            _this.defaultGroovyTriggerScript = data;
        });
        this.http.get(app_restClient_1.REST.GROOVY_PATH + "/ScalingPolicy.groovy")
            .map(function (res) { return res.text(); })
            .subscribe(function (data) {
            _this.defaultGroovyPolicyScript = data;
            _this.activePolicy.script = data;
        });
    };
    MainComponent.prototype.ngAfterViewInit = function () { };
    MainComponent.prototype.initTriggerModal = function () {
        // clean the data if the component was already initialized
        if (this.triggerInitialized) {
            // reset wizard
            $(this.getTriggerWizardId()).off("showStep");
            $(this.getTriggerWizardId()).smartWizard("reset");
        }
        this.initTriggerWizard();
        // open the modal
        $("#editTriggerModal").modal("show");
        // and next time user adds the chart - we will reinit all the dialog
        this.triggerInitialized = true;
    };
    MainComponent.prototype.initCheckersModal = function () {
        // clean the data if the component was already initialized
        if (this.checkersInitialized) {
            // reset wizard
            $(this.getCheckersWizardId()).off("showStep");
            $(this.getCheckersWizardId()).smartWizard("reset");
        }
        this.initCheckersWizard();
        // open the modal
        $("#editCheckerModal").modal("show");
        // and next time user adds the chart - we will reinit all the dialog
        this.checkersInitialized = true;
    };
    MainComponent.prototype.initPoliciesModal = function () {
        // clean the data if the component was already initialized
        this.newPolicyAppended = false;
        if (this.policiesInitialized) {
            // reset wizard
            $(this.getPoliciesWizardId()).off("showStep");
            $(this.getPoliciesWizardId()).smartWizard("reset");
        }
        this.initPoliciesWizard();
        // open the modal
        $("#editPolicyModal").modal("show");
        // and next time user adds the chart - we will reinit all the dialog
        this.policiesInitialized = true;
    };
    MainComponent.prototype.selectCurrentComponent = function (component) {
        this.selectedComponent = component;
        this.oldSelectedComponent = component;
        this.loadAttributesOnComponentSelected();
        this.activeWatcher.name = component;
        this.activeWatcher.trigger = new scriptlet_data_object_1.ScriptletDataObject({});
        this.activeWatcher.trigger.script = this.defaultGroovyTriggerScript;
        this.activeWatcher.attributeCheckers = {};
        this.activeWatcher.scalingPolicies = {};
    };
    MainComponent.prototype.isTriggerAvailable = function () {
        return (this.activeWatcher != undefined && this.activeWatcher.trigger != undefined);
    };
    MainComponent.prototype.isCheckersAvailable = function () {
        return (this.activeWatcher != undefined
            && this.activeWatcher.attributeCheckers != undefined
            && !$.isEmptyObject(this.activeWatcher.attributeCheckers));
    };
    MainComponent.prototype.removeCheckerForAttribute = function (attr) {
        delete this.activeWatcher.attributeCheckers[attr.name];
    };
    MainComponent.prototype.editCheckerForAttribute = function (attr) {
        if (!this.activeWatcher.checkerExists(attr.name)) {
            this.activeWatcher.attributeCheckers[attr.name] = new scriptlet_data_object_1.ScriptletDataObject({});
            this.activeWatcher.attributeCheckers[attr.name].script = this.defaultGroovyCheckerScript;
        }
        this.activeChecker = this.activeWatcher.attributeCheckers[attr.name];
        this.selectedAttribute = attr;
        $(this.getCheckersWizardId()).smartWizard("next");
    };
    MainComponent.prototype.loadAttributesOnComponentSelected = function () {
        var _this = this;
        console.debug("Looking for attributes for group: ", this.selectedComponent);
        this.attributes = [];
        this.http.get(app_restClient_1.REST.CHART_METRICS_BY_COMPONENT(this.selectedComponent))
            .map(function (res) {
            var _data = res.json();
            var _values = [];
            for (var i in _data) {
                _values.push(new attribute_1.AttributeInformation(_data[i]));
            }
            return _values;
        })
            .catch(function (res) { return Observable_1.Observable.of([]); }).cache()
            .subscribe(function (data) {
            _this.attributes = data;
            console.debug("attributes: ", data);
        });
    };
    MainComponent.prototype.selectCheckerType = function (type) {
        if (type == "ColoredAttributeChecker") {
            this.activeChecker.object = new colored_checker_1.ColoredAttributeChecker();
        }
        else {
            this.activeChecker.object = undefined;
            this.activeChecker.script = this.defaultGroovyCheckerScript;
        }
    };
    MainComponent.prototype.cleanSelection = function () {
        this.activeWatcher = undefined;
        this.isNewEntity = true;
    };
    MainComponent.prototype.removeWatcher = function (watcher) {
        var _this = this;
        this.modal.confirm()
            .className('default')
            .message('Watcher is going to be removed. Proceed?')
            .open()
            .then(function (resultPromise) {
            return resultPromise.result
                .then(function (response) {
                _this.http.delete(app_restClient_1.REST.SUPERVISOR_BY_NAME(watcher.name))
                    .map(function (res) { return res.text(); })
                    .subscribe(function (data) {
                    console.debug("watcher has been removed: ", data);
                    for (var i = 0; i < _this.watchers.length; i++) {
                        if (_this.watchers[i].name == watcher.name) {
                            _this.watchers.splice(i, 1);
                            break;
                        }
                    }
                    _this.cd.detectChanges();
                });
                return response;
            })
                .catch(function () {
                console.debug("user preferred to decline watcher removing");
            });
        });
    };
    MainComponent.prototype.editWatcher = function (watcher) {
        this.activeWatcher = $.extend(true, {}, watcher);
        this.isNewEntity = false;
        this.selectedComponent = watcher.name;
        this.oldSelectedComponent = watcher.name;
        this.groupNameChanged = false;
        this.loadAttributesOnComponentSelected();
        this.groupSelection = this.getGroupSelectionForActiveResource();
    };
    MainComponent.prototype.getPanelHeader = function () {
        return this.isNewEntity ? "Add new watcher" : ("Edit watcher " + this.activeWatcher.name);
    };
    MainComponent.prototype.getTriggerWizardId = function () {
        return "#smartwizardForTrigger";
    };
    MainComponent.prototype.getCheckersWizardId = function () {
        return "#smartwizardForCheckers";
    };
    MainComponent.prototype.getPoliciesWizardId = function () {
        return "#smartwizardForPolicies";
    };
    MainComponent.prototype.initTriggerWizard = function () {
        $(this.getTriggerWizardId()).smartWizard({
            theme: 'arrows',
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });
        $(this.getTriggerWizardId()).on("showStep", function (e, anchorObject, stepNumber, stepDirection) {
            console.debug(stepNumber);
        });
    };
    MainComponent.prototype.initCheckersWizard = function () {
        this.activeChecker = new scriptlet_data_object_1.ScriptletDataObject({});
        this.activeChecker.script = this.defaultGroovyCheckerScript;
        $(this.getCheckersWizardId()).smartWizard({
            theme: 'arrows',
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });
        $(this.getCheckersWizardId()).on("showStep", function (e, anchorObject, stepNumber, stepDirection) {
            console.debug(stepNumber);
        });
    };
    MainComponent.prototype.initPoliciesWizard = function () {
        this.activePolicy = new scriptlet_data_object_1.ScriptletDataObject({});
        this.activePolicyName = "";
        this.activePolicy.script = this.defaultGroovyPolicyScript;
        this.operationalRangeVisible = false;
        this.currentRecommendation = undefined;
        $(this.getPoliciesWizardId()).smartWizard({
            theme: 'arrows',
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });
        $(this.getPoliciesWizardId()).on("showStep", function (e, anchorObject, stepNumber, stepDirection) {
            console.debug(stepNumber);
        });
    };
    MainComponent.prototype.addNewWatcher = function () {
        this.activeWatcher = new watcher_1.Watcher(undefined, {});
        this.activeWatcher.trigger.script = this.defaultGroovyTriggerScript;
        this.selectedComponent = "";
    };
    MainComponent.prototype.onGreenNotify = function (event) {
        this.activeChecker.object.green = event;
        console.debug("Saved green condition: ", event);
    };
    MainComponent.prototype.onYellowNotify = function (event) {
        this.activeChecker.object.yellow = event;
        console.debug("Saved yellow condition: ", event);
    };
    MainComponent.prototype.saveActiveWatcher = function () {
        var _this = this;
        console.debug("Saving selected watcher: ", this.activeWatcher, ", json is: ", this.activeWatcher.toJSON());
        this.http.put(app_restClient_1.REST.SUPERVISOR_BY_NAME(this.activeWatcher.name), this.activeWatcher.toJSON())
            .map(function (res) { return res.text(); })
            .subscribe(function (data) {
            console.debug("watcher has been saved: ", data);
            var _found = false;
            for (var i = 0; i < _this.watchers.length; i++) {
                if (_this.watchers[i].name == _this.activeWatcher.name) {
                    _this.watchers[i] = _this.activeWatcher;
                    _found = true;
                    break;
                }
            }
            if (!_found) {
                _this.watchers.push(_this.activeWatcher);
            }
            _this.cleanSelection();
        });
    };
    MainComponent.prototype.isWatcherActive = function (_watcher) {
        return this.activeWatcher != null && this.activeWatcher.name == _watcher.name;
    };
    MainComponent.prototype.isPolicyActive = function (policyKey) {
        return this.activePolicy != undefined && this.activePolicyName != undefined && this.activePolicyName == policyKey;
    };
    MainComponent.prototype.editPolicy = function (policyKey, policyValue) {
        this.activePolicyName = policyKey;
        this.activePolicy = policyValue;
        $(this.getPoliciesWizardId()).smartWizard("next");
    };
    MainComponent.prototype.removePolicy = function (policyKey) {
        delete this.activeWatcher.scalingPolicies[policyKey];
        var newMap = {};
        for (var key in this.activeWatcher.scalingPolicies) {
            newMap[key] = this.activeWatcher.scalingPolicies[key];
        }
        this.activeWatcher.scalingPolicies = newMap;
    };
    MainComponent.prototype.addNewPolicy = function () {
        var _this = this;
        this.modal.prompt()
            .className('default')
            .message('New policy')
            .placeholder('Please set the name for a new policy')
            .open()
            .then(function (dialog) { return dialog.result; })
            .then(function (result) {
            _this.activeWatcher.scalingPolicies[result] = new scriptlet_data_object_1.ScriptletDataObject({});
            _this.activeWatcher.scalingPolicies[result].script = _this.defaultGroovyPolicyScript;
            var newMap = {};
            for (var key in _this.activeWatcher.scalingPolicies) {
                newMap[key] = _this.activeWatcher.scalingPolicies[key];
            }
            _this.activeWatcher.scalingPolicies = newMap;
            _this.activePolicy = _this.activeWatcher.scalingPolicies[result];
            _this.activePolicyName = result;
            _this.newPolicyAppended = true;
            $(_this.getPoliciesWizardId()).smartWizard("next");
            _this.cd.markForCheck();
        })
            .catch(function () { });
    };
    MainComponent.prototype.selectPolicyType = function (type) {
        if (type == "HealthStatusBased") {
            this.activePolicy.policyObject = new health_status_based_scaling_policy_1.HealthStatusBasedScalingPolicy();
        }
        else if (type == "MetricBased") {
            this.activePolicy.policyObject = new attribute_based_scaling_policy_1.AttributeBasedScalingPolicy();
        }
        else {
            this.activeChecker.policyObject = undefined;
            this.activePolicy.script = this.defaultGroovyPolicyScript;
        }
    };
    MainComponent.prototype.saveCurrentPolicy = function () {
        this.activeWatcher.scalingPolicies[this.activePolicyName] = this.activePolicy;
        console.debug("Policy has been saved");
    };
    MainComponent.prototype.selectVotingStrategy = function (type) {
        this.activeWatcher.votingStrategy = type;
        this.activeWatcher.recalculateVotes();
    };
    MainComponent.prototype.updatePolicyRecommendation = function () {
        var _this = this;
        this.http.get(app_restClient_1.REST.SUPERVISOR_POLICY_RECOMMENDATION(this.activeWatcher.name, this.activePolicyName))
            .map(function (res) { console.debug("Response is: ", res.json()); return res.json(); })
            .subscribe(function (data) {
            _this.currentRecommendation = operational_range_1.OpRange.fromString(data);
            _this.cd.detectChanges();
        });
    };
    MainComponent.prototype.applyRecommendation = function () {
        this.activePolicy.policyObject.operationalRange = this.currentRecommendation;
        this.currentRecommendation = undefined;
        this.cd.detectChanges();
    };
    // group setting
    MainComponent.prototype.saveManualGroupName = function () {
        this.selectCurrentComponent(this.selectedComponent);
        console.debug("Manual group name has been saved, no reload is required");
        this.groupNameChanged = false;
    };
    MainComponent.prototype.triggerGroupNameChanged = function (value) {
        this.groupNameChanged = (this.oldSelectedComponent != value);
    };
    MainComponent.prototype.getGroupSelectionForActiveResource = function () {
        if (util_1.isNullOrUndefined(this.components) || this.components.length == 0) {
            return false;
        }
        else if (!util_1.isNullOrUndefined(this.activeWatcher)
            && !util_1.isNullOrUndefined(this.activeWatcher.name)
            && this.activeWatcher.name.length > 0) {
            for (var i = 0; i < this.components.length; i++) {
                if (this.components[i] == this.activeWatcher.name) {
                    return true;
                }
            }
            return false;
        }
        else {
            return true;
        }
    };
    MainComponent = __decorate([
        core_1.Component({
            moduleId: module.i,
            template: __webpack_require__("./src/app/watchers/templates/main.html"),
            styles: [__webpack_require__("./src/app/watchers/templates/css/main.css")]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof app_restClient_1.ApiClient !== 'undefined' && app_restClient_1.ApiClient) === 'function' && _a) || Object, (typeof (_b = typeof vex_1.Modal !== 'undefined' && vex_1.Modal) === 'function' && _b) || Object, (typeof (_c = typeof angular2_modal_1.Overlay !== 'undefined' && angular2_modal_1.Overlay) === 'function' && _c) || Object, (typeof (_d = typeof core_1.ViewContainerRef !== 'undefined' && core_1.ViewContainerRef) === 'function' && _d) || Object, (typeof (_e = typeof core_1.ChangeDetectorRef !== 'undefined' && core_1.ChangeDetectorRef) === 'function' && _e) || Object])
    ], MainComponent);
    return MainComponent;
    var _a, _b, _c, _d, _e;
}());
exports.MainComponent = MainComponent;
var EntityWithDescription = (function () {
    function EntityWithDescription(id, description) {
        this.id = id;
        this.description = description;
    }
    EntityWithDescription.generateCheckersTypes = function () {
        var _value = [];
        _value.push(new EntityWithDescription("Groovy", "Groovy checker"));
        //_value.push(new EntityWithDescription("JavaScript", "Javascript checker"));
        _value.push(new EntityWithDescription("ColoredAttributeChecker", "Green and yellow conditions based checker"));
        return _value;
    };
    EntityWithDescription.generatePoliciesTypes = function () {
        var _value = [];
        _value.push(new EntityWithDescription("Groovy", "Groovy policy"));
        _value.push(new EntityWithDescription("HealthStatusBased", "Health status based scaling policy"));
        _value.push(new EntityWithDescription("MetricBased", "Attribute based scaling policy"));
        return _value;
    };
    EntityWithDescription.generateStrategyTypes = function () {
        var _value = [];
        _value.push(new EntityWithDescription("all", "All policies voted for scaling decision"));
        _value.push(new EntityWithDescription("any", "At least one policy voted for scaling decision"));
        _value.push(new EntityWithDescription("most", "Majorities voted for scaling decision"));
        _value.push(new EntityWithDescription("custom", "User defined vote weights"));
        return _value;
    };
    return EntityWithDescription;
}());
exports.EntityWithDescription = EntityWithDescription;

/* WEBPACK VAR INJECTION */}.call(exports, __webpack_require__("./node_modules/jquery/dist/jquery.js")))

/***/ }

});
//# sourceMappingURL=1.map