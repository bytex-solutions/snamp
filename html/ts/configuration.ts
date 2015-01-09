/// <reference path="types/jquery.d.ts" />
interface JQuery {
    createConfigurations(opts: options): JQuery;
    representConfigAsJsTreeJson(configuration:config.configuration):any;
}




(function ($)  {
    $.fn.representConfigAsJsTreeJson = function(configuration:config.configuration)
    {
        var result = [];
        result.push(new jsTreeHelper.jsonFormat("managedResources","#", "connectors", "glyphicon glyphicon-resize-small"));
        result.push(new jsTreeHelper.jsonFormat("resourceAdapters","#", "adapters", "glyphicon glyphicon-resize-full"));
        configuration.managedResources.forEach(currentResource  => {
            result.push(new jsTreeHelper.jsonFormat(currentResource.name, "managedResources",   currentResource.name, "glyphicon glyphicon-log-out"));
            result.push(new jsTreeHelper.jsonFormat(currentResource.name + "_connectionString", currentResource.name, currentResource.connectionString));
            result.push(new jsTreeHelper.jsonFormat(currentResource.name + "_type",             currentResource.name, currentResource.connectionType));
        });
        return result;
    };


    $.fn.createConfigurations = function (opts:options=null) {
        var data:config.configuration = null;

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
                        data = new config.configuration(json);
                }
            });
        }
        $(this).jstree({ 'core' : {
            'data' : $(this).representConfigAsJsTreeJson(data)
        } });

    };
})( jQuery );