(function( $ ){

  $.fn.createTable = function( options ) {  
    // Making the header part of table
    var table = $("<table>", {class:"table"});
    table.append("<thead><tr><th>Status</th><th>Bundle Name</th><th>Description</th><th>Operations</th></tr></thead>");

    
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

      content = data[i].name;
      tr.append("<td>" + content + "</td>");

      content = data[i].description;
      tr.append("<td>" + content + "</td>");

      tbody.append(tr);
    };

    table.append(tbody)

    // Appending the table to the element
    table.appendTo(this);
  };
})( jQuery );