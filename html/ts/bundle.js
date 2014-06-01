(function (com) {
    (function (snamp) {
        /*
        This class contains specification for describing Bundle model
        */
        (function (models) {
            var bundle = (function () {
                function bundle(active, type, name, description, status, licenseInfo) {
                    if (typeof active === "undefined") { active = false; }
                    if (typeof type === "undefined") { type = ""; }
                    if (typeof name === "undefined") { name = ""; }
                    if (typeof description === "undefined") { description = ""; }
                    if (typeof status === "undefined") { status = null; }
                    if (typeof licenseInfo === "undefined") { licenseInfo = null; }
                    this.active = active;
                    this.type = type;
                    this.name = name;
                    this.description = description;
                    this.status = bundleStatus;
                    this.licenseInfo = licenseInfo;
                }
                return bundle;
            })();
            models.bundle = bundle;
        })(snamp.models || (snamp.models = {}));
        var models = snamp.models;
    })(com.snamp || (com.snamp = {}));
    var snamp = com.snamp;
})(exports.com || (exports.com = {}));
var com = exports.com;
