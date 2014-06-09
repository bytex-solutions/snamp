/*
This class contains specification for describing Bundle model
*/
var bundles;
(function (bundles) {
    var bundle = (function () {
        function bundle(active, type, name, description, statusCurrent, licenseInfo) {
            if (typeof active === "undefined") { active = false; }
            if (typeof type === "undefined") { type = ""; }
            if (typeof name === "undefined") { name = ""; }
            if (typeof description === "undefined") { description = ""; }
            if (typeof statusCurrent === "undefined") { statusCurrent = null; }
            if (typeof licenseInfo === "undefined") { licenseInfo = null; }
            this.active = active;
            this.type = type;
            this.name = name;
            this.description = description;
            this.status = statusCurrent;
            this.licenseInfo = licenseInfo;
        }
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
        return [
            new bundles.bundle(true, "connector", "JMX connector", "Provides information about remote or local JMX bean " + "according to configuration file propertiess", new bunstatus.bundleStatus(5, 10, [
                new target.managmentTarget("service:jmx:rmi:///jndi/rmi://10.200.100.101:23400/jmxrmi", 5, 2),
                new target.managmentTarget("service:jmx:rmi:///jndi/rmi://10.200.100.102:23400/jmxrmi", 1, 4),
                new target.managmentTarget("service:jmx:rmi:///jndi/rmi://10.200.100.103:23400/jmxrmi", 2, 0),
                new target.managmentTarget("service:jmx:rmi:///jndi/rmi://10.200.100.104:23400/jmxrmi", 1, 10),
                new target.managmentTarget("service:jmx:rmi:///jndi/rmi://10.200.100.105:23400/jmxrmi", 0, 5)
            ]), new license.licenseInfo("Full license, includes up to 5 instances and 1 year of support subscribe (excluding on-demand development of additional features)")),
            new bundles.bundle(true, "adapter", "SNMP adapter", "Provides SNMP OIDs from any source", new bunstatus.bundleStatus(1, 1, [
                new target.managmentTarget("localhost:3122", 1, 10)
            ]), new license.licenseInfo("Full license, includes infinite count of instances and 2 years of full support")),
            new bundles.bundle(true, "connector", "WMB connector", "Links to Websphere Message Broker and returns some information from there", new bunstatus.bundleStatus(1, 1, [
                new target.managmentTarget("wmb://anticitizen.dhis.org:8000/TEST_QMGR", 1, 1)
            ]), new license.licenseInfo("Trial license, expires 5 days, includes 1 instance of connector"))
        ];
    }
    stubs.getSummary = getSummary;
})(stubs || (stubs = {}));
/// <reference path="types/jquery.d.ts" />
/// <reference path="options.ts" />
var _this = this;
(function ($) {
    $.fn.createSummaryTable = function (opts) {
        if (typeof opts === "undefined") { opts = null; }
        var commonData;

        if (opts != null && opts.useStub) {
            commonData = stubs.getSummary();
        } else {
            // ajax-rest loader for bundleInfo
        }

        var panelGroup = $("<div class=\"panel-group\"> id=\"accordion\"");

        for (var j = commonData.length - 1; j >= 0; j--) {
            var data = commonData[j].status;

            // Now make a table per bundle
            if (data != null) {
                var headAccordeonTab = $("<div>", { class: "panel panel-default" });
                headAccordeonTab.append("<div class=\"panel-heading\">" + "<h4 class=\"panel-title\">" + "<a data-toggle=\"collapse\" data-parent=\"#accordion\" href=\"#idd" + j + "\">" + commonData[j].name + ". Instances (current/max): " + data.currentInstanceCount + "\\" + data.maxInstanceCount + "</a></h4></div>");

                panelGroup.append(headAccordeonTab);

                if (data.managmentTargets != null) {
                    if (data.managmentTargets.length > 0) {
                        var _class = "panel-collapse collapse in";

                        //  if (j != commonData.length - 1) _class = _class + " in";
                        var divBodyAccordeonTab = $("<div>", { id: "idd" + j, class: _class });
                        var divPanelBody = $("<div>", { class: "panel-body" });

                        var table = $("<table>", { class: "table" });
                        table.append("<thead><tr><th>Connection String</th><th>Events</th><th>attributes</th></tr></thead>");
                        var tbody = $("<tbody></tbody>");

                        for (var i = data.managmentTargets.length - 1; i >= 0; i--) {
                            var tr = $("<tr></tr>");
                            var content = data.managmentTargets[i].connectionString;
                            tr.append("<td>" + content + "</td>");

                            content = data.managmentTargets[i].events.toString();
                            tr.append("<td>" + content + "</td>");

                            content = data.managmentTargets[i].attributes.toString();
                            tr.append("<td>" + content + "</td>");

                            tbody.append(tr);
                        }
                        ;
                        table.append(tbody);

                        divPanelBody.append(table);

                        divBodyAccordeonTab.append(divPanelBody);
                    }
                }

                headAccordeonTab.append(divBodyAccordeonTab);
            }
        }
        ;
        $(_this).append(panelGroup);

        return _this;
    };
})(jQuery);
