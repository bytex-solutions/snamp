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

            $scope.modalContent = [];
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

            /**
             * Generate dropdown dom element from the adapters/connectors list with currently active option
             * @param array
             * @param id
             * @param active
             * @returns {*}
             */
            function generateDropDown(array, id, active) {
                var s = $("<select id=\"" + id + "\"/>");
                angular.forEach(array, function (value) {
                    $("<option />", {value: value.name, text: value.DisplayName}).appendTo(s);
                });
                s.val(active);
                return s.prop('outerHTML');
            }

            /**
             * Remove node with check.
             * @param node
             */
            $scope.removeNode = function(node) {
                if (node.data.removable == true) {
                    node.remove();
                }
            };

            /**
             * Edit node title;
             * @param node
             */
            $scope.editNode = function(node){
                if (!(node.data.editable == true)) {
                    return;
                }
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
                        node.data.value = title;
                        // Re-enable mouse and keyboard handlling
                        tree.$widget.bind();
                        node.focus();
                    });
            };

            /**
             * Forward transforming the json object to the dynatree data model.
             * @param jsonObject
             * @returns {{title: string, isFolder: boolean, name: string, children: Array, type: string}[]}
             */
            $scope.configurationJSON2Tree = function (jsonObject) {
                var array = [
                    {
                        title: "Resource Adapters",
                        isFolder: true,
                        name: "Resource Adapters",
                        children: [],
                        type: "adapters"
                    },
                    {
                        title: "Managed Resources",
                        isFolder: true,
                        name: "Managed Resources",
                        children: [],
                        type: "connectors"
                    }
                ];
                if (jsonObject.hasOwnProperty("ResourceAdapters")) {
                    angular.forEach(jsonObject["ResourceAdapters"], function (value, key) {
                        var currentChild = {
                            title: key,
                            isFolder: true,
                            name: value["Adapter"]["Name"],
                            type: "adapter",
                            children: [],
                            removable: true
                        }; // adapter userDefined name
                        currentChild.children.push({
                            title: "Adapter type: " + generateDropDown($scope.getAdapters(), "adapterName", value["Adapter"]["Name"]),
                            isFolder: false,
                            name: value["Adapter"]["Name"],
                            type: "type",
                            value: value["Adapter"]["Name"]
                        }); // adapter system name
                        var params = {
                            title: "Parameters",
                            isFolder: true,
                            children: [],
                            name: value["Adapter"]["Name"] + " params",
                            type: "params"
                        };
                        if (value["Adapter"]["Parameters"]) {
                            angular.forEach(value["Adapter"]["Parameters"], function (parameterValue, parameterKey) {
                                params.children.push({
                                    title: parameterKey +": <input name=\"value\" type=\"text\" value=\"" + parameterValue["Value"] + "\"/>",
                                    isFolder: false,
                                    name: parameterKey,
                                    type: "param",
                                    value: parameterValue["Value"],
                                    removable: true
                                });
                            });
                        }
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

            $scope.activeNode = "nothing";

            /**
             * Return root element.
             * @param node
             * @returns {*}
             */
            function getRootNode(node) {
                var rootNode = node;
                while (rootNode.getLevel() > 1) {
                    rootNode = rootNode.getParent();
                }
                return rootNode;
            }

            /**
             * Returns child with appropriate name
             * @param children
             * @param name
             * @returns {*}
             */
            function getChildrenByName(children, name) {
                var child = null;
                angular.forEach(children, function (value) {
                    if (value.data.name == name) {
                        child = value;
                    }
                });
                return child;
            }

            /**
             * Returns child with appropriate type
             * @param children
             * @param type
             * @returns {*}
             */
            function getChildrenByType(children, type) {
                var child = null;
                angular.forEach(children, function (value) {
                    if (value.data.type == type) {
                        child = value;
                    }
                });
                return child;
            }

            // Get adapter's param array
            function getActiveNodeParams() {
                if ($scope.activeNode.data.type == "params") {
                    return $scope.activeNode;
                }
                if ($scope.activeNode.data.type == "param") {
                    return $scope.activeNode;
                }
                if ($scope.activeNode.data.type == "type") {
                    return getChildrenByType($scope.activeNode.getParent().getChildren(),"params");
                }
                if ($scope.activeNode.data.type == "adapter") {
                    return getChildrenByType($scope.activeNode.getChildren(),"params");
                }
                if ($scope.activeNode.data.type == "subParam") {
                    return $scope.activeNode.getParent();
                }
                return $scope.activeNode;
            }

            // Check if activeNode already contains param with a given name
            $scope.checkParamExists = function(paramName) {
                var node = getActiveNodeParams();
                var contains = false;
                angular.forEach(node.getChildren(), function (value) {
                   if  (value.data.name == paramName) {
                       contains = true;
                   }
                });
                return contains;
            };

            /**
             * Onclick action for filling the necessary scope params
             * @param key
             * @param content
             */
            $scope.fillCurrentParamValue = function (key, content) {
                $scope.currentValue = content;
                $scope.currentParamKey = key;
                Core.$apply($scope);
            };

            /**
             * Checks of all available params are already defined
             */
            function checkAllParamsSet() {
                return getActiveNodeParams().getChildren().length < $scope.modalContent.length;
            }

            /**
             * Append new element to the treeView model.
             */
            $scope.appendNewElement = function() {
                var node = getActiveNodeParams();
                // append new child to the current node
                var parent = getRootNode(node);
                if (parent.data.type == "adapters") {
                    var adapterName = getChildrenByType(parent.getChildren(), "adapter").data.name;
                    SnampShell.log.info(adapterName);
                    var adapterConfig = jolokia.request({
                        type: 'exec',
                        mbean: SnampShell.mbean,
                        operation: 'getAdapterConfigurationSchema',
                        arguments: [adapterName, ""] // default console
                    }).value;
                    SnampShell.log.info(JSON.stringify(adapterConfig));

                    // Appending "AttributeParameters"
                    if ($scope.activeNode.data.type == "param") {
                        $scope.modalTitle = "Appending new attribute param";
                        $scope.modalContent = adapterConfig["AttributeParameters"];
                    } else {
                        $scope.modalTitle = "Appending new attribute to " + node.getParent().data.title + " adapter";
                        $scope.modalContent = adapterConfig["ResourceAdapterParameters"];
                    }
                    if (checkAllParamsSet) {
                        Core.$apply($scope);
                        $('#myModal').modal('show');
                    } else {
                        Core.notification('info', "All available params are already set");
                    }
                }
            };

            // Append chosen param to the active node on scope
            $scope.appendParam = function() {
                var value = "";
                if ($scope.currentValue["Description"]["DefaultValue"]) {
                    value = $scope.currentValue["Description"]["DefaultValue"];
                }
                var node = getActiveNodeParams();
                if ($scope.activeNode.data.type == "param") {
                    node = $scope.activeNode;
                    node.addChild({
                        title: $scope.currentParamKey + ": <input name=\"value\" type=\"text\" value=\"" + value + "\"/>",
                        name: $scope.currentParamKey,
                        type: "subParam",
                        value: value,
                        removable: true
                    });
                } else {
                    node.addChild({
                        title: $scope.currentParamKey + ": <input name=\"value\" type=\"text\" value=\"" + value + "\"/>",
                        isFolder: false,
                        name: $scope.currentParamKey,
                        type: "param",
                        value: value,
                        removable: true
                    });
                }
                $('#myModal').modal('hide');
                node.expand(true);
            };

            /**
             * Draw configuration to the html.
             */
            $scope.drawConfiguration = function () {
                $.ui.dynatree.nodedatadefaults["icon"] = false; // Turn off icons by default
                var isMac = /Mac/.test(navigator.platform);
                $("#snampTreeConfig").dynatree({
                    noLink: true,
                    selectMode: 1,
                    onClick: function(node, event) {
                        $scope.activeNode = node;
                        Core.$apply($scope);
                        if (node.data.editable == true) {
                            if (event.shiftKey) {
                                $scope.editNode(node);
                                return false;
                            }
                        }
                    },
                    onKeydown: function(node, event) {
                        if (node.data.editable == true) {
                            switch (event.which) {
                                case 113: // [F2]
                                    $scope.editNode(node);
                                    return false;
                                case 13: // [enter]
                                    if (isMac) {
                                        $scope.editNode(node);
                                        return false;
                                    }
                            }
                        }
                    },
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