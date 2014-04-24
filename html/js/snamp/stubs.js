(function( $ ){

  $.fn.getBundleInfo = function() {  
    // activeStatus, bundle name, description
    var bundleInfo = [
        {status: true, name: "JMX connector", description: "Provides information about remote or local JMX bean according to configuration file propertiess"},
        {status: true, name: "SNMP adapter", description: "Provides SNMP OIDs from any source"},
        {status: false, name: "WMB connector", description: "Links to Websphere Message Broker and returns some information from there"}
    ];
    return bundleInfo;
  };
})( jQuery );