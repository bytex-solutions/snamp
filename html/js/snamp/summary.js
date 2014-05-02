(function( $ ){

  $.fn.createSummaryTable = function( options ) {  
        
    // Making data request
    var commonData;
    if (options.stub == true)
    {
        commonData = this.getBundleInfo();
    }
    else
    {
      // ajax code here
    }

   
    var panelGroup = $("<div class=\"panel-group\"> id=\"accordion\"");

    for (var j = commonData.length - 1; j >= 0; j--) {
        var data;
        if (options.stub == true)
        {
            data = this.getBundleStatusByName(commonData[j].name);
        }
        else
        {
          // ajax code here
        }
        

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
                    content = data.managmentTargets[i].connectionString;
                    tr.append("<td>" + content + "</td>");

                    content = data.managmentTargets[i].events;
                    tr.append("<td>" + content + "</td>");

                    content = data.managmentTargets[i].attributes;
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
    this.append(panelGroup);
    this.find(".collapse").collapse();
  
  };

})( jQuery );