module config
{

    /**
     * Managed Resources typescript representation
     */
    class manRes
    {
        name:string;
        connectionString:string;
        connectionType:string;
        additionalProperties: { [s: string]: string; } = {};
        attributes: attribute[];
        events: event[];

        constructor (name:string="",connectionString:string="",connectionType:string="",additionalProperties:any=null,
            attributes:any=null, events:any=null)
        {
            this.name = name;
            this.connectionString = connectionString;
            this.connectionType = connectionType;
            this.additionalProperties = additionalProperties;
            this.attributes = attributes;
            this.events = events;
        }
    }


    /**
     * Connector attribute restrictions
     */
    class attributeAdditionalParamsRestriction
    {
        association: string[] = [];
        exclusion: string[] = [];
        extension: string[] = [];
        defaultValue: string = "";
        description: string = "";
        inputPattern: string = "";
        required: boolean = false;

        constructor(association:any = null, exclusion:any = null, extension:any = null,
                    defaultValue:string = "", description:string = "", inputPattern:string = "", required:boolean = false)
        {
            this.association = association;
            this.exclusion = exclusion;
            this.extension = extension;
            this.defaultValue = defaultValue;
            this.description = description;
            this.inputPattern = inputPattern;
            this.required = required;
        }
    }

    /**
     * Connector Attribute Additional Elements
     */
    class attributeAdditionalParam
    {
        paramName:string = "";
        paramRestriction:attributeAdditionalParamsRestriction = null;
        paramValue:string = "";

        constructor(paramName:string = "", paramRestriction:any = null, paramValue:string = "")
        {
            this.paramName = paramName;
            this.paramRestriction = paramRestriction;
            this.paramValue = paramValue;
        }
    }

    /**
     * Managed Resource attribute
     */
    class attribute
    {
        attributeId:string = "";
        attributeName:string = "";
        readWriteTimeout: number = -1;
        additionalElements: attributeAdditionalParam[] = [];

        constructor (attributeId:string="", attributeName:string="",readWriteTimeout: number=-1,
                     additionalElements:any=null)
        {
            this.additionalElements = additionalElements;
            this.attributeName = attributeName;
            this.attributeId = attributeId;
            this.readWriteTimeout = readWriteTimeout;
        }
    }

    /**
     * Managed Resources event
     */
    class event
    {
        category: string;
        parameters: { [s: string]: string; } = {};

        constructor (category:string="", parameters:any=null)
        {
            this.category = category;
            this.parameters = parameters;
        }
    }


    /**
     * Resource Adapter typescript representation
     */
    class resAdapters
    {
        adapterName:string;
        hostingParams : { [s: string]: string; } = {};

        constructor (adapterName:string="", hostingParams:any=null)
        {
            this.adapterName = adapterName;
            this.hostingParams = hostingParams;
        }
    }

    /**
     * External configuration
     */
    export class configuration
    {
        managedResources: manRes[];
        resourceAdapters: resAdapters[];

        constructor(managedResources:any=null, resourceAdapters:any=null)
        {
            this.managedResources = managedResources;
            this.resourceAdapters = resourceAdapters;
        }

        public parseJsonToManagedResources(data:Object=null)
        {
           var result:manRes[] = [];
           for (propertyName in data) {

                var local = data[propertyName];
                if (!local.hasOwnProperty("connectionType")) continue; // if it does not contain con.type -
                                                                       // it's not an instance of connector SNAMP

               // preparing attribute restrictions
               var restrictions: { [s: string]: attributeAdditionalParamsRestriction; } = {};
               $.ajax({
                       url: "/snamp/management/api/connectors/" + local['connectionType'].toLowerCase() + "/configurationSchema",
                       dataType: "json",
                       cache: false,
                       type: "GET",
                       async: false,
                       success: function (scheme) {
                           if (scheme.hasOwnProperty("attributeParameters"))
                               for (restrictionName in scheme['attributeParameters']) {
                                   var atrRes:attributeAdditionalParamsRestriction = new attributeAdditionalParamsRestriction(
                                       scheme['attributeParameters'][restrictionName]['ASSOCIATION'],
                                       scheme['attributeParameters'][restrictionName]['EXCLUSION'],
                                       scheme['attributeParameters'][restrictionName]['EXTENSION'],
                                       scheme['attributeParameters'][restrictionName]['defaultValue'],
                                       scheme['attributeParameters'][restrictionName]['description'],
                                       scheme['attributeParameters'][restrictionName]['inputPattern'],
                                       scheme['attributeParameters'][restrictionName]['required']
                                   );
                                   restrictions[restrictionName] = atrRes;
                               }
                       }
               });

               // preparing attribute parsing
               var attributes:attribute[] = [];
               for  (attributeId in local['attributes']) {

                   // attribute params preparation
                   var attrParam:attributeAdditionalParam[] = [];
                   for (paramName in local['attributes'][attributeId]['additionalProperties'])
                   {
                        var locAttrParam:attributeAdditionalParam = new attributeAdditionalParam(
                            paramName,
                            restrictions[paramName],
                            local['attributes'][attributeId]['additionalProperties'][paramName]
                        );
                       attrParam.push(locAttrParam);
                   }

                   // create new attribute
                   var locAtr:attribute = new attribute(
                       attributeId,
                       local['attributes'][attributeId]['name'],
                       local['attributes'][attributeId]['readWriteTimeout'],
                       attrParam
                   )

                   attributes.push(locAtr);
               }


                var currentConnector:manRes = new manRes(propertyName, local['connectionString'], local['connectionType'],
                                                            local['additionalProperties'], attributes, local['events']);

               // append connector whole information to the resulting object
               result.push(currentConnector);
            }
            return result;

        }

        public parseJsonToResourceAdapters(data:Object=null)
        {

        }

        constructor(data:Object=null)
        {
            if (data == null || (!data.hasOwnProperty("managedResources") && !data.hasOwnProperty("resourceAdapters")))
                this.constructor(null,null);
            else
            {
                this.managedResources = data.hasOwnProperty("managedResources")? this.parseJsonToManagedResources(data['managedResources']): [];
                this.resourceAdapters= data.hasOwnProperty("resourceAdapters")? this.parseJsonToResourceAdapters(data['resourceAdapters']): [];
            }
        }
    }
}
