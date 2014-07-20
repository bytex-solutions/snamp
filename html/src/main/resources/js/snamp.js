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
})(stubs || (stubs = {}));

(function ($) {
})(jQuery);
