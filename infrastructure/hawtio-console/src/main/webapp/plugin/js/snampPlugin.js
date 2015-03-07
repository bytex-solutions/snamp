/**
 * @module SnampShell
 * @mail SnampShell
 *
 * The main entry point for the SnampShell module
 *
 */
var SnampShell = (function(SnampShell) {

    /**
     * @property pluginName
     * @type {string}
     *
     * The name of this plugin
     */
    SnampShell.pluginName = 'snamp_shell_plugin';

    /**
     * @property log
     * @type {Logging.Logger}
     *
     * This plugin's logger instance
     */
    SnampShell.log = Logger.get('SnampShell');

    /**
     * @property contextPath
     * @type {string}
     *,
     * The top level path of this plugin on the server
     *
     */
    SnampShell.contextPath = "/snamp-shell-plugin/";

    /**
     * @property templatePath
     * @type {string}
     *
     * The path to this plugin's partials
     */
    SnampShell.templatePath = SnampShell.contextPath + "plugin/html/";


    /**
     * The mbean for OpenMBean Snamp Core
     */
    SnampShell.mbean = "com.itworks.snamp.management:type=SnampCore";

    /**
     * @property module
     * @type {object}
     *
     * This plugin's angularjs module instance.  This plugin only
     * needs hawtioCore to run, which provides services like
     * workspace, viewRegistry and layoutFull used by the
     * run function
     */
    SnampShell.module = angular.module('snamp_shell_plugin', ['hawtioCore'])
        .config(function($routeProvider) {

            /**
             * Here we define the route for our plugin.  One note is
             * to avoid using 'otherwise', as hawtio has a handler
             * in place when a route doesn't match any routes that
             * routeProvider has been configured with.
             */
            $routeProvider.
                when('/snamp_shell_plugin', {
                    templateUrl: SnampShell.templatePath + 'snamp.html'
                });
        });

    /**
     * Here we define any initialization to be done when this angular
     * module is bootstrapped.  In here we do a number of things:
     *
     * 1.  We log that we've been loaded (kinda optional)
     * 2.  We load our .css file for our views
     * 3.  We configure the viewRegistry service from hawtio for our
     *     route; in this case we use a pre-defined layout that uses
     *     the full viewing area
     * 4.  We add our help to the help registry
     * 5.  We configure our top-level tab and provide a link to our
     *     plugin.  This is just a matter of adding to the workspace's
     *     topLevelTabs array.
     */
    SnampShell.module.run(function(workspace, viewRegistry, helpRegistry, layoutFull) {

        SnampShell.log.info(SnampShell.pluginName, " loaded");

        Core.addCSS(SnampShell.contextPath + "plugin/css/snampPlugin.css");

        // tell the app to use the full layout, also could use layoutTree
        // to get the JMX tree or provide a URL to a custom layout
        viewRegistry["snamp_shell_plugin"] = layoutFull;

        /* Set up top-level link to our plugin.  Requires an object
         with the following attributes:

         id - the ID of this plugin, used by the perspective plugin
         and by the preferences page
         content - The text or HTML that should be shown in the tab
         title - This will be the tab's tooltip
         isValid - A function that returns whether or not this
         plugin has functionality that can be used for
         the current JVM.  The workspace object is passed
         in by hawtio's navbar controller which lets
         you inspect the JMX tree, however you can do
         any checking necessary and return a boolean
         href - a function that returns a link, normally you'd
         return a hash link like #/foo/bar but you can
         also return a full URL to some other site
         isActive - Called by hawtio's navbar to see if the current
         $location.url() matches up with this plugin.
         Here we use a helper from workspace that
         checks if $location.url() starts with our
         route.
         */
        workspace.topLevelTabs.push({
            id: "snamp-shell",
            content: "Snamp Shell",
            title: "SnampShell plugin loaded dynamically",
            isValid: function(workspace) { return true; },
            href: function() { return "#/snamp_shell_plugin"; },
            isActive: function(workspace) { return workspace.isLinkActive("snamp_shell_plugin"); }
        });

    });

    /**
     * @function SnampController
     * @param $scope
     * @param $location
     * @param jolokia
     *
     * The controller for shell.html, only requires the jolokia and location
     * services from hawtioCore
     */
    SnampShell.SnampController = function($scope, $location, jolokia) {

        $scope.sections = [
            {name: "/snamp_shell_plugin", label: "General Information", url: SnampShell.templatePath + "general.html"},
            {name: "/configuration", label: "Configuration", url: SnampShell.templatePath + "config.html"},
            {name: "/licensing", label: "License management", url: SnampShell.templatePath + "license.html"},
            {name: "/commands", label: "Components management", url: SnampShell.templatePath + "commands.html"}
        ];
        $scope.template = $scope.sections[0];

        // management
        $scope.refreshValues = function() {
            $scope.dmc = jolokia.request({
                type: 'read',
                mbean: SnampShell.mbean,
                attribute: 'DebugMessagesCount'
            }).value;
            $scope.imc = jolokia.request({
                type: 'read',
                mbean: SnampShell.mbean,
                attribute: 'InformationMessagesCount'
            }).value;
            $scope.wmc = jolokia.request({
                type: 'read',
                mbean: SnampShell.mbean,
                attribute: 'WarningMessagesCount'
            }).value;
            $scope.fc = jolokia.request({
                type: 'read',
                mbean: SnampShell.mbean,
                attribute: 'FaultsCount'
            }).value;
            SnampShell.log.info($scope.dmc, +$scope.imc, $scope.wmc, $scope.fc);
            Core.$apply($scope);
        };

        // Menu items
        $scope.menuSelected = function(section) {
            $scope.template = section;
            Core.$apply($scope);
            if ($scope.template == $scope.sections[3]) {
                $scope.refreshValues();
                var timer = jolokia.request({
                    type: 'read',
                    mbean: SnampShell.mbean,
                    attribute: 'StatisticRenewalTime'
                });
                SnampShell.log.info(timer.value);
                if (timer && timer.value) {
                    var spinWaitValue = timer.value;
                    setInterval($scope.refreshValues, timer.value);
                }
            }
        };

        // Grid data
        $scope.getGeneralGridData = function() {
            var result = [];

            // get adapters
            var adapters = jolokia.request({
                type: 'read',
                mbean: SnampShell.mbean,
                attribute: 'InstalledAdapters'
            }, onSuccess(null, {error: renderError, maxDepth: 20}));

            if (adapters && adapters.value) {
                angular.forEach(adapters.value, function(item) {
                    var adapterInfo = jolokia.request({
                        type: 'exec',
                        mbean: SnampShell.mbean,
                        operation: 'getAdapterInfo',
                        arguments: [item, 'en']
                    }, onSuccess(null, {error: renderError, maxDepth: 20}));
                    if (adapterInfo && adapterInfo.value ) {
                        result.push({
                            Name: adapterInfo.value.DisplayName,
                            Type: 'adapter',
                            Status: adapterInfo.value.State,
                            Version: adapterInfo.value.Version,
                            Description: adapterInfo.value.Description
                        })
                    }
                });
            }
            // get connectors
            // get adapters
            var connectors = jolokia.request({
                type: 'read',
                mbean: SnampShell.mbean,
                attribute: 'InstalledConnectors'
            }, onSuccess(null, {error: renderError, maxDepth: 20}));

            if (connectors && connectors.value) {
                angular.forEach(connectors.value, function(item) {
                    var connectorInfo = jolokia.request({
                        type: 'exec',
                        mbean: SnampShell.mbean,
                        operation: 'getConnectorInfo',
                        arguments: [item, 'en']
                    }, onSuccess(null, {error: renderError, maxDepth: 20}));
                    if (connectorInfo && connectorInfo.value ) {
                        result.push({
                            Name: connectorInfo.value.DisplayName,
                            Type: 'connector',
                            Status: connectorInfo.value.State,
                            Version: connectorInfo.value.Version,
                            Description: connectorInfo.value.Description
                        })
                    }
                });
            }
            SnampShell.log.info(JSON.stringify(result));
            return result;
        };

        var columnDefs = [
            {
                field: 'Name',
                displayName: 'Name',
                maxWidth: 250,
                minWidth: 150,
                width:200,
                resizable: true
            },
            {
                field: 'Type',
                displayName: 'Type',
                cellFilter: null,
                maxWidth: 85,
                minWidth: 85,
                width:85,
                resizable: true
            },
            {
                field: 'Status',
                displayName: 'Status',
                cellFilter: null,
                maxWidth: 85,
                minWidth: 85,
                width:95,
                resizable: true
            },
            {
                field: 'Version',
                displayName: 'Version',
                cellFilter: null,
                maxWidth: 70,
                minWidth: 70,
                width:90,
                resizable: true
            },
            {
                field: 'Description',
                displayName: 'Description',
                cellFilter: null,
                width: "*",
                height: "*",
                resizable: true
            }
        ];

        $scope.genData = $scope.getGeneralGridData();

        $scope.generalGrid = {
            data:  'genData',
            displayFooter: true,
            columnDefs: columnDefs,
            canSelectRows: false,
            title: "Current installed components info"
        };

        // Other functions
        $scope.getConfiguration = function() {
            SnampShell.log.info(SnampShell.pluginName, " get configuration operation (" + $scope + ")");
                // call mbean
               jolokia.request({
                    type: 'read',
                    mbean: SnampShell.mbean,
                    attribute: 'configuration'
                }, onSuccess(render, {error: renderError, maxDepth: 20}));
        };

        // update display with groovy result
        function render(response) {
            SnampShell.log.info(SnampShell.pluginName, " --> " + response.value);
            $scope.output = response.value;
            $scope.error = null;
            Core.$apply($scope);
        }

        function renderError(response) {
            SnampShell.log.info(SnampShell.pluginName, " error " + response);
            $scope.output = null;
            $scope.error = response;
            Core.$apply($scope);
        }
    };

    return SnampShell;

})(SnampShell || {});

// tell the hawtio plugin loader about our plugin so it can be
// bootstrapped with the rest of angular
hawtioPluginLoader.addModule(SnampShell.pluginName);