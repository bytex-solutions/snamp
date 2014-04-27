(function( $ ){

  $.fn.getLicenseInfo = function( options ) {   
       
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

    for (var i = commonData.length - 1; i >= 0; i--) {
      var data;
      if (options.stub == true)
      {
          data = this.getLicenseInfoByBundleName(commonData[i].name);
      }
      else
      {
        // ajax code here
      }

      // Now make a table per bundle
      if (data != null)
      {
          this.append("<h3>" + commonData[i].name + "</h3>");
          this.append("<div class=\"well\">" + data + "</div>");
      }
    }
  };

})( jQuery );