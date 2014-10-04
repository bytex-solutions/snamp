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
     * Managed Resource attribute
     */
    class attribute
    {
        attributeName:string;
        readWriteTimeout: number;
        additionalElements: { [s: string]: string; } = {};

        constructor ( attributeName:string="",readWriteTimeout: number=-1, additionalElements:any=null )
        {
            this.additionalElements = additionalElements;
            this.attributeName = attributeName;
            this.readWriteTimeout = readWriteTimeout;
        }

        constructor(json:Object = null)
        {
            if (json != null)
            {
                json
            }
            else
                constructor();
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
                var currentConnector:manRes = new manRes(propertyName, local['connectionString'], local['connectionType'],
                                                            local['additionalProperties'],local['attributes'], local['events']);


                var obj:Object = null;

                $.getJSON("/snamp/management/api/connectors/" + local['connectionType'].toLowerCase() + "/configurationSchema",
                    function (scheme) {
                        obj = scheme;
                        console.log(obj);

                    }
                );
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
