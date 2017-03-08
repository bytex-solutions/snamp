webpackJsonpac__name_([1],{

/***/ "./node_modules/css-loader/index.js!./src/app/watchers/templates/css/main.css":
/***/ function(module, exports, __webpack_require__) {

exports = module.exports = __webpack_require__("./node_modules/css-loader/lib/css-base.js")();
// imports


// module
exports.push([module.i, "", ""]);

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
    }
    __decorate([
        core_1.Input(), 
        __metadata('design:type', Object)
    ], CheckersComponent.prototype, "entity", void 0);
    CheckersComponent = __decorate([
        core_1.Component({
            moduleId: module.i,
            selector: 'checkers',
            template: __webpack_require__("./src/app/watchers/components/templates/checkers.html"),
            styles: ['.flatbar { width: 100% !important; text-align: left !important; margin-left: -15px !important; }']
        }), 
        __metadata('design:paramtypes', [])
    ], CheckersComponent);
    return CheckersComponent;
}());
exports.CheckersComponent = CheckersComponent;


/***/ },

/***/ "./src/app/watchers/components/templates/checkers.html":
/***/ function(module, exports) {

module.exports = "<div>\r\n    <dl class=\"row\">\r\n        <dt class=\"col-sm-3\">Description lists</dt>\r\n        <dd class=\"col-sm-9\">A description list is perfect for defining terms.</dd>\r\n\r\n        <dt class=\"col-sm-3\">Euismod</dt>\r\n        <dd class=\"col-sm-9\">Vestibulum id ligula porta felis euismod semper eget lacinia odio sem nec elit.</dd>\r\n        <dd class=\"col-sm-9 offset-sm-3\">Donec id elit non mi porta gravida at eget metus.</dd>\r\n\r\n        <dt class=\"col-sm-3\">Malesuada porta</dt>\r\n        <dd class=\"col-sm-9\">Etiam porta sem malesuada magna mollis euismod.</dd>\r\n\r\n        <dt class=\"col-sm-3 text-truncate\">Truncated term is truncated</dt>\r\n        <dd class=\"col-sm-9\">Fusce dapibus, tellus ac cursus commodo, tortor mauris condimentum nibh, ut fermentum massa justo sit amet risus.</dd>\r\n\r\n    </dl>\r\n</div>"

/***/ },

/***/ "./src/app/watchers/components/templates/trigger.html":
/***/ function(module, exports) {

module.exports = "<div>\r\n    <dl class=\"row\">\r\n        <dt class=\"col-sm-3\">Language:</dt>\r\n        <dd class=\"col-sm-9\">{{entity.language}}</dd>\r\n\r\n        <dt class=\"col-sm-3\">Is url:</dt>\r\n        <dd class=\"col-sm-9\">{{entity.isURL}}</dd>\r\n\r\n        <dt class=\"col-sm-3\">Script:</dt>\r\n        <dd class=\"col-sm-9\" [tooltip]=\"entity.script\">{{entity.shortScript()}}</dd>\r\n    </dl>\r\n</div>"

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
    }
    __decorate([
        core_1.Input(), 
        __metadata('design:type', (typeof (_a = typeof scriptlet_data_object_1.ScriptletDataObject !== 'undefined' && scriptlet_data_object_1.ScriptletDataObject) === 'function' && _a) || Object)
    ], TriggerComponent.prototype, "entity", void 0);
    TriggerComponent = __decorate([
        core_1.Component({
            moduleId: module.i,
            selector: 'trigger',
            template: __webpack_require__("./src/app/watchers/components/templates/trigger.html"),
            styles: ['.flatbar { width: 100% !important; text-align: left !important; margin-left: -15px !important; }']
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
var colored_checker_1 = __webpack_require__("./src/app/watchers/model/colored.checker.ts");
var ScriptletDataObject = (function () {
    function ScriptletDataObject() {
        this.language = "n/a";
        this.script = "empty";
        this.isURL = false;
        this.object = undefined;
    }
    ScriptletDataObject.prototype.shortScript = function () {
        return ((this.script.length > 30) ? this.script.substring(0, 30) : this.script);
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
var Watcher = (function (_super) {
    __extends(Watcher, _super);
    function Watcher() {
        _super.apply(this, arguments);
        this.attributeCheckers = {};
        this.trigger = undefined;
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

module.exports = "<div class=\"right_col\" role=\"main\" style=\"min-height: 949px;\">\r\n  <div class=\"\">\r\n    <div class=\"page-title\">\r\n      <div class=\"title_left\">\r\n        <h3>Setup watchers</h3>\r\n      </div>\r\n    </div>\r\n\r\n    <div class=\"clearfix\"></div>\r\n\r\n    <div class=\"row\" style=\"margin-top: 30px\">\r\n\r\n      <panel [header]=\"'List of watchers'\" [column]=\"'12'\">\r\n        <table class=\"table\">\r\n          <thead class=\"thead-inverse\">\r\n          <tr>\r\n            <th>Name</th>\r\n            <th>Trigger</th>\r\n            <th>Checkers</th>\r\n          </tr>\r\n          </thead>\r\n          <tbody>\r\n          <tr *ngFor=\"let watcher of watchers\">\r\n            <th scope=\"row\">{{watcher.name}}</th>\r\n            <td>\r\n              <checkers [entity]=\"watcher.attributeCheckers\"></checkers>\r\n            </td>\r\n            <td>\r\n              <trigger [entity]=\"watcher.trigger\"></trigger>\r\n            </td>\r\n          </tr>\r\n          </tbody>\r\n        </table>\r\n\r\n      </panel>\r\n    </div>\r\n  </div>\r\n</div>\r\n\r\n"

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
            declarations: [watchers_template_1.TemplateComponent, watchers_view_1.MainComponent, checkers_component_1.CheckersComponent, trigger_component_1.TriggerComponent],
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
var factory_1 = __webpack_require__("./src/app/watchers/model/factory.ts");
var router_1 = __webpack_require__("./node_modules/@angular/router/index.js");
__webpack_require__("./node_modules/rxjs/add/operator/publishLast.js");
var MainComponent = (function () {
    function MainComponent(apiClient, _router) {
        this._router = _router;
        this.watchers = [];
        this.http = apiClient;
    }
    MainComponent.prototype.ngOnInit = function () {
        var _this = this;
        this.http.get(app_restClient_1.REST.WATCHERS_LIST)
            .map(function (res) { return res.json(); })
            .subscribe(function (data) {
            _this.watchers = factory_1.Factory.watchersArrayFromJSON(data);
            console.log("Watchers list is: ", data, _this.watchers);
        });
        this.components = this.http.get(app_restClient_1.REST.CHART_COMPONENTS)
            .map(function (res) { return res.json(); })
            .publishLast().refCount();
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
//# sourceMappingURL=1.map