/// <reference path="types/jquery.d.ts" />
interface JQuery {
    getLicenseInfo(opts: options): JQuery;
}


(function ($)  {
/*    $.fn.getLicenseInfo = function (opts:options=null) {
        var commonData:bundles.bundle[];

        if (opts != null && opts.useStub) {
            commonData = stubs.getSummary();
        }
        else {
            // ajax-rest loader for bundleInfo
        }
        for (var i = commonData.length - 1; i >= 0; i--) {
            var data = commonData[i].licenseInfo;

            // Now make a table per bundle
            if (data != null) {
                this.append("<h3>" + commonData[i].name + "</h3>");
                this.append("<div class=\"well\">" + data.description + "</div>");
            }
        }
    }*/

})( jQuery );