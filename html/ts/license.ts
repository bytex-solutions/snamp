module license
{

    export class licenseInfo
    {
        description: string;

        constructor(public descr:string = "There's no available description")
        {
            this.description = descr;
        }
    }
}