(function( $ ){

  $.fn.createTable = function( options ) {  
    // Making the header part of table
    var table = $("<table>", {class:"table"});
    table.append("<thead><tr><th>Status</th><th>Type</th><th>Bundle Name</th><th>Description</th><th width=\"15%\">Operations</th></tr></thead>");

    
    // Making data request
    var data;
    if (options.stub == true)
    {
        data = this.getBundleInfo();
    }
    else
    {
      // ajax code here
    }
    
    var tbody = $("<tbody></tbody>");
    // filling the body of table
    for (var i = data.length - 1; i >= 0; i--) {
      var tr = $("<tr></tr>");
      content = "<img src=\"" + data[i].status + ".png" +"\"/>";
      tr.append("<td>" + content + "</td>");

      content = data[i].type;
      tr.append("<td>" + content + "</td>");

      content = data[i].name;
      tr.append("<td>" + content + "</td>");

      content = data[i].description;
      tr.append("<td>" + content + "</td>");

      var td = $("<td></td>");
      td.appendTo(tr);
      td.addOperations(data[i].name, data[i].status);

      tbody.append(tr);
    };

    table.append(tbody)

    // Appending the table to the element
    table.appendTo(this);
  };

  $.fn.addOperations = function( UUID, status )
  {
      var btnGrp = $("<div>", { class: "btn-group" });

      // start button
      var btnStart = $("<button>", { type: "button", class: "btn btn-default btn-xs"} );
      if (status == true) btnStart.attr("disabled", "disabled");
      var span = $("<span>", { class : "glyphicon glyphicon-play"});
      span.appendTo(btnStart);
      btnStart.appendTo(btnGrp);

      // stop button
      var btnStop = $("<button>", { type: "button", class: "btn btn-default btn-xs"} );
      if (status == false) btnStop.attr("disabled", "disabled");
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