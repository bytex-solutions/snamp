webpackJsonpac__name_([1],{

/***/ "./node_modules/css-loader/index.js!./src/app/watchers/templates/css/main.css":
/***/ function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("./node_modules/css-loader/lib/css-base.js")();
// imports


// module
exports.push([module.i, ".form-group {\r\n    margin-bottom: 10px;\r\n    display: flex;\r\n}\r\n\r\n.btn-inline {\r\n    display: inline-block;\r\n    margin-left: 15px;\r\n}\r\n\r\n.btn-pull-right {\r\n    margin-top:20px;\r\n    float:right;\r\n    margin-right: 20px;\r\n}", ""]);

// exports


/***/ },

/***/ "./src/app/watchers/components/checkers.component.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var constant_attribute_predicate_1 = __webpack_require__("./src/app/watchers/model/constant.attribute.predicate.ts");
var number_comparator_predicate_1 = __webpack_require__("./src/app/watchers/model/number.comparator.predicate.ts");
var range_comparator_1 = __webpack_require__("./src/app/watchers/model/range.comparator.ts");
var CheckersComponent = (function () {
    function CheckersComponent() {
        this.entity = {};
        this.hideDetails = false;
    }
    CheckersComponent.prototype.isConstantType = function (predicate) {
        return (predicate instanceof constant_attribute_predicate_1.ConstantAttributePredicate);
    };
    CheckersComponent.prototype.isOperatorType = function (predicate) {
        return (predicate instanceof number_comparator_predicate_1.NumberComparatorPredicate);
    };
    CheckersComponent.prototype.isRangeType = function (predicate) {
        return (predicate instanceof range_comparator_1.IsInRangePredicate);
    };
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

/***/ "./src/app/watchers/components/templates/checkers.html":
/***/ function(module, exports) {

module.exports = "<div>\r\n    <dl class=\"row\" *ngFor=\"let entry of entity | keys\">\r\n\r\n        <div class=\"modal fade\" *ngIf=\"!hideDetails\" [attr.id]=\"'details_' + entry.value.id\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"detailsLabel\">\r\n            <div class=\"modal-dialog modal-lg\" role=\"document\">\r\n                <div class=\"modal-content\">\r\n                    <div class=\"modal-header\">\r\n                        <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>\r\n                        <h4 class=\"modal-title leftAlign\" id=\"detailsLabel\">Entity {{entry.value.name}} details</h4>\r\n                    </div>\r\n                    <br/>\r\n                    <div class=\"modal-body\" *ngIf=\"entry.value.object == undefined\">\r\n                        <pre class=\"normalspaces\"><code [innerHTML]=\"entry.value.script\"></code></pre>\r\n                    </div>\r\n                    <div class=\"modal-body\" *ngIf=\"entry.value.object != undefined\">\r\n                        <div class=\"alert alert-success\">\r\n                            <strong>Green condition: </strong>\r\n                            <div *ngIf=\"isConstantType(entry.value.object.green)\">\r\n                                <strong>{{entry.value.object.green.represent()}}</strong>\r\n                            </div>\r\n                            <div *ngIf=\"isOperatorType(entry.value.object.green)\">\r\n                                <strong>{{entry.value.object.green.represent()}}</strong>\r\n                            </div>\r\n                            <div *ngIf=\"isRangeType(entry.value.object.green)\">\r\n                                <strong>{{entry.value.object.green.represent()}}</strong>\r\n                            </div>\r\n                        </div>\r\n\r\n                        <div class=\"alert alert-warning\">\r\n                            <strong>Yellow condition: </strong>\r\n                            <div *ngIf=\"isConstantType(entry.value.object.yellow)\">\r\n                                <strong>{{entry.value.object.yellow.represent()}}</strong>\r\n                            </div>\r\n                            <div *ngIf=\"isOperatorType(entry.value.object.yellow)\">\r\n                                <strong>{{entry.value.object.yellow.represent()}}</strong>\r\n                            </div>\r\n                            <div *ngIf=\"isRangeType(entry.value.object.yellow)\">\r\n                                <strong>{{entry.value.object.yellow.represent()}}</strong>\r\n                            </div>\r\n                        </div>\r\n                    </div>\r\n                    <div class=\"modal-footer\">\r\n                        <button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">Close</button>\r\n                    </div>\r\n                </div>\r\n            </div>\r\n        </div>\r\n\r\n        <dt class=\"col-sm-3\">Attribute:</dt>\r\n        <dd class=\"col-sm-9\">{{entry.key}}</dd>\r\n\r\n        <dt class=\"col-sm-3\">Language:</dt>\r\n        <dd class=\"col-sm-9\">{{entry.value.language}}</dd>\r\n\r\n        <dt class=\"col-sm-3\">Is url:</dt>\r\n        <dd class=\"col-sm-9\">{{entry.value.isURL}}</dd>\r\n\r\n        <dt class=\"col-sm-3\">Script:</dt>\r\n        <dd class=\"col-sm-9\">{{entry.value.shortScript()}}\r\n            <button *ngIf=\"!hideDetails\"\r\n                    class=\"center-block btn btn-inline btn-sm\"\r\n                    data-toggle=\"modal\"\r\n                    [attr.data-target]=\"'#details_' + entry.value.id\">\r\n                <i class=\"fa fa-search\"></i> Details\r\n            </button>\r\n        </dd>\r\n    </dl>\r\n</div>"

/***/ },

/***/ "./src/app/watchers/components/templates/trigger.html":
/***/ function(module, exports) {

module.exports = "<div class=\"modal fade\" *ngIf=\"!hideDetails\" [attr.id]=\"'details_' + entity.id\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"addEntityLabel\">\r\n    <div class=\"modal-dialog modal-lg\" role=\"document\">\r\n        <div class=\"modal-content\">\r\n            <div class=\"modal-header\">\r\n                <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>\r\n                <h4 class=\"modal-title leftAlign\" id=\"addEntityLabel\">Entity {{entity.name}} details</h4>\r\n            </div>\r\n            <br/>\r\n            <div class=\"modal-body\">\r\n                <pre class=\"normalspaces\"><code [innerHTML]=\"entity.script\"></code></pre>\r\n            </div>\r\n            <div class=\"modal-footer\">\r\n                <button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">Close</button>\r\n            </div>\r\n        </div>\r\n    </div>\r\n</div>\r\n\r\n\r\n<div>\r\n    <dl class=\"row\">\r\n        <dt class=\"col-sm-3\">Language:</dt>\r\n        <dd class=\"col-sm-9\">{{entity.language}}</dd>\r\n\r\n        <dt class=\"col-sm-3\">Is url:</dt>\r\n        <dd class=\"col-sm-9\">{{entity.isURL}}</dd>\r\n\r\n        <dt class=\"col-sm-3\">Script:</dt>\r\n        <dd class=\"col-sm-9\">{{entity.shortScript()}}\r\n            <button *ngIf=\"!hideDetails\"\r\n                    class=\"center-block btn btn-inline btn-sm\"\r\n                    data-toggle=\"modal\"\r\n                    [attr.data-target]=\"'#details_' + entity.id\">\r\n                <i class=\"fa fa-search\"></i> Details\r\n            </button>\r\n        </dd>\r\n    </dl>\r\n</div>"

/***/ },

/***/ "./src/app/watchers/components/trigger.component.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var core_1 = __webpack_require__("./node_modules/@angular/core/index.js");
var scriptlet_data_object_1 = __webpack_require__("./src/app/watchers/model/scriptlet.data.object.ts");
var TriggerComponent = (function () {
    function TriggerComponent() {
        this.entity = undefined;
        this.hideDetails = false;
    }
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
"use strict";
var watcher_1 = __webpack_require__("./src/app/watchers/model/watcher.ts");
var scriptlet_data_object_1 = __webpack_require__("./src/app/watchers/model/scriptlet.data.object.ts");
var Factory = (function () {
    function Factory() {
    }
    Factory.watcherFromJSON = function (name, json) {
        var _watcher = new watcher_1.Watcher(name, json["parameters"]);
        if (json["attributeCheckers"] != undefined) {
            for (var key in json["attributeCheckers"]) {
                _watcher.attributeCheckers[key] = scriptlet_data_object_1.ScriptletDataObject.fromJSON(json["attributeCheckers"][key]);
            }
        }
        if (json["trigger"] != undefined) {
            _watcher.trigger = scriptlet_data_object_1.ScriptletDataObject.fromJSON(json["trigger"]);
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
                _value += ">";
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
var ScriptletDataObject = (function () {
    function ScriptletDataObject() {
        this.id = entity_1.Guid.newGuid();
        this.language = "Groovy";
        this.script = "";
        this.isURL = false;
        this.object = undefined;
    }
    ScriptletDataObject.prototype.shortScript = function () {
        return ((this.script.length > 60) ? this.script.substring(0, 60) + '...' : this.script);
    };
    ScriptletDataObject.fromJSON = function (json) {
        var instance = new ScriptletDataObject();
        if (json["language"] != undefined) {
            instance.language = json["language"];
        }
        if (json["script"] != undefined) {
            instance.script = json["script"];
        }
        if (json["isURL"] != undefined) {
            instance.isURL = (json["isURL"] == 'true');
        }
        switch (instance.language) {
            case "Groovy":
            case "JavaScript":
                instance.object = undefined;
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
        _value["isURL"] = this.isURL;
        return _value;
    };
    return ScriptletDataObject;
}());
exports.ScriptletDataObject = ScriptletDataObject;


/***/ },

/***/ "./src/app/watchers/model/watcher.ts":
/***/ function(module, exports, __webpack_require__) {

"use strict";
"use strict";
var entity_1 = __webpack_require__("./src/app/watchers/model/entity.ts");
var scriptlet_data_object_1 = __webpack_require__("./src/app/watchers/model/scriptlet.data.object.ts");
var Watcher = (function (_super) {
    __extends(Watcher, _super);
    function Watcher() {
        _super.apply(this, arguments);
        this.attributeCheckers = {};
        this.trigger = new scriptlet_data_object_1.ScriptletDataObject();
    }
    Watcher.prototype.toJSON = function () {
        var _value = {};
        _value["attributeCheckers"] = {};
        for (var key in this.attributeCheckers) {
            _value["attributeCheckers"][key] = this.attributeCheckers[key].toJSON();
        }
        _value["trigger"] = this.trigger.toJSON();
        _value["parameters"] = this.stringifyParameters();
        return _value;
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

/***/ "./src/app/watchers/templates/main.html":
/***/ function(module, exports) {

module.exports = "<div class=\"right_col\" role=\"main\" style=\"min-height: 949px;\">\r\n  <div class=\"\">\r\n    <div class=\"page-title\">\r\n      <div class=\"title_left\">\r\n        <h3>Setup watchers</h3>\r\n      </div>\r\n    </div>\r\n\r\n    <div class=\"clearfix\"></div>\r\n\r\n    <div class=\"row\" style=\"margin-top: 30px\">\r\n\r\n      <!-- Modal for edit the trigger for activeWatcher -->\r\n      <div class=\"modal fade\" id=\"editTriggerModal\" role=\"dialog\" aria-labelledby=\"editTriggerLabel\">\r\n        <div class=\"modal-dialog modal-xlg modal-lg\" role=\"document\">\r\n          <div class=\"modal-content\">\r\n            <div class=\"modal-header\">\r\n              <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>\r\n              <h4 class=\"modal-title leftAlign\" id=\"editTriggerLabel\">Edit trigger</h4>\r\n              <br/>\r\n              <div class=\"modal-body\">\r\n                <div id=\"smartwizardForTrigger\" style=\"height: 95%\">\r\n                  <ul>\r\n                    <li>\r\n                      <a [attr.href]=\"'#language'\">\r\n                        Language<br />\r\n                        <small>Select language for trigger</small>\r\n                      </a>\r\n                    </li>\r\n                    <li>\r\n                      <a [attr.href]=\"'#url'\">\r\n                        Use url<br />\r\n                        <small>Use URL for loading script</small>\r\n                      </a>\r\n                    </li>\r\n                    <li>\r\n                      <a [attr.href]=\"'#script'\">\r\n                        Script<br />\r\n                        <small>Trigger's script</small>\r\n                      </a>\r\n                    </li>\r\n                  </ul>\r\n\r\n                  <div style=\"height:100%\">\r\n                    <div id=\"language\" class=\"row\" style=\"margin-top: 100px;\">\r\n                      <div class=\"item form-group\">\r\n                        <label\r\n                                class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                for=\"languageSelect\"\r\n                                style=\"margin-top: 7px;\">\r\n                          Language <span class=\"required\">*</span>\r\n                        </label>\r\n                        <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                          <select class=\"form-control\" [(ngModel)]=\"activeWatcher.trigger.language\" id=\"languageSelect\">\r\n                            <option *ngFor=\"let language of triggerLanguages\" [ngValue]=\"language\">{{language}}</option>\r\n                          </select>\r\n                        </div>\r\n                      </div>\r\n                    </div>\r\n\r\n                    <div id=\"url\" class=\"row\" style=\"margin-top: 100px;\">\r\n                      <div class=\"item form-group\">\r\n                        <label\r\n                                class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                style=\"margin-top: 7px;\">\r\n                          Load script from external URL <span class=\"required\">*</span>\r\n                        </label>\r\n\r\n                        <div class=\"col-md-6 col-sm-6 col-xs-12\" >\r\n                          <ui-switch\r\n                                  [(ngModel)]=\"activeWatcher.trigger.isURL\"\r\n                                  [size]=\"'small'\">\r\n                          </ui-switch>\r\n                        </div>\r\n                      </div>\r\n                    </div>\r\n\r\n                    <div id=\"script\" class=\"row\" style=\"margin-top: 10px;\">\r\n                      <div *ngIf=\"activeWatcher.trigger.useUrl\">\r\n                        <div class=\"item form-group\">\r\n                          <label\r\n                                  class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                  for=\"urlForTrigger\"\r\n                                  style=\"margin-top: 7px;\">\r\n                            Url for script loading <span class=\"required\">*</span>\r\n                          </label>\r\n                          <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                            <input type=\"text\" id=\"urlForTrigger\" placeholder=\"Input url for script to be loaded from\" [(ngModel)]=\"activeWatcher.trigger.script\"/>\r\n                          </div>\r\n                        </div>\r\n                      </div>\r\n                      <div *ngIf=\"!activeWatcher.trigger.useUrl\">\r\n                        <div class=\"item form-group\">\r\n                          <label\r\n                                  class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                  for=\"scriptBodyForTrigger\"\r\n                                  style=\"margin-top: 7px;\">\r\n                            Url for script loading <span class=\"required\">*</span>\r\n                          </label>\r\n                          <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                            <textarea id=\"scriptBodyForTrigger\" [(ngModel)]=\"activeWatcher.trigger.script\"></textarea>\r\n                          </div>\r\n                        </div>\r\n                      </div>\r\n                      <div class=\"col-md-3\" style=\"float:right; margin-right: 20px;\">\r\n                        <button class=\"btn btn-primary\" (click)=\"saveCurrentTrigger()\">Save trigger</button>\r\n                      </div>\r\n                    </div>\r\n                  </div>\r\n                </div>\r\n              </div>\r\n            </div>\r\n          </div>\r\n        </div>\r\n      </div>\r\n\r\n      <!-- Modal for edit checkers for activeWatcher -->\r\n      <div class=\"modal fade\" id=\"editCheckersModal\" role=\"dialog\" aria-labelledby=\"editCheckersLabel\">\r\n        <div class=\"modal-dialog modal-xlg modal-lg\" role=\"document\">\r\n          <div class=\"modal-content\">\r\n            <div class=\"modal-header\">\r\n              <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><span aria-hidden=\"true\">&times;</span></button>\r\n              <h4 class=\"modal-title leftAlign\" id=\"editCheckersLabel\">Edit trigger</h4>\r\n              <br/>\r\n              <div class=\"modal-body\">\r\n                <div id=\"smartwizardForCheckers\" style=\"height: 95%\">\r\n                  <ul>\r\n                    <li>\r\n                      <a [attr.href]=\"'#language'\">\r\n                        Language<br />\r\n                        <small>Select language for trigger</small>\r\n                      </a>\r\n                    </li>\r\n                    <li>\r\n                      <a [attr.href]=\"'#url'\">\r\n                        Use url<br />\r\n                        <small>Use URL for loading script</small>\r\n                      </a>\r\n                    </li>\r\n                    <li>\r\n                      <a [attr.href]=\"'#script'\">\r\n                        Script<br />\r\n                        <small>Trigger's script</small>\r\n                      </a>\r\n                    </li>\r\n                  </ul>\r\n\r\n                  <div style=\"height:100%\">\r\n                    <div id=\"language\" class=\"row\" style=\"margin-top: 100px;\">\r\n                      <div class=\"item form-group\">\r\n                        <label\r\n                                class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                for=\"languageSelect\"\r\n                                style=\"margin-top: 7px;\">\r\n                          Language <span class=\"required\">*</span>\r\n                        </label>\r\n                        <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                          <select class=\"form-control\" [(ngModel)]=\"activeWatcher.trigger.language\" id=\"languageSelect\">\r\n                            <option *ngFor=\"let language of triggerLanguages\" [ngValue]=\"language\">{{language}}</option>\r\n                          </select>\r\n                        </div>\r\n                      </div>\r\n                    </div>\r\n\r\n                    <div id=\"url\" class=\"row\" style=\"margin-top: 100px;\">\r\n                      <div class=\"item form-group\">\r\n                        <label\r\n                                class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                style=\"margin-top: 7px;\">\r\n                          Load script from external URL <span class=\"required\">*</span>\r\n                        </label>\r\n\r\n                        <div class=\"col-md-6 col-sm-6 col-xs-12\" >\r\n                          <ui-switch\r\n                                  [(ngModel)]=\"activeWatcher.trigger.isURL\"\r\n                                  [size]=\"'small'\">\r\n                          </ui-switch>\r\n                        </div>\r\n                      </div>\r\n                    </div>\r\n\r\n                    <div id=\"script\" class=\"row\" style=\"margin-top: 10px;\">\r\n                      <div *ngIf=\"activeWatcher.trigger.useUrl\">\r\n                        <div class=\"item form-group\">\r\n                          <label\r\n                                  class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                  for=\"urlForTrigger\"\r\n                                  style=\"margin-top: 7px;\">\r\n                            Url for script loading <span class=\"required\">*</span>\r\n                          </label>\r\n                          <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                            <input type=\"text\" id=\"urlForTrigger\" placeholder=\"Input url for script to be loaded from\" [(ngModel)]=\"activeWatcher.trigger.script\"/>\r\n                          </div>\r\n                        </div>\r\n                      </div>\r\n                      <div *ngIf=\"!activeWatcher.trigger.useUrl\">\r\n                        <div class=\"item form-group\">\r\n                          <label\r\n                                  class=\"control-label col-md-3 col-sm-3 col-xs-12\"\r\n                                  for=\"scriptBodyForTrigger\"\r\n                                  style=\"margin-top: 7px;\">\r\n                            Url for script loading <span class=\"required\">*</span>\r\n                          </label>\r\n                          <div class=\"col-md-6 col-sm-6 col-xs-12\">\r\n                            <textarea id=\"scriptBodyForTrigger\" [(ngModel)]=\"activeWatcher.trigger.script\"></textarea>\r\n                          </div>\r\n                        </div>\r\n                      </div>\r\n                      <div class=\"col-md-3\" style=\"float:right; margin-right: 20px;\">\r\n                        <button class=\"btn btn-primary\" (click)=\"saveCurrentTrigger()\">Save trigger</button>\r\n                      </div>\r\n                    </div>\r\n                  </div>\r\n                </div>\r\n              </div>\r\n            </div>\r\n          </div>\r\n        </div>\r\n      </div>\r\n\r\n      <panel [header]=\"getPanelHeader()\" [column]=\"'12'\">\r\n\r\n        <div class=\"row\">\r\n          <label class=\"control-label col-md-1 col-sm-1 col-xs-12\" for=\"componentSelection\" style=\"margin-top:10px;\">\r\n              Component <span class=\"required\">*</span>\r\n          </label>\r\n          <div class=\"col-md-3 col-sm-3 col-xs-12\">\r\n            <select\r\n                    id=\"componentSelection\"\r\n                    class=\"select2_group form-control\">\r\n              <optgroup label=\"Components\">\r\n                <option\r\n                        *ngFor=\"let component of getAvailableComponents()\"\r\n                        [value]=\"component\">\r\n                  {{component}}\r\n                </option>\r\n              </optgroup>\r\n            </select>\r\n          </div>\r\n          <div class=\"col-md-2 col-md-offset-1\">\r\n            <button class=\"btn btn-primary\"\r\n                    [attr.data-target]=\"'#editTriggerModal'\"\r\n                    data-toggle=\"modal\"\r\n                    aria-hidden=\"true\"> Edit trigger\r\n            </button>\r\n          </div>\r\n          <div class=\"col-md-2 col-md-offset-1\">\r\n            <button class=\"btn btn-primary\"\r\n                    [attr.data-target]=\"'#editCheckersModal'\"\r\n                    data-toggle=\"modal\"\r\n                    aria-hidden=\"true\"> Edit checkers\r\n            </button>\r\n          </div>\r\n        </div>\r\n\r\n        <div class=\"row\" style=\"margin-top: 20px;\">\r\n            <div class=\"col-md-4\">\r\n              <button class=\"btn btn-sm btn-primary\" (click)=\"saveActiveWatcher()\">Save</button>\r\n              <button class=\"btn btn-sm\" (click)=\"cleanSelection()\">Cancel/clean</button>\r\n            </div>\r\n        </div>\r\n\r\n      </panel>\r\n    </div>\r\n\r\n    <div class=\"row\" style=\"margin-top: 10px\">\r\n      <panel [header]=\"'List of watchers'\" [column]=\"'12'\">\r\n        <table class=\"table table-hover table-bordered\">\r\n          <thead class=\"thead-inverse\">\r\n          <tr>\r\n            <th>Actions</th>\r\n            <th>Name</th>\r\n            <th>Checkers</th>\r\n            <th>Trigger</th>\r\n          </tr>\r\n          </thead>\r\n          <tbody>\r\n          <tr *ngFor=\"let watcher of watchers\">\r\n            <td>\r\n              <button class=\"btn btn-primary btn-sm\" (click)=\"removeWatcher(watcher)\">Remove</button>\r\n              <br/>\r\n              <button class=\"btn btn-primary btn-sm\" (click)=\"editWatcher(watcher)\">Edit</button>\r\n            </td>\r\n            <th scope=\"row\">{{watcher.name}}</th>\r\n            <td>\r\n              <checkers [entity]=\"watcher.attributeCheckers\"></checkers>\r\n            </td>\r\n            <td>\r\n              <trigger [entity]=\"watcher.trigger\"></trigger>\r\n            </td>\r\n          </tr>\r\n          </tbody>\r\n        </table>\r\n\r\n      </panel>\r\n    </div>\r\n\r\n\r\n  </div>\r\n</div>\r\n\r\n"

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
var checkers_component_1 = __webpack_require__("./src/app/watchers/components/checkers.component.ts");
var trigger_component_1 = __webpack_require__("./src/app/watchers/components/trigger.component.ts");
var watchers_pipes_1 = __webpack_require__("./src/app/watchers/watchers.pipes.ts");
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
            declarations: [watchers_template_1.TemplateComponent, watchers_view_1.MainComponent, checkers_component_1.CheckersComponent, trigger_component_1.TriggerComponent, watchers_pipes_1.KeysPipe],
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
var app_restClient_1 = __webpack_require__("./src/app/app.restClient.ts");
var Observable_1 = __webpack_require__("./node_modules/rxjs/Observable.js");
var factory_1 = __webpack_require__("./src/app/watchers/model/factory.ts");
var watcher_1 = __webpack_require__("./src/app/watchers/model/watcher.ts");
var router_1 = __webpack_require__("./node_modules/@angular/router/index.js");
var attribute_1 = __webpack_require__("./src/app/charts/model/attribute.ts");
__webpack_require__("./node_modules/rxjs/add/operator/publishLast.js");
__webpack_require__("./node_modules/select2/dist/js/select2.js");
__webpack_require__("./node_modules/smartwizard/js/jquery.smartWizard.min.js");
var angular2_modal_1 = __webpack_require__("./node_modules/angular2-modal/esm/index.js");
var vex_1 = __webpack_require__("./node_modules/angular2-modal/plugins/vex/index.js");
var MainComponent = (function () {
    function MainComponent(apiClient, _router, modal, overlay, vcRef) {
        this._router = _router;
        this.modal = modal;
        this.components = [];
        this.watchers = [];
        this.activeWatcher = new watcher_1.Watcher(undefined, {});
        this.copyWatcher = undefined;
        this.isNewEntity = true;
        this.selectedComponent = undefined;
        this.selectedMetric = undefined;
        this.triggerLanguages = ["Groovy", "JavaScript"];
        this.http = apiClient;
        overlay.defaultViewContainer = vcRef;
    }
    MainComponent.prototype.saveCurrentTrigger = function () {
        console.log(this.activeWatcher);
    };
    MainComponent.prototype.ngOnInit = function () {
        var _this = this;
        // load the list of watchers
        this.http.get(app_restClient_1.REST.WATCHERS_LIST)
            .map(function (res) { return res.json(); })
            .subscribe(function (data) {
            _this.watchers = factory_1.Factory.watchersArrayFromJSON(data);
        });
        // find all the components
        this.http.get(app_restClient_1.REST.CHART_COMPONENTS)
            .map(function (res) { return res.json(); })
            .subscribe(function (data) {
            _this.components = data;
        });
        var _thisReference = this;
        // initialize select2 logic
        $(document).ready(function () {
            $("#componentSelection").select2();
            $("#componentSelection").on('change', function (e) {
                _thisReference.selectCurrentComponent($(e.target).val());
            });
        });
        this.initTriggerWizard();
        this.initCheckersWizard();
    };
    MainComponent.prototype.selectCurrentComponent = function (component) {
        this.selectedComponent = component;
        this.loadMetricsOnComponentSelected();
    };
    MainComponent.prototype.isTriggerAvailable = function () {
        return (this.activeWatcher != undefined && this.activeWatcher.trigger != undefined);
    };
    MainComponent.prototype.isCheckersAvailable = function () {
        return (this.activeWatcher != undefined
            && this.activeWatcher.attributeCheckers != undefined
            && !$.isEmptyObject(this.activeWatcher.attributeCheckers));
    };
    MainComponent.prototype.loadMetricsOnComponentSelected = function () {
        var _this = this;
        this.metrics = this.http.getIgnoreErrors(app_restClient_1.REST.CHART_METRICS_BY_COMPONENT(this.selectedComponent))
            .map(function (res) {
            var _data = res.json();
            var _values = [];
            for (var i in _data) {
                _values.push(new attribute_1.AttributeInformation(_data[i]));
            }
            return _values;
        }).catch(function (res) { return Observable_1.Observable.of([]); }).cache();
        // set auto selected first metric if the array is not empty
        this.metrics.subscribe(function (data) {
            if (data && data.length > 0) {
                _this.selectedMetric = data[0];
            }
        });
    };
    MainComponent.prototype.getAvailableComponents = function () {
        var _this = this;
        return this.components.filter(function (element) {
            var _available = true;
            for (var i = 0; i < _this.watchers.length; i++) {
                if (_this.watchers[i].name == element) {
                    _available = false;
                    break;
                }
            }
            return _available;
        });
    };
    MainComponent.prototype.cleanSelection = function () {
        for (var i = 0; i < this.watchers.length; i++) {
            if (this.watchers[i].guid == this.activeWatcher.guid) {
                this.watchers[i] = this.copyWatcher;
            }
        }
        this.activeWatcher = new watcher_1.Watcher(undefined, {});
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
                _this.http.delete(app_restClient_1.REST.WATCHER_BY_NAME(watcher.name))
                    .map(function (res) { return res.text(); })
                    .subscribe(function (data) {
                    console.log("watcher has been removed: ", data);
                });
                return response;
            })
                .catch(function () {
                console.log("user preferred to decline watcher removing");
            });
        });
    };
    MainComponent.prototype.editWatcher = function (watcher) {
        this.activeWatcher = watcher;
        this.isNewEntity = false;
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
    MainComponent.prototype.initTriggerWizard = function () {
        console.log($(this.getTriggerWizardId()));
        $(this.getTriggerWizardId()).smartWizard({
            theme: 'arrows',
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });
        var _thisReference = this;
        $(this.getTriggerWizardId()).on("showStep", function (e, anchorObject, stepNumber, stepDirection) {
            console.log(stepNumber);
        });
    };
    MainComponent.prototype.initCheckersWizard = function () {
        $(this.getCheckersWizardId()).smartWizard({
            theme: 'arrows',
            useURLhash: false,
            showStepURLhash: false,
            transitionEffect: 'fade'
        });
        var _thisReference = this;
        $(this.getCheckersWizardId()).on("showStep", function (e, anchorObject, stepNumber, stepDirection) {
            console.log(stepNumber);
        });
    };
    MainComponent = __decorate([
        core_1.Component({
            moduleId: module.i,
            template: __webpack_require__("./src/app/watchers/templates/main.html"),
            styles: [__webpack_require__("./src/app/watchers/templates/css/main.css")]
        }), 
        __metadata('design:paramtypes', [(typeof (_a = typeof app_restClient_1.ApiClient !== 'undefined' && app_restClient_1.ApiClient) === 'function' && _a) || Object, (typeof (_b = typeof router_1.Router !== 'undefined' && router_1.Router) === 'function' && _b) || Object, (typeof (_c = typeof vex_1.Modal !== 'undefined' && vex_1.Modal) === 'function' && _c) || Object, (typeof (_d = typeof angular2_modal_1.Overlay !== 'undefined' && angular2_modal_1.Overlay) === 'function' && _d) || Object, (typeof (_e = typeof core_1.ViewContainerRef !== 'undefined' && core_1.ViewContainerRef) === 'function' && _e) || Object])
    ], MainComponent);
    return MainComponent;
    var _a, _b, _c, _d, _e;
}());
exports.MainComponent = MainComponent;

/* WEBPACK VAR INJECTION */}.call(exports, __webpack_require__("./node_modules/jquery/dist/jquery.js")))

/***/ }

});
//# sourceMappingURL=1.map