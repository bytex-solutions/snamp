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

            if(jQuery)( function() {

                /* Check browser version, since $.browser was removed in jQuery 1.9 */
                function _checkBrowser(){
                    var matched, browser;
                    function uaMatch( ua ) {
                        ua = ua.toLowerCase();
                        var match = /(chrome)[ \/]([\w.]+)/.exec( ua ) ||
                            /(webkit)[ \/]([\w.]+)/.exec( ua ) ||
                            /(opera)(?:.*version|)[ \/]([\w.]+)/.exec( ua ) ||
                            /(msie) ([\w.]+)/.exec( ua ) ||
                            ua.indexOf("compatible") < 0 && /(mozilla)(?:.*? rv:([\w.]+)|)/.exec( ua ) ||
                            [];
                        return {
                            browser: match[ 1 ] || "",
                            version: match[ 2 ] || "0"
                        };
                    }
                    matched = uaMatch( navigator.userAgent );
                    browser = {};
                    if ( matched.browser ) {
                        browser[ matched.browser ] = true;
                        browser.version = matched.version;
                    }
                    if ( browser.chrome ) {
                        browser.webkit = true;
                    } else if ( browser.webkit ) {
                        browser.safari = true;
                    }
                    return browser;
                }
                var BROWSER = jQuery.browser || _checkBrowser();

                $.extend($.fn, {

                    contextMenu: function(o, callback) {
                        // Defaults
                        if( o.menu == undefined ) return false;
                        if( o.inSpeed == undefined ) o.inSpeed = 150;
                        if( o.outSpeed == undefined ) o.outSpeed = 75;
                        // 0 needs to be -1 for expected results (no fade)
                        if( o.inSpeed == 0 ) o.inSpeed = -1;
                        if( o.outSpeed == 0 ) o.outSpeed = -1;
                        // Loop each context menu
                        $(this).each( function() {
                            var el = $(this);
                            var offset = $(el).offset();
                            // Add contextMenu class
                            $('#' + o.menu).addClass('contextMenu');
                            // Simulate a true right click
                            $(this).mousedown( function(e) {
                                var evt = e;
//					evt.stopPropagation();
                                evt.preventDefault();
                                $(this).mouseup( function(e) {
//						e.stopPropagation();
                                    e.preventDefault();
                                    var srcElement = $(this);
                                    $(this).unbind('mouseup');
                                    if( evt.button == 2 ) {
                                        // Hide context menus that may be showing
                                        $(".contextMenu").hide();
                                        // Get this context menu
                                        var menu = $('#' + o.menu);

                                        if( $(el).hasClass('disabled') ) return false;

                                        // Detect mouse position
                                        var d = {}, x, y;
                                        if( self.innerHeight ) {
                                            d.pageYOffset = self.pageYOffset;
                                            d.pageXOffset = self.pageXOffset;
                                            d.innerHeight = self.innerHeight;
                                            d.innerWidth = self.innerWidth;
                                        } else if( document.documentElement &&
                                            document.documentElement.clientHeight ) {
                                            d.pageYOffset = document.documentElement.scrollTop;
                                            d.pageXOffset = document.documentElement.scrollLeft;
                                            d.innerHeight = document.documentElement.clientHeight;
                                            d.innerWidth = document.documentElement.clientWidth;
                                        } else if( document.body ) {
                                            d.pageYOffset = document.body.scrollTop;
                                            d.pageXOffset = document.body.scrollLeft;
                                            d.innerHeight = document.body.clientHeight;
                                            d.innerWidth = document.body.clientWidth;
                                        }
                                        (e.pageX) ? x = e.pageX : x = e.clientX + d.scrollLeft;
                                        (e.pageY) ? y = e.pageY : y = e.clientY + d.scrollTop;

                                        // Show the menu
                                        $(document).unbind('click');
                                        // MW: fixed position in a fancy layout
//							$(menu).css({ top: y, left: x }).fadeIn(o.inSpeed);
                                        $(menu).fadeIn(o.inSpeed).offset({ top: y, left: x }); // must be visible, before calling offset()
                                        // Hover events
                                        $(menu).find('A').mouseover( function() {
                                            $(menu).find('LI.hover').removeClass('hover');
                                            $(this).parent().addClass('hover');
                                        }).mouseout( function() {
                                            $(menu).find('LI.hover').removeClass('hover');
                                        });

                                        // Keyboard
                                        $(document).keypress( function(e) {
                                            switch( e.keyCode ) {
                                                case 38: // up
                                                    if( $(menu).find('LI.hover').size() == 0 ) {
                                                        $(menu).find('LI:last').addClass('hover');
                                                    } else {
                                                        $(menu).find('LI.hover').removeClass('hover').prevAll('LI:not(.disabled)').eq(0).addClass('hover');
                                                        if( $(menu).find('LI.hover').size() == 0 ) $(menu).find('LI:last').addClass('hover');
                                                    }
                                                    break;
                                                case 40: // down
                                                    if( $(menu).find('LI.hover').size() == 0 ) {
                                                        $(menu).find('LI:first').addClass('hover');
                                                    } else {
                                                        $(menu).find('LI.hover').removeClass('hover').nextAll('LI:not(.disabled)').eq(0).addClass('hover');
                                                        if( $(menu).find('LI.hover').size() == 0 ) $(menu).find('LI:first').addClass('hover');
                                                    }
                                                    break;
                                                case 13: // enter
                                                    $(menu).find('LI.hover A').trigger('click');
                                                    break;
                                                case 27: // esc
                                                    $(document).trigger('click');
                                                    break
                                            }
                                        });

                                        // When items are selected
                                        $('#' + o.menu).find('A').unbind('click');
                                        $('#' + o.menu).find('LI:not(.disabled) A').click( function() {
                                            $(document).unbind('click').unbind('keypress');
                                            $(".contextMenu").hide();
                                            // Callback
                                            if( callback ) callback( $(this).attr('href').substr(1), $(srcElement), {x: x - offset.left, y: y - offset.top, docX: x, docY: y} );
                                            return false;
                                        });

                                        // Hide bindings
                                        setTimeout( function() { // Delay for Mozilla
                                            $(document).click( function() {
                                                $(document).unbind('click').unbind('keypress');
                                                $(menu).fadeOut(o.outSpeed);
                                                return false;
                                            });
                                        }, 0);
                                    }
                                });
                            });

                            // Disable text selection
                            if( BROWSER.mozilla ) {
                                $('#' + o.menu).each( function() { $(this).css({ 'MozUserSelect' : 'none' }); });
                            } else if( BROWSER.msie ) {
                                $('#' + o.menu).each( function() { $(this).bind('selectstart.disableTextSelect', function() { return false; }); });
                            } else {
                                $('#' + o.menu).each(function() { $(this).bind('mousedown.disableTextSelect', function() { return false; }); });
                            }
                            // Disable browser context menu (requires both selectors to work in IE/Safari + FF/Chrome)
                            $(el).add($('UL.contextMenu')).bind('contextmenu', function() { return false; });

                        });
                        return $(this);
                    },

                    // Disable context menu items on the fly
                    disableContextMenuItems: function(o) {
                        if( o == undefined ) {
                            // Disable all
                            $(this).find('LI').addClass('disabled');
                            return( $(this) );
                        }
                        $(this).each( function() {
                            if( o != undefined ) {
                                var d = o.split(',');
                                for( var i = 0; i < d.length; i++ ) {
                                    $(this).find('A[href="' + d[i] + '"]').parent().addClass('disabled');

                                }
                            }
                        });
                        return( $(this) );
                    },

                    // Enable context menu items on the fly
                    enableContextMenuItems: function(o) {
                        if( o == undefined ) {
                            // Enable all
                            $(this).find('LI.disabled').removeClass('disabled');
                            return( $(this) );
                        }
                        $(this).each( function() {
                            if( o != undefined ) {
                                var d = o.split(',');
                                for( var i = 0; i < d.length; i++ ) {
                                    $(this).find('A[href="' + d[i] + '"]').parent().removeClass('disabled');

                                }
                            }
                        });
                        return( $(this) );
                    },

                    // Disable context menu(s)
                    disableContextMenu: function() {
                        $(this).each( function() {
                            $(this).addClass('disabled');
                        });
                        return( $(this) );
                    },

                    // Enable context menu(s)
                    enableContextMenu: function() {
                        $(this).each( function() {
                            $(this).removeClass('disabled');
                        });
                        return( $(this) );
                    },

                    // Destroy context menu(s)
                    destroyContextMenu: function() {
                        // Destroy specified context menus
                        $(this).each( function() {
                            // Disable action
                            $(this).unbind('mousedown').unbind('mouseup');
                        });
                        return( $(this) );
                    }

                });
            })(jQuery);


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
                var tree = node.tree;

                // Replace node with <input>
                inputObject = $(".dynatree-title", node.span).find("input");
                // Focus <input> and bind keyboard handler
                inputObject.focus()
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
                                    title: parameterKey +": <value>" + parameterValue["Value"] + "</value>",
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
                        var currentChild = {
                            title: key,
                            isFolder: true,
                            name: value["Connector"]["ConnectionType"],
                            type: "connector",
                            children: [],
                            removable: true
                        }; // adapter userDefined name
                        currentChild.children.push({
                            title: "Connection type: " + generateDropDown($scope.getConnectors(), "connectorName", value["Connector"]["ConnectionType"]),
                            isFolder: false,
                            name: value["Connector"]["ConnectionType"],
                            type: "type",
                            value: value["Connector"]["ConnectionType"]
                        }); // adapter system name
                        currentChild.children.push({
                            title: "Connection string: " + "<value>" + value["Connector"]["ConnectionString"] + "</value>",
                            isFolder: false,
                            name: value["Connector"]["ConnectionString"],
                            type: "string",
                            value: value["Connector"]["ConnectionString"]
                        }); // adapter system name

                        // Parsing connector's parameters
                        var params = {
                            title: "Parameters",
                            isFolder: true,
                            children: [],
                            name: value["Connector"]["ConnectionType"] + " params",
                            type: "params"
                        };

                        // Loop over certain connector's parameters
                        if (value["Connector"]["Parameters"]) {
                            angular.forEach(value["Connector"]["Parameters"], function (parameterValue, parameterKey) {
                                params.children.push({
                                    title: parameterKey +": <value>" + parameterValue["Value"] + "</value>",
                                    isFolder: false,
                                    name: parameterKey,
                                    type: "param",
                                    value: parameterValue["Value"],
                                    removable: true
                                });
                            });
                        }

                        // Appending proper parameters to the certain connector
                        currentChild.children.push(params);

                        // Parsing certain connector's attributes
                        var attributes = {
                            title: "Attributes",
                            isFolder: true,
                            children: [],
                            name: value["Connector"]["ConnectionType"] + " attribites",
                            type: "attributes"
                        };

                        // Loop over the attributes
                        if (value["Connector"]["Attributes"]) {
                            angular.forEach(value["Connector"]["Attributes"], function (parameterValue, parameterKey) {
                                // Parameters
                                var attrParams = {
                                    title: "Parameters",
                                    isFolder: true,
                                    children: [],
                                    name: parameterKey + " params",
                                    type: "attrParams"
                                };

                                // Loop over certain attribute additional properties
                                if (parameterValue["Attribute"]["AdditionalProperties"]) {
                                    angular.forEach(parameterValue["Attribute"]["AdditionalProperties"], function (attrValue, attrKey) {
                                        attrParams.children.push({
                                            title: attrKey + ": <value>" + attrValue["Value"] + "</value>",
                                            isFolder: false,
                                            name: attrKey,
                                            type: "attrParam",
                                            value: attrValue["Value"],
                                            removable: true
                                        });
                                    });
                                }

                                // Appending certain attribute to the attributes tree node
                                attributes.children.push({
                                    title: "User defined name" +": <value>" + parameterKey + "</value>",
                                    isFolder: true,
                                    name: parameterKey,
                                    type: "attribute",
                                    value: parameterKey,
                                    removable: true,
                                    children: [ // appending Attribute name and it's already defined additional properties
                                        {
                                            title: "Attribute name" + ": <value>" + parameterValue["Attribute"]["Name"] + "</value>",
                                            isFolder: true,
                                            name: parameterValue["Attribute"]["Name"],
                                            type: "attrName",
                                            value: parameterValue["Attribute"]["Name"]
                                        },
                                        attrParams
                                    ]
                                });

                            });
                        }

                        // Appending attributes to the certain connector
                        currentChild.children.push(attributes);

                        // Appending certain connector to the Managed Resources node
                        array[1].children.push(currentChild);
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
                    return $scope.activeNode.getParent();
                }
                if ($scope.activeNode.data.type == "type") {
                    return getChildrenByType($scope.activeNode.getParent().getChildren(),"params");
                }
                if ($scope.activeNode.data.type == "adapter") {
                    return getChildrenByType($scope.activeNode.getChildren(),"params");
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
            $scope.allParamAlreadySet = function() {
                return getActiveNodeParams().getChildren().length >= $scope.modalContent.length;
            };

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
                    SnampShell.log.info(JSON.stringify(adapterConfig["ResourceAdapterParameters"]));

                    // Appending "AttributeParameters"

                    $scope.modalTitle = "Appending new attribute to " + node.getParent().data.title + " adapter";
                    $scope.modalContent = adapterConfig["ResourceAdapterParameters"];

                    if (!$scope.allParamAlreadySet()) {
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
                node.addChild({
                    title: $scope.currentParamKey + ": <value>" + value + "</value>",
                    isFolder: false,
                    name: $scope.currentParamKey,
                    type: "param",
                    value: value,
                    removable: true
                });
                $('#myModal').modal('hide');
                node.expand(true);
            };

            var clipboardNode = null;
            var pasteMode = null;

            // --- Copy/paste/cut functionality ----------------------------------------
            function copyPaste(action, node) {
                switch( action ) {
                    case "cut":
                    case "copy":
                        clipboardNode = node;
                        pasteMode = action;
                        break;
                    case "paste":
                        if( !clipboardNode ) {
                            alert("Clipoard is empty.");
                            break;
                        }
                        if( pasteMode == "cut" ) {
                            // Cut mode: check for recursion and remove source
                            var isRecursive = false;
                            var cb = clipboardNode.toDict(true, function(dict){
                                // If one of the source nodes is the target, we must not move
                                if( dict.key == node.data.key )
                                    isRecursive = true;
                            });
                            if( isRecursive ) {
                                alert("Cannot move a node to a sub node.");
                                return;
                            }
                            node.addChild(cb);
                            clipboardNode.remove();
                        } else {
                            // Copy mode: prevent duplicate keys:
                            var cb = clipboardNode.toDict(true, function(dict){
                                dict.title = "Copy of " + dict.title;
                                delete dict.key; // Remove key, so a new one will be created
                            });
                            node.addChild(cb);
                        }
                        clipboardNode = pasteMode = null;
                        break;
                    default:
                        alert("Unhandled clipboard action '" + action + "'");
                }
            };


            // --- Contextmenu helper --------------------------------------------------
            function bindContextMenu(span) {
                // Add context menu to this node:
                $(span).contextMenu({menu: "myMenu"}, function(action, el, pos) {
                    // The event was bound to the <span> tag, but the node object
                    // is stored in the parent <li> tag
                    var node = $.ui.dynatree.getNode(el);
                    $scope.activeNode = node;
                    Core.$apply($scope);
                    switch( action ) {
                        case "append":
                            $scope.appendNewElement();
                            break;
                        case "delete":
                            $scope.removeNode($scope.activeNode)
                            break;
                        case "cut":
                        case "copy":
                        case "paste":
                            copyPaste(action, node);
                            break;
                        default:
                            alert("Todo: appply action '" + action + "' to node " + node);
                    }
                });
            }

            /**
             * Draw configuration to the html.
             */
            $scope.drawConfiguration = function () {
                $.ui.dynatree.nodedatadefaults["icon"] = false; // Turn off icons by default
                var isMac = /Mac/.test(navigator.platform);
                var divContent = $("#snampTreeConfig");
                divContent.dynatree({
                    noLink: true,
                    selectMode: 1,
                    keyboard: false,
                    onClick: function(node) {
                        $scope.activeNode = node;
                        Core.$apply($scope);
                        if( $(".contextMenu:visible").length > 0 ){
                            $(".contextMenu").hide();
                        }
                    },
                    onKeydown: function(node, event) {
                        if( $(".contextMenu:visible").length > 0 )
                            return false;

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
                    onCreate: function(node, span){
                        bindContextMenu(span);
                    },
                    children: $scope.configurationJSON2Tree($scope.configuration)
                });

                // Before binding events to the tree - render inputs.
                divContent.dynatree("getTree").renderInvisibleNodes();
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