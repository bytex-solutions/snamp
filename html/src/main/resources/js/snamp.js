var bundles;
(function (bundles) {
    function getStringStatusRepresentation(status) {
        switch (status) {
            case 1:
                return "UNINSTALLED";
                break;
            case 2:
                return "INSTALLED";
                break;
            case 4:
                return "RESOLVED";
                break;
            case 8:
                return "STARTING";
                break;
            case 16:
                return "STOPPING";
                break;
            case 32:
                return "ACTIVE";
                break;
            default:
                return "UNKNOWN STATUS";
        }
    }
    var bundle = (function () {
        function bundle(data) {
            if (typeof data === "undefined") { data = null; }
            this.licensing = {};
            if (data == null)
                new bundle();
            else {
                this.version = data.hasOwnProperty("Version") ? data['Version'] : "";
                this.state = data.hasOwnProperty("State") ? getStringStatusRepresentation(data['State']) : "";
                this.name = data.hasOwnProperty("DisplayName") ? data['DisplayName'] : "";
                this.description = data.hasOwnProperty("Description") ? data['Description'] : "";
                if (data.hasOwnProperty("Licensing")) {
                    var licensing_ = data['Licensing'];
                    if (licensing_ instanceof Object) {
                        this.licensing = licensing_;
                    }
                }
            }
        }
        bundle.prototype.getLicenseAsAString = function () {
            var res_string = "";
            if (this.licensing != null) {
                for (var key in this.licensing) {
                    res_string += key + " : " + this.licensing[key] + ";\n";
                }
                return res_string;
            } else
                return "";
        };
        return bundle;
    })();
    bundles.bundle = bundle;
})(bundles || (bundles = {}));
var bunstatus;
(function (bunstatus) {
    var bundleStatus = (function () {
        function bundleStatus(curCount, maxCount, targets) {
            if (typeof curCount === "undefined") { curCount = 0; }
            if (typeof maxCount === "undefined") { maxCount = 0; }
            if (typeof targets === "undefined") { targets = null; }
            this.curCount = curCount;
            this.maxCount = maxCount;
            this.currentInstanceCount = curCount;
            this.maxInstanceCount = maxCount;
            this.managmentTargets = targets;
        }
        return bundleStatus;
    })();
    bunstatus.bundleStatus = bundleStatus;
})(bunstatus || (bunstatus = {}));
var config;
(function (config) {
    var manRes = (function () {
        function manRes(name, connectionString, connectionType, additionalProperties, attributes, events) {
            if (typeof name === "undefined") { name = ""; }
            if (typeof connectionString === "undefined") { connectionString = ""; }
            if (typeof connectionType === "undefined") { connectionType = ""; }
            if (typeof additionalProperties === "undefined") { additionalProperties = null; }
            if (typeof attributes === "undefined") { attributes = null; }
            if (typeof events === "undefined") { events = null; }
            this.additionalProperties = {};
            this.name = name;
            this.connectionString = connectionString;
            this.connectionType = connectionType;
            this.additionalProperties = additionalProperties;
            this.attributes = attributes;
            this.events = events;
        }
        return manRes;
    })();
    config.manRes = manRes;

    var attributeAdditionalParamsRestriction = (function () {
        function attributeAdditionalParamsRestriction(association, exclusion, extension, defaultValue, description, inputPattern, required) {
            if (typeof association === "undefined") { association = null; }
            if (typeof exclusion === "undefined") { exclusion = null; }
            if (typeof extension === "undefined") { extension = null; }
            if (typeof defaultValue === "undefined") { defaultValue = ""; }
            if (typeof description === "undefined") { description = ""; }
            if (typeof inputPattern === "undefined") { inputPattern = ""; }
            if (typeof required === "undefined") { required = false; }
            this.association = [];
            this.exclusion = [];
            this.extension = [];
            this.defaultValue = "";
            this.description = "";
            this.inputPattern = "";
            this.required = false;
            this.association = association;
            this.exclusion = exclusion;
            this.extension = extension;
            this.defaultValue = defaultValue;
            this.description = description;
            this.inputPattern = inputPattern;
            this.required = required;
        }
        return attributeAdditionalParamsRestriction;
    })();
    config.attributeAdditionalParamsRestriction = attributeAdditionalParamsRestriction;

    var attributeAdditionalParam = (function () {
        function attributeAdditionalParam(paramName, paramRestriction, paramValue) {
            if (typeof paramName === "undefined") { paramName = ""; }
            if (typeof paramRestriction === "undefined") { paramRestriction = null; }
            if (typeof paramValue === "undefined") { paramValue = ""; }
            this.paramName = "";
            this.paramRestriction = null;
            this.paramValue = "";
            this.paramName = paramName;
            this.paramRestriction = paramRestriction;
            this.paramValue = paramValue;
        }
        return attributeAdditionalParam;
    })();
    config.attributeAdditionalParam = attributeAdditionalParam;

    var attribute = (function () {
        function attribute(attributeId, attributeName, readWriteTimeout, additionalElements) {
            if (typeof attributeId === "undefined") { attributeId = ""; }
            if (typeof attributeName === "undefined") { attributeName = ""; }
            if (typeof readWriteTimeout === "undefined") { readWriteTimeout = -1; }
            if (typeof additionalElements === "undefined") { additionalElements = null; }
            this.attributeId = "";
            this.attributeName = "";
            this.readWriteTimeout = -1;
            this.additionalElements = [];
            this.additionalElements = additionalElements;
            this.attributeName = attributeName;
            this.attributeId = attributeId;
            this.readWriteTimeout = readWriteTimeout;
        }
        return attribute;
    })();
    config.attribute = attribute;

    var event = (function () {
        function event(category, parameters) {
            if (typeof category === "undefined") { category = ""; }
            if (typeof parameters === "undefined") { parameters = null; }
            this.parameters = {};
            this.category = category;
            this.parameters = parameters;
        }
        return event;
    })();
    config.event = event;

    var resAdapters = (function () {
        function resAdapters(adapterName, hostingParams) {
            if (typeof adapterName === "undefined") { adapterName = ""; }
            if (typeof hostingParams === "undefined") { hostingParams = null; }
            this.hostingParams = {};
            this.adapterName = adapterName;
            this.hostingParams = hostingParams;
        }
        return resAdapters;
    })();
    config.resAdapters = resAdapters;

    var configuration = (function () {
        function configuration(data) {
            if (typeof data === "undefined") { data = null; }
            if (data == null || (!data.hasOwnProperty("managedResources") && !data.hasOwnProperty("resourceAdapters")))
                this.constructor(null, null);
            else {
                this.managedResources = data.hasOwnProperty("managedResources") ? this.parseJsonToManagedResources(data['managedResources']) : [];
                this.resourceAdapters = data.hasOwnProperty("resourceAdapters") ? this.parseJsonToResourceAdapters(data['resourceAdapters']) : [];
            }
        }
        configuration.prototype.parseJsonToManagedResources = function (data) {
            if (typeof data === "undefined") { data = null; }
            var result = [];
            for (var propertyName in data) {
                var local = data[propertyName];
                if (!local.hasOwnProperty("connectionType"))
                    continue;

                var restrictions = {};
                $.ajax({
                    url: "/snamp/management/api/connectors/" + local['connectionType'].toLowerCase() + "/configurationSchema",
                    dataType: "json",
                    cache: false,
                    type: "GET",
                    async: false,
                    success: function (scheme) {
                        if (scheme.hasOwnProperty("attributeParameters")) {
                            for (var restrictionName in scheme['attributeParameters']) {
                                var atrRes = new attributeAdditionalParamsRestriction(scheme['attributeParameters'][restrictionName]['ASSOCIATION'], scheme['attributeParameters'][restrictionName]['EXCLUSION'], scheme['attributeParameters'][restrictionName]['EXTENSION'], scheme['attributeParameters'][restrictionName]['defaultValue'], scheme['attributeParameters'][restrictionName]['description'], scheme['attributeParameters'][restrictionName]['inputPattern'], scheme['attributeParameters'][restrictionName]['required']);
                                restrictions[restrictionName] = atrRes;
                            }
                        }
                    }
                });

                var attributes = [];
                for (var attributeId in local['attributes']) {
                    var attrParam = [];
                    for (var paramName in local['attributes'][attributeId]['additionalProperties']) {
                        var locAttrParam = new attributeAdditionalParam(paramName, restrictions[paramName], local['attributes'][attributeId]['additionalProperties'][paramName]);
                        attrParam.push(locAttrParam);
                    }

                    var locAtr = new attribute(attributeId, local['attributes'][attributeId]['name'], local['attributes'][attributeId]['readWriteTimeout'], attrParam);

                    attributes.push(locAtr);
                }

                var currentConnector = new manRes(propertyName, local['connectionString'], local['connectionType'], local['additionalProperties'], attributes, local['events']);

                result.push(currentConnector);
            }
            return result;
        };

        configuration.prototype.parseJsonToResourceAdapters = function (data) {
            if (typeof data === "undefined") { data = null; }
            return [];
        };
        return configuration;
    })();
    config.configuration = configuration;
})(config || (config = {}));

(function ($) {
    $.fn.representConfigAsJsTreeJson = function (configuration) {
        var result = [];
        result.push(new jsTreeHelper.jsonFormat("managedResources", "#", "connectors", "glyphicon glyphicon-resize-small"));
        result.push(new jsTreeHelper.jsonFormat("resourceAdapters", "#", "adapters", "glyphicon glyphicon-resize-full"));
        result.push();
        return result;
    };

    $.fn.createConfigurations = function (opts) {
        if (typeof opts === "undefined") { opts = null; }
        var data = null;

        if (opts != null && opts.useStub) {
            data = stubs.getConfiguration();
        } else {
            $.ajax({
                url: "/snamp/management/api/configuration",
                dataType: "json",
                cache: false,
                type: "GET",
                async: false,
                success: function (json) {
                    if (json instanceof Object)
                        data = new config.configuration(json);
                }
            });
        }
        $(this).jstree({ 'core': {
                'data': $(this).representConfigAsJsTreeJson(data)
            } });
    };
})(jQuery);
var jsTreeHelper;
(function (jsTreeHelper) {
    var jsonFormat = (function () {
        function jsonFormat(id, parent, text, icon, state, li_attr, a_attr) {
            if (typeof id === "undefined") { id = "#"; }
            if (typeof parent === "undefined") { parent = "#"; }
            if (typeof text === "undefined") { text = ""; }
            if (typeof icon === "undefined") { icon = ""; }
            if (typeof state === "undefined") { state = new stateJson; }
            if (typeof li_attr === "undefined") { li_attr = {}; }
            if (typeof a_attr === "undefined") { a_attr = {}; }
            this.id = "#";
            this.parent = "#";
            this.text = "";
            this.icon = "";
            this.state = new stateJson;
            this.id = id;
            this.parent = parent;
            this.text = text;
            this.icon = icon;
            this.state = state;
            this.li_attr = li_attr;
            this.a_attr = a_attr;
        }
        return jsonFormat;
    })();
    jsTreeHelper.jsonFormat = jsonFormat;

    var stateJson = (function () {
        function stateJson() {
            this.opened = true;
            this.disabled = false;
            this.selected = false;
        }
        return stateJson;
    })();
    jsTreeHelper.stateJson = stateJson;
})(jsTreeHelper || (jsTreeHelper = {}));
var license;
(function (license) {
    var licenseInfo = (function () {
        function licenseInfo(descr) {
            if (typeof descr === "undefined") { descr = "There's no available description"; }
            this.descr = descr;
            this.description = descr;
        }
        return licenseInfo;
    })();
    license.licenseInfo = licenseInfo;
})(license || (license = {}));

(function ($) {
})(jQuery);

(function ($) {
    $.fn.createLoaderTable = function (opts) {
        if (typeof opts === "undefined") { opts = null; }
        var table = $("<table>", { class: "table" });
        table.append("<thead><tr><th>State</th><th>Version</th><th>Bundle Name</th><th>Description</th><th>Licensing</th></tr></thead>");

        var data = [];

        if (opts != null && opts.useStub) {
            data = stubs.getSummary();
        } else {
            $.ajax({
                url: "/snamp/management/api/components",
                dataType: "json",
                cache: false,
                type: "GET",
                async: false,
                success: function (json) {
                    if (json instanceof Object && json.length > 0)
                        for (var obj in json) {
                            data.push(new bundles.bundle(json[obj]));
                        }
                }
            });
        }
        var tbody = $("<tbody></tbody>");

        for (var i = data.length - 1; i >= 0; i--) {
            var content;
            var tr = $("<tr></tr>");
            content = data[i].state;
            tr.append("<td>" + content + "</td>");

            content = data[i].version;
            tr.append("<td>" + content + "</td>");

            content = data[i].name;
            tr.append("<td>" + content + "</td>");

            content = data[i].description;
            tr.append("<td>" + content + "</td>");

            content = data[i].getLicenseAsAString();
            tr.append("<td>" + content + "</td>");

            tbody.append(tr);
        }
        ;

        table.append(tbody);

        table.appendTo(this);
    };
})(jQuery);
var target;
(function (target) {
    var managmentTarget = (function () {
        function managmentTarget(connectionString, events, attributes) {
            if (typeof connectionString === "undefined") { connectionString = ""; }
            if (typeof events === "undefined") { events = 0; }
            if (typeof attributes === "undefined") { attributes = 0; }
            this.connectionString = connectionString;
            this.events = events;
            this.attributes = attributes;
        }
        return managmentTarget;
    })();
    target.managmentTarget = managmentTarget;
})(target || (target = {}));
var options = (function () {
    function options(usestub) {
        if (typeof usestub === "undefined") { usestub = true; }
        this._useStub = usestub;
    }
    Object.defineProperty(options.prototype, "useStub", {
        get: function () {
            return this._useStub;
        },
        set: function (value) {
            this._useStub = value;
        },
        enumerable: true,
        configurable: true
    });

    return options;
})();
var stubs;
(function (stubs) {
    function getSummary() {
        return [];
    }
    stubs.getSummary = getSummary;

    function getConfiguration() {
        return null;
    }
    stubs.getConfiguration = getConfiguration;
})(stubs || (stubs = {}));

(function ($) {
})(jQuery);
