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

  $.fn.getBundleStatusByName = function ( name )
  {
  		switch(name)
  		{
  			case "JMX connector":
  				return {currentInstanceCount: 5, maxInstanceCount: 10, managmentTargets: [
  					{connectionString: "service:jmx:rmi:///jndi/rmi://10.200.100.100:23400/jmxrmi", events: 5, attributes: 2},
  					{connectionString: "service:jmx:rmi:///jndi/rmi://10.200.100.111:23400/jmxrmi", events: 3, attributes: 5},
  					{connectionString: "service:jmx:rmi:///jndi/rmi://10.200.100.112:23400/jmxrmi", events: 8, attributes: 5},
  					{connectionString: "service:jmx:rmi:///jndi/rmi://10.200.100.121:23400/jmxrmi", events: 5, attributes: 5},
  					{connectionString: "service:jmx:rmi:///jndi/rmi://10.200.100.122:23400/jmxrmi", events: 0, attributes: 5},
  				] 
  			};

  			case "SNMP adapter":
  				return {currentInstanceCount: 1, maxInstanceCount: 1, managmentTargets: [
  					{connectionString: "localhost:3122", events: 1, attributes: 1}
  				] 
  			};

  			case "WMB connector" :
  				return {currentInstanceCount: 1, maxInstanceCount: 1, managmentTargets: [
  					{connectionString: "wmb://anticitizen.dhis.org:8000/TEST_QMGR", events: 1, attributes: 1}
  				] 
  			};
  		}
  };

})( jQuery );