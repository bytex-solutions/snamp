module com.snamp.models
{
    function getSummary() : bundle[]
    {
       return [

           // First bundle
           new bundle(type, "connector",
               "JMX connector",
               "Provides information about remote or local JMX bean " +
               "according to configuration file propertiess",
               new bundleStatus(5,10, [
                   new managmentTarget("service:jmx:rmi:///jndi/rmi://10.200.100.101:23400/jmxrmi", 5,2),
                   new managmentTarget("service:jmx:rmi:///jndi/rmi://10.200.100.102:23400/jmxrmi", 1,4),
                   new managmentTarget("service:jmx:rmi:///jndi/rmi://10.200.100.103:23400/jmxrmi", 2,0),
                   new managmentTarget("service:jmx:rmi:///jndi/rmi://10.200.100.104:23400/jmxrmi", 1,10),
                   new managmentTarget("service:jmx:rmi:///jndi/rmi://10.200.100.105:23400/jmxrmi", 0,5)
               ]),
               new license("Full license, includes up to 5 instances and 1 year of support subscribe (excluding on-demand development of additional features)")
           ),

           // Second bundle
           new bundle(type, "adapter",
               "SNMP adapter",
                   "Provides SNMP OIDs from any source",
               new bundleStatus(1,1, [
                   new managmentTarget("localhost:3122", 1,10)
               ])           ,
               new license("Full license, includes infinite count of instances and 2 years of full support")
           ),

           // Third bundle
           new bundle(type, "connector",
               "WMB connector",
                   "Links to Websphere Message Broker and returns some information from there",
               new bundleStatus(1,1, [
                   new managmentTarget("wmb://anticitizen.dhis.org:8000/TEST_QMGR", 1,1)
               ])    ,
               new license("Trial license, expires 5 days, includes 1 instance of connector")
           ),
       ];
    }
}