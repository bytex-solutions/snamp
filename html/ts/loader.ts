/// <reference path="types/jquery.d.ts" />
interface JQuery {
    createLoaderTable(opts: options): JQuery;
    addOperations(UUID, status ):void;
}


(function ($)  {
    $.fn.createLoaderTable = function (opts:options=null) {
        // Making the header part of table
        var table = $("<table>", {class:"table"});
        table.append("<thead><tr><th>State</th><th>Version</th><th>Bundle Name</th><th>Description</th><th>Licensing</th><th width=\"15%\">Operations</th></tr></thead>");


        var data = [];

        if (opts != null && opts.useStub)
        {
            data = stubs.getSummary();
        }
        else
        {
            $.ajax({
                url: "/snamp/management/api/components",
                dataType: "json",
                cache: false,
                type:  "GET",
                async: false,
                success: function(json)
                {
                    if (json instanceof Object && json.length>0)
                        for (var obj in json)
                        {
                            data.push(new bundles.bundle(json[obj]));
                        }
                }
            });
        }
        var tbody = $("<tbody></tbody>");
        // filling the body of table
        for (var i = data.length - 1; i >= 0; i--) {
            var content;
            var tr = $("<tr></tr>");
            content =  data[i].state;
            tr.append("<td>" + content + "</td>");

            content = data[i].version;
            tr.append("<td>" + content + "</td>");

            content = data[i].name;
            tr.append("<td>" + content + "</td>");

            content = data[i].description;
            tr.append("<td>" + content + "</td>");

            content = data[i].getLicenseAsAString();
            tr.append("<td>" + content + "</td>");

            var td = $("<td></td>");
            td.appendTo(tr);
            td.addOperations(data[i].name, data[i].state);

            tbody.append(tr);
        };

        table.append(tbody);

        // Appending the table to the element
        table.appendTo(this);
    };

    $.fn.addOperations = function( UUID, status )
    {
        var btnGrp = $("<div>", { class: "btn-group" });

        // start button
        var btnStart = $("<button>", { type: "button", class: "btn btn-default btn-xs"} );
        if (status == "ACTIVE") btnStart.attr("disabled", "disabled");
        var span = $("<span>", { class : "glyphicon glyphicon-play"});
        span.appendTo(btnStart);
        btnStart.appendTo(btnGrp);

        // stop button
        var btnStop = $("<button>", { type: "button", class: "btn btn-default btn-xs"} );
        if (status != "ACTIVE") btnStop.attr("disabled", "disabled");
        var span = $("<span>", { class : "glyphicon glyphicon-stop"});
        span.appendTo(btnStop);
        btnStop.appendTo(btnGrp);

        // refresh button
        var btnRefresh = $("<button>", { type: "button", class: "btn btn-default btn-xs"} );
        var span = $("<span>", { class : "glyphicon glyphicon-refresh"});
        span.appendTo(btnRefresh);
        btnRefresh.appendTo(btnGrp);

        // remove button
        var btnRemove = $("<button>", { type: "button", class: "btn btn-default btn-xs"} );
        var span = $("<span>", { class : "glyphicon glyphicon-remove"});
        span.appendTo(btnRemove);
        btnRemove.appendTo(btnGrp);

        this.append(btnGrp);
    };

})( jQuery );