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
})( jQuery );