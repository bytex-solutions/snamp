export module com.snamp.models
{

    export class license
    {
        description: string;

        constructor(public description:string = "There's no available description")
        {
            this.description = description;
        }
    }
}