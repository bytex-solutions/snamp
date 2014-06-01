export module com.snamp.models
{
    export class managmentTarget
    {
        connectionString: string;
        events: number;
        attributes: number;

        constructor(connectionString:string="",events:number=0, attributes:number=0)
        {
            this.connectionString = connectionString;
            this.events = events;
            this.attributes = attributes;
        }
    }

}