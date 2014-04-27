(function( $ ){

  $.fn.getBundleInfo = function() {  
    // activeStatus, bundle name, description
    var bundleInfo = [
        {status: true, type: "connector", name: "JMX connector", description: "Provides information about remote or local JMX bean according to configuration file propertiess"},
        {status: true, type: "adapter",  name: "SNMP adapter", description: "Provides SNMP OIDs from any source"},
        {status: false, type: "connector", name: "WMB connector", description: "Links to Websphere Message Broker and returns some information from there"}
    ];
    return bundleInfo;
  };

  $.fn.getLicenseInfoByBundleName = function( name )
  {
  		switch(name)
  		{
  			case "JMX connector":
  				return "Full license, includes up to 5 instances and 1 year of support subscribe (excluding on-demand development of additional features)";

  			case "SNMP adapter":
  				return "Full license, includes infinite count of instances and 2 years of full support";

  			case "WMB connector" :
  				return "Trial license, expires 5 days, includes 1 instance of connector";
  		}
  };

})( jQuery );