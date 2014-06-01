(function (com) {
    (function (snamp) {
        (function (models) {
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
            models.bundleStatus = bundleStatus;
        })(snamp.models || (snamp.models = {}));
        var models = snamp.models;
    })(com.snamp || (com.snamp = {}));
    var snamp = com.snamp;
})(exports.com || (exports.com = {}));
var com = exports.com;
