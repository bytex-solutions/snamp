(function (com) {
    (function (snamp) {
        (function (models) {
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
            models.managmentTarget = managmentTarget;
        })(snamp.models || (snamp.models = {}));
        var models = snamp.models;
    })(com.snamp || (com.snamp = {}));
    var snamp = com.snamp;
})(exports.com || (exports.com = {}));
var com = exports.com;
