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
    SnampShell.module = angular.module('snamp_shell_plugin', ['hawtioCore', 'tree'])
        .config(function ($routeProvider) {
            $routeProvider.
                when('/snamp_shell_plugin', {
                    templateUrl: SnampShell.templatePath + 'snamp.html'
                });
        })
        .controller('SnampShell.SnampController', function ($scope, $location, jolokia, $timeout) {

            // ============================================================================================================= //
            // Menu sections
            $scope.sections = [
                {
                    name: "/snamp_shell_plugin",
                    label: "General Information",
                    url: SnampShell.templatePath + "general.html"
                },
                {name: "/configuration", label: "Configuration", url: SnampShell.templatePath + "config.html"},
                {name: "/licensing", label: "License management", url: SnampShell.templatePath + "license.html"},
                {name: "/commands", label: "Components management", url: SnampShell.templatePath + "commands.html"}
            ];
            $scope.template = $scope.sections[0];

            // management
            $scope.refreshValues = function () {
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
                SnampShell.log.info($scope.dmc, $scope.imc, $scope.wmc, $scope.fc);
                Core.$apply($scope);
            };

            var timerId = null;

            // ============================================================================================================= //
            // Grid data
            $scope.getGeneralGridData = function () {
                var array = [];
                // get all the components
                var result = jolokia.request({
                    type: 'read',
                    mbean: SnampShell.mbean,
                    attribute: 'InstalledComponents'
                }).value;
                angular.forEach(result, function (value, key) {
                    if (key != 'null')
                        this.push({
                            UserName: key,
                            ComponentName: value.Name,
                            State: value.State,
                            Version: value.Version,
                            IsCommerciallyLicensed: value.IsCommerciallyLicensed,
                            IsManageable: value.IsManageable,
                            IsConfigurationDescriptionAvailable: value.IsConfigurationDescriptionAvailable,
                            Description: value.Description
                        });
                }, array);
                return array;
            };

            // General information section
            var columnDefs = [
                {
                    field: 'UserName',
                    displayName: 'User defined name',
                    maxWidth: 250,
                    minWidth: 150,
                    width: 200,
                    resizable: true
                },
                {
                    field: 'ComponentName',
                    displayName: 'Component name',
                    maxWidth: 250,
                    minWidth: 150,
                    width: 200,
                    resizable: true
                },
                {
                    field: 'State',
                    displayName: 'State',
                    cellFilter: null,
                    maxWidth: 65,
                    minWidth: 65,
                    width: 65,
                    resizable: false
                },
                {
                    field: 'Version',
                    displayName: 'Version',
                    cellFilter: null,
                    maxWidth: 70,
                    minWidth: 70,
                    width: 70,
                    resizable: true
                },
                {
                    field: 'IsCommerciallyLicensed',
                    displayName: 'Licensable',
                    cellFilter: null,
                    maxWidth: 85,
                    minWidth: 85,
                    width: 85,
                    resizable: true
                },
                {
                    field: 'IsManageable',
                    displayName: 'Manageable',
                    cellFilter: null,
                    maxWidth: 85,
                    minWidth: 85,
                    width: 85,
                    resizable: true
                },
                {
                    field: 'IsConfigurationDescriptionAvailable',
                    displayName: 'Configurable',
                    cellFilter: null,
                    maxWidth: 85,
                    minWidth: 85,
                    width: 85,
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
                data: 'genData',
                displayFooter: true,
                columnDefs: columnDefs,
                canSelectRows: false,
                title: "Current installed components info"
            };
            // ============================================================================================================= //
            // Connectors section
            $scope.getConnectors = function () {
                var result = [];
                var connectors = jolokia.request({
                    type: 'read',
                    mbean: SnampShell.mbean,
                    attribute: 'InstalledConnectors'
                }).value;

                if (connectors) {
                    angular.forEach(connectors, function (item) {
                        var connectorInfo = jolokia.request({
                            type: 'exec',
                            mbean: SnampShell.mbean,
                            operation: 'getConnectorInfo',
                            arguments: [item, 'en']
                        }).value;
                        if (connectorInfo) {
                            result.push({
                                DisplayName: connectorInfo.DisplayName,
                                Name: item,
                                Status: connectorInfo.State,
                                Version: connectorInfo.Version,
                                Description: connectorInfo.Description
                            })
                        }
                    });
                }
                return result;
            };

            // Adapters section
            $scope.getAdapters = function () {
                var result = [];
                var adapters = jolokia.request({
                    type: 'read',
                    mbean: SnampShell.mbean,
                    attribute: 'InstalledAdapters'
                }).value;

                if (adapters) {
                    angular.forEach(adapters, function (item) {
                        var adapterInfo = jolokia.request({
                            type: 'exec',
                            mbean: SnampShell.mbean,
                            operation: 'getAdapterInfo',
                            arguments: [item, 'en']
                        }).value;
                        if (adapterInfo) {
                            result.push({
                                DisplayName: adapterInfo.DisplayName,
                                Name: item,
                                Status: adapterInfo.State,
                                Version: adapterInfo.Version,
                                Description: adapterInfo.Description
                            })
                        }
                    });
                }
                return result;
            };

            $scope.refreshComponents = function () {
                $scope.genData = $scope.getGeneralGridData();
                $scope.connectors = $scope.getConnectors();
                $scope.adapters = $scope.getAdapters();
                Core.$apply($scope);
            };

            // Start connector
            $scope.startConnector = function (name) {
                SnampShell.log.info("Starting " + name + " connector...");
                SnampShell.log.info(JSON.stringify(jolokia.request({
                    type: 'exec',
                    mbean: SnampShell.mbean,
                    operation: 'startConnector',
                    arguments: [name]
                })));
                $scope.refreshComponents();
            };

            // Stop connector
            $scope.stopConnector = function (name) {
                SnampShell.log.info("Stopping " + name + " connector...");
                SnampShell.log.info(JSON.stringify(jolokia.request({
                    type: 'exec',
                    mbean: SnampShell.mbean,
                    operation: 'stopConnector',
                    arguments: [name]
                })));
                $scope.refreshComponents();
            };

            // Start adapter
            $scope.startAdapter = function (name) {
                SnampShell.log.info("Starting " + name + " adapter...");
                SnampShell.log.info(JSON.stringify(jolokia.request({
                    type: 'exec',
                    mbean: SnampShell.mbean,
                    operation: 'startAdapter',
                    arguments: [name]
                })));
                $scope.refreshComponents();
            };

            // Stop adapter
            $scope.stopAdapter = function (name) {
                SnampShell.log.info("Stopping " + name + " adapter...");
                SnampShell.log.info(JSON.stringify(jolokia.request({
                    type: 'exec',
                    mbean: SnampShell.mbean,
                    operation: 'stopAdapter',
                    arguments: [name]
                })));
                $scope.refreshComponents();
            };

            $scope.fillModal = function (content, title) {
                $scope.modalContent = content;
                $scope.modalTitle = title;
                Core.$apply($scope);
            };

            $scope.modalContent = "This is a HUGE mistake, you should not see it";
            $scope.modalTitle = "Undefined title";

            // Initial connectors array
            $scope.connectors = $scope.getConnectors();

            // Initial adapters array
            $scope.adapters = $scope.getAdapters();

            // Licensing
            $scope.getLicense = function () {
                return jolokia.request({
                    type: 'read',
                    mbean: SnampShell.mbean,
                    attribute: 'license'
                }).value;
            };

            $scope.license = $scope.getLicense();

            // Configuration
            $scope.getConfiguration = function () {
                return jolokia.request({
                    type: 'read',
                    mbean: SnampShell.mbean,
                    attribute: 'configuration'
                }, onSuccess(null, {maxDepth: 20})).value;
            };

            function generateDropDown(array, id, active) {
                var s = $("<select id=\"" + id + "\"/>");
                for(var val in array) {
                    SnampShell.log.info(val);
                    $("<option />", {value: val.name, text: val.DisplayName}).appendTo(s);
                }
                s.val(active);
                return s.prop('outerHTML');
            }

            function editNode(node){
                var prevTitle = node.data.title,
                    tree = node.tree;
                // Disable dynatree mouse- and key handling
                tree.$widget.unbind();
                // Replace node with <input>
                $(".dynatree-title", node.span).html("<input id='editNode' value='" + prevTitle + "'>");
                // Focus <input> and bind keyboard handler
                $("input#editNode")
                    .focus()
                    .keydown(function(event){
                        switch( event.which ) {
                            case 27: // [esc]
                                // discard changes on [esc]
                                $("input#editNode").val(prevTitle);
                                $(this).blur();
                                break;
                            case 13: // [enter]
                                // simulate blur to accept new value
                                $(this).blur();
                                break;
                        }
                    }).blur(function(event){
                        // Accept new value, when user leaves <input>
                        var title = $("input#editNode").val();
                        node.setTitle(title);
                        // Re-enable mouse and keyboard handlling
                        tree.$widget.bind();
                        node.focus();
                    });
            }

            $scope.configurationJSON2Tree = function (jsonObject) {
                var array = [
                    {title: "Resource Adapters", isFolder: true},
                    {title: "Managed Resources", isFolder: true}
                ];
                if (jsonObject.hasOwnProperty("ResourceAdapters")) {
                    array[0].children = [];
                    angular.forEach(jsonObject["ResourceAdapters"], function (value, key) {
                        var currentChild = {title: key, isFolder: true, editable: true}; // adapter userDefined name
                        currentChild.children = [];
                        currentChild.children.push({title: "Adapter type: " + generateDropDown($scope.getAdapters(), "adapterName", value["Adapter"]["Name"]), isFolder: false}); // adapter system name
                        var params = {title: "Parameters", isFolder: true};
                        params.children = [];
                        if (value["Adapter"]["Parameters"]) {
                            angular.forEach(value["Adapter"]["Parameters"], function (parameterValue, parameterKey) {
                                var editableValue = parameterKey + ":" + "" +
                                    "<input name=\"value\" type=\"text\" value=\"" + parameterValue["Value"] + "\"/>";
                                params.children.push({title: editableValue, isFolder: false});
                            });
                        }
                        params.children.push({title: "<span class=\"glyphicon glyphicon-plus\"/> new parameter", service: "add"});
                        currentChild.children.push(params);
                        array[0].children.push(currentChild);
                    });
                }

                if (jsonObject.hasOwnProperty("ManagedResources")) {
                    array[1].children = [];
                    angular.forEach(jsonObject["ManagedResources"], function (value, key) {
                        array[1].children.push({title: key, isFolder: true})
                    });
                }
                return array;
            };

            $scope.drawConfiguration = function () {
                $.ui.dynatree.nodedatadefaults["icon"] = false; // Turn off icons by default
                var isMac = /Mac/.test(navigator.platform);
                $("#snampTreeConfig").dynatree({
                    onClick: function(node, event) {
                        if (node.data.editable == true) {
                            if (event.shiftKey) {
                                editNode(node);
                                return false;
                            }
                        }
                        if (node.data.service) {
                            if (node.data.service == "add") {
                                node.getParent().addChild({title: "New Node", key: "3333"})
                            }
                        }
                    },
                    onKeydown: function(node, event) {
                        if (node.data.editable == true) {
                            switch (event.which) {
                                case 113: // [F2]
                                    editNode(node);
                                    return false;
                                case 13: // [enter]
                                    if (isMac) {
                                        editNode(node);
                                        return false;
                                    }
                            }
                        }
                    },
                   /* onActivate: function (node) {
                        // A DynaTreeNode object is passed to the activation handler
                        // Note: we also get this event, if persistence is on, and the page is reloaded.
                        alert("You activated " + node.data.title);
                    },*/
                    persist: false,
                    children: $scope.configurationJSON2Tree($scope.configuration)
                });
            };

            // Menu items
            $scope.menuSelected = function (section) {
                $scope.template = section;
                if ($scope.template == $scope.sections[3]) {
                    $scope.refreshValues();
                    var StatisticRenewalTime = jolokia.request({
                        type: 'read',
                        mbean: SnampShell.mbean,
                        attribute: 'StatisticRenewalTime'
                    }).value;
                    if (StatisticRenewalTime) {
                        timerId = setInterval($scope.refreshValues, StatisticRenewalTime);
                    }
                } else if ($scope.template == $scope.sections[1]) {
                    $scope.configuration = $scope.getConfiguration();
                    Core.$apply($scope);
                } else {
                    if (timerId != null) {
                        clearInterval(timerId);
                    }
                    Core.$apply($scope);
                }
            };
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
    SnampShell.module.run(function (workspace, viewRegistry, helpRegistry, layoutFull) {

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
            isValid: function (workspace) {
                return true;
            },
            href: function () {
                return "#/snamp_shell_plugin";
            },
            isActive: function (workspace) {
                return workspace.isLinkActive("snamp_shell_plugin");
            }
        });
    });


        return SnampShell;

    })(SnampShell || {});
// tell the hawtio plugin loader about our plugin so it can be
// bootstrapped with the rest of angular
hawtioPluginLoader.addModule(SnampShell.pluginName);