/// <reference path="types/jquery.d.ts" />
interface JQuery {
    createSummaryTable(opts: options): JQuery;
}


(function ($)  {
   /* $.fn.createSummaryTable = function (opts:options=null) {
        var commonData:bundles.bundle[];

        if (opts != null && opts.useStub)
        {
            commonData = stubs.getSummary();
        }
        else
        {

        }

        var panelGroup = $("<div class=\"panel-group\"> id=\"accordion\"");

        for (var j = commonData.length - 1; j >= 0; j--) {
            var data = commonData[j].status;



            // Now make a table per bundle
            if (data != null)
            {

                var headAccordeonTab = $("<div>", {class: "panel panel-default"});
                headAccordeonTab.append("<div class=\"panel-heading\">"+
                    "<h4 class=\"panel-title\">"+
                    "<a data-toggle=\"collapse\" data-parent=\"#accordion\" href=\"#idd"+j+"\">"
                    +commonData[j].name+". Instances (current/max): " +
                    data.currentInstanceCount + "\\" + data.maxInstanceCount + "</a></h4></div>")

                panelGroup.append(headAccordeonTab);



                if (data.managmentTargets != null)
                {
                    if (data.managmentTargets.length > 0)
                    {
                        var _class = "panel-collapse collapse in";
                        //  if (j != commonData.length - 1) _class = _class + " in";
                        var divBodyAccordeonTab = $("<div>", {id: "idd"+j, class:_class});
                        var divPanelBody = $("<div>", {class: "panel-body"}) ;

                        var table = $("<table>", {class:"table"});
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
                        };
                        table.append(tbody);

                        divPanelBody.append(table);

                        divBodyAccordeonTab.append(divPanelBody);
                    }
                }

                headAccordeonTab.append(divBodyAccordeonTab);
            }
        };
        $(this).append(panelGroup);

        return this;
    };*/

})(jQuery);