/// <reference path="types/jquery.d.ts" />
interface JQuery {
    createConfigurations(opts: options): JQuery;
}


(function ($)  {
    $.fn.createConfigurations = function (opts:options=null) {
        var data = [];

        if (opts != null && opts.useStub)
        {
            data = stubs.getConfiguration();
        }
        else
        {
            $.ajax({
                url: "/snamp/management/api/configuration",
                dataType: "json",
                cache: false,
                type:  "GET",
                async: false,
                success: function(json)
                {
                    if (json instanceof Object)
                        console.log(new config.configuration(json));
                }
            });
        }
    };
})( jQuery );